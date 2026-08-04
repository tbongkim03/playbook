package playbook.encore.back.allowip.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import playbook.encore.back.allowip.service.IpAllowlistEvaluator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 허용 IP 외 접속을 차단하는 필터. 인터셉터보다 앞에서 동작한다.
 *
 * <p><b>fail-open 원칙</b>: 이 필터에서 예외가 나면 요청을 통과시킨다.
 * DB 장애·설정 오류로 전 캠퍼스가 잠기는 사고를 막기 위한 것이며, 예외는 ERROR 로그로 남긴다.
 *
 * <p>차단 응답은 JSON 래핑을 쓰지 않는다 — 프론트 정적 파일 요청도 이 필터를 타므로
 * 브라우저가 본문을 그대로 읽어야 한다.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class IpAllowlistFilter extends OncePerRequestFilter {

    /** Micrometer 메터명. Prometheus 렌더링 시 playbook_ip_blocked_total 로 노출된다. */
    private static final String METER_NAME = "playbook.ip.blocked";
    private static final String TAG_OVERFLOW = "__other__";
    private static final String TAG_UNKNOWN = "unknown";
    private static final long BLOCK_LOG_INTERVAL_MS = 10_000L;

    private final IpAllowlistEvaluator evaluator;
    private final ClientIpResolver clientIpResolver;
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    /**
     * ip 태그의 서로 다른 값 상한. 계약서는 playbook_ip_blocked_total{ip="..."} 로만 적혀 있으나
     * 원시 IP를 무제한 태그로 쓰면 카디널리티가 폭발하므로 상한을 두고 초과분은 __other__ 로 접는다.
     */
    @Value("${playbook.ip-allowlist.metric-ip-cardinality-limit:100}")
    private int metricIpCardinalityLimit;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final AtomicLong lastBlockLogAt = new AtomicLong(0L);

    @PostConstruct
    public void warnOnRiskyConfig() {
        if (evaluator.isEnabled() && clientIpResolver.hasNoTrustedProxy()) {
            log.warn("[IpAllowlist] 필터가 활성인데 playbook.ip-allowlist.trusted-proxies 가 비어 있습니다. "
                    + "리버스 프록시(nginx) 뒤에 있다면 모든 요청이 프록시 IP로 보여 사실상 차단이 동작하지 않습니다.");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean allow;
        String clientIp = null;

        // 판정 구간에서만 예외를 삼킨다. chain.doFilter 는 try 밖에서 호출해야 이중 실행이 없다.
        try {
            if (!evaluator.isEnabled()) {
                allow = true;
            } else {
                clientIp = clientIpResolver.resolve(request);
                allow = evaluator.evaluate(clientIp, true).allowed();
            }
        } catch (Exception e) {
            log.error("[IpAllowlist] 판정 중 예외 발생 - 요청을 통과시킵니다(fail-open). uri={}",
                    request.getRequestURI(), e);
            allow = true;
        }

        if (allow) {
            filterChain.doFilter(request, response);
            return;
        }

        countBlocked(clientIp);
        logBlockedThrottled(clientIp, request);
        writeForbidden(response, clientIp);
    }

    private void writeForbidden(HttpServletResponse response, String clientIp) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.reset();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write("허용되지 않은 접속 위치입니다. (IP: " + (clientIp == null ? "unknown" : clientIp) + ")");
        response.getWriter().flush();
    }

    /**
     * 차단 카운터 증가.
     * 서로 다른 ip 태그가 상한에 도달하면 이후 신규 IP는 __other__ 로 집계해 카디널리티를 묶는다.
     */
    private void countBlocked(String clientIp) {
        try {
            MeterRegistry registry = meterRegistryProvider.getIfAvailable();
            if (registry == null) {
                return;
            }
            String tag = (clientIp == null || clientIp.isBlank()) ? TAG_UNKNOWN : clientIp;
            Counter counter = counters.get(tag);
            if (counter == null) {
                if (counters.size() >= Math.max(1, metricIpCardinalityLimit)) {
                    tag = TAG_OVERFLOW;
                }
                final String finalTag = tag;
                counter = counters.computeIfAbsent(finalTag, t -> Counter.builder(METER_NAME)
                        .description("IP 허용목록 필터가 차단한 요청 수")
                        .tag("ip", t)
                        .register(registry));
            }
            counter.increment();
        } catch (Exception e) {
            log.error("[IpAllowlist] 차단 카운터 기록 실패", e);
        }
    }

    private void logBlockedThrottled(String clientIp, HttpServletRequest request) {
        long now = System.currentTimeMillis();
        long last = lastBlockLogAt.get();
        if (now - last >= BLOCK_LOG_INTERVAL_MS && lastBlockLogAt.compareAndSet(last, now)) {
            log.warn("[IpAllowlist] 차단 - ip={}, method={}, uri={}",
                    clientIp, request.getMethod(), request.getRequestURI());
        }
    }
}

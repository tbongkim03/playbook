package playbook.encore.back.allowip.filter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import playbook.encore.back.allowip.util.IpRangeMatcher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 클라이언트 IP 결정 규칙 (계약서 "차단 필터 명세" — 2026-08-04 정정본).
 *
 * <ol>
 *   <li>playbook.ip-allowlist.trusted-proxies 에 포함된 원격주소에서 온 요청만
 *       X-Forwarded-For 의 <b>마지막 항목</b>을 클라이언트 IP 로 본다</li>
 *   <li>신뢰 프록시인데 XFF 가 없거나 부정하면 <b>null 을 반환</b>한다 (ERROR 로그)</li>
 *   <li>신뢰 프록시가 아닌 원격주소면 request.getRemoteAddr() 을 쓴다 (헤더 무시)</li>
 * </ol>
 *
 * <p><b>왜 마지막 항목인가 (보안감사 S-1, CRITICAL)</b><br>
 * nginx 의 {@code $proxy_add_x_forwarded_for} 는 정의상 {@code $http_x_forwarded_for, $remote_addr} 다.
 * 즉 <b>클라이언트가 보낸 값이 체인 맨 앞에 붙고, nginx 가 관측한 진짜 IP 는 맨 뒤에 붙는다.</b>
 * 첫 항목을 신뢰하면 {@code curl -H "X-Forwarded-For: 127.0.0.1"} 한 줄로 차단이 전면 무력화된다.
 * 프록시 1단 구성에서 신뢰할 수 있는 유일한 항목은 <b>마지막 항목</b>이다.
 * nginx 가 {@code $remote_addr} 로 덮어쓰도록 고쳐도(1차 방어), 그 설정이 회귀했을 때
 * 마지막 항목은 여전히 nginx 가 붙인 실제 {@code $remote_addr} 이므로 안전하다(2차 방어).
 *
 * <p><b>왜 폴백에서 remote 를 쓰지 않는가 (보안감사 S-2, HIGH)</b><br>
 * 신뢰 프록시 뒤에서 XFF 해석에 실패했을 때 {@code getRemoteAddr()}(= 프록시 IP)로 폴백하면,
 * 그 값이 internal-networks 상시허용 대역에 매칭되어 <b>차단이 아니라 전면 허용</b>이 된다.
 * 그래서 null 을 반환해 evaluator 의 UNRESOLVED(fail-open) 경로로 보낸다 —
 * 잠김 방지를 위해 통과시키되, <b>정상 통과와 구분되도록 ERROR 로 관측 가능하게</b> 남긴다.
 *
 * <p>필터와 컨트롤러(my-ip)가 같은 규칙을 쓰도록 공용 컴포넌트로 분리했다.
 */
@Slf4j
@Component
public class ClientIpResolver {

    private static final String HEADER_XFF = "X-Forwarded-For";
    /** 프록시 설정 오류 ERROR 로그 스로틀 간격(ms). 정적 자산마다 찍혀 로그가 폭주하는 것을 막는다. */
    private static final long PROXY_ERROR_INTERVAL_MS = 60_000L;

    /** 신뢰 프록시 CIDR 목록 (비어 있으면 XFF 를 전혀 신뢰하지 않는다) */
    @Value("${playbook.ip-allowlist.trusted-proxies:}")
    private String trustedProxiesRaw;

    private List<IpRangeMatcher> trustedProxies = Collections.emptyList();

    private final AtomicLong lastProxyErrorAt = new AtomicLong(0L);

    @PostConstruct
    public void init() {
        this.trustedProxies = IpRangeMatcher.parseList(trustedProxiesRaw);
        log.info("[ClientIpResolver] 신뢰 프록시 {}건 — XFF 는 마지막 항목을 사용합니다.", trustedProxies.size());
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String remote = normalize(request.getRemoteAddr());
        if (remote == null) {
            return null;
        }
        if (IpRangeMatcher.matchesAny(trustedProxies, remote)) {
            // 신뢰 프록시 뒤 → 클라이언트 IP 는 반드시 헤더에서 와야 한다.
            String clientIp = lastValidXffEntry(request.getHeader(HEADER_XFF));
            if (clientIp != null) {
                return clientIp;
            }
            // 프록시가 XFF 를 안 붙였거나 값이 부정하다 = 프록시 설정 오류.
            // remote(프록시 IP)로 폴백하면 상시허용 대역에 매칭되어 전면 허용이 되므로 null 을 돌려준다.
            logProxyMisconfigThrottled(remote, request);
            return null;
        }
        return remote;
    }

    /**
     * 감사·접속이력 기록용 클라이언트 IP. {@link #resolve(HttpServletRequest)} 와 같은 규칙을 쓰되
     * <b>절대 null 을 돌려주지 않는다</b> — {@code tb_access_log.ip_address} 가 NOT NULL 이기 때문이다.
     *
     * <p>판정용({@code resolve})은 확정하지 못하면 null 을 돌려 fail-open 으로 넘기지만,
     * 기록용은 "모름"보다 "프록시 IP"가 낫다. 어느 쪽이든 <b>클라이언트가 보낸 XFF 를
     * 그대로 믿지 않는다</b>는 점이 핵심이다 (보안감사 S-10).
     *
     * <p>이전에는 {@code WebUtil.getClientIp} 가 신뢰 프록시 검사 없이 XFF 첫 항목을 그대로 썼고,
     * 그래서 누구나 임의 IP 로 로그인 이력을 남길 수 있었다 — 침해 사고 조사 시 로그가 오염된다.
     */
    public String resolveForAudit(HttpServletRequest request) {
        String resolved = resolve(request);
        if (resolved != null) {
            return resolved;
        }
        String remote = request != null ? normalize(request.getRemoteAddr()) : null;
        return remote != null ? remote : "unknown";
    }

    /**
     * X-Forwarded-For 체인의 <b>마지막</b> 유효 항목을 돌려준다.
     * 프록시 1단 구성에서 nginx 가 마지막에 붙인 항목만이 위조 불가능한 값이다.
     *
     * @return 유효 리터럴이 없으면 null
     */
    private String lastValidXffEntry(String xff) {
        if (xff == null || xff.isBlank()) {
            return null;
        }
        String[] entries = xff.split(",");
        for (int i = entries.length - 1; i >= 0; i--) {
            String candidate = normalize(entries[i]);
            if (candidate != null && IpRangeMatcher.isValidLiteral(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void logProxyMisconfigThrottled(String remote, HttpServletRequest request) {
        long now = System.currentTimeMillis();
        long last = lastProxyErrorAt.get();
        if (now - last >= PROXY_ERROR_INTERVAL_MS && lastProxyErrorAt.compareAndSet(last, now)) {
            log.error("[ClientIpResolver] 신뢰 프록시({})에서 온 요청에 유효한 X-Forwarded-For 가 없습니다. "
                            + "프록시 설정을 확인하세요 (nginx: proxy_set_header X-Forwarded-For $remote_addr). "
                            + "클라이언트 IP 미확정으로 이 요청은 통과시킵니다(fail-open). uri={}, XFF={}",
                    remote, request.getRequestURI(), request.getHeader(HEADER_XFF));
        }
    }

    /** 신뢰 프록시가 하나도 설정되지 않은 상태인지 (기동 경고용) */
    public boolean hasNoTrustedProxy() {
        return trustedProxies.isEmpty();
    }

    private String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return null;
        }
        // IPv6 스코프 ID(fe80::1%eth0) 제거
        int pct = s.indexOf('%');
        if (pct > 0) {
            s = s.substring(0, pct);
        }
        return s;
    }
}

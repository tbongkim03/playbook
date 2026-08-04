package playbook.encore.back.allowip.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import playbook.encore.back.allowip.dao.AllowedIpRepository;
import playbook.encore.back.allowip.entity.AllowedIp;
import playbook.encore.back.allowip.util.IpRangeMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IP 허용목록 판정 엔진 + 규칙 메모리 캐시.
 *
 * <p><b>잠김 방지(fail-open)가 이 클래스의 최우선 요건이다.</b>
 * <ol>
 *   <li>필터 비활성(기본값) → 통과</li>
 *   <li>루프백 → 통과</li>
 *   <li>IP_ALLOWLIST_BYPASS 대역 → 통과</li>
 *   <li>Docker 내부망 → 통과</li>
 *   <li>활성 규칙 0건 → 통과 (WARN, 60초 스로틀)</li>
 *   <li>활성 규칙 매칭 → 통과</li>
 *   <li>그 외 → 차단</li>
 * </ol>
 * DB 조회 실패 시 기존 캐시를 유지하고, 최초 로드 실패면 빈 목록(=0건=전면 허용)으로 남는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpAllowlistEvaluator {

    /** 규칙 0건 경고 로그 스로틀 간격(ms) */
    private static final long NO_RULE_WARN_INTERVAL_MS = 60_000L;

    private final AllowedIpRepository allowedIpRepository;

    /** 차단 필터 활성 여부. 기본 비활성 — 실수로 dev/test 가 잠기지 않게 한다. */
    @Value("${playbook.ip-allowlist.enabled:false}")
    private boolean enabled;

    /** 비상 우회 CIDR 목록. 프로퍼티가 없으면 환경변수 IP_ALLOWLIST_BYPASS 를 본다. */
    @Value("${playbook.ip-allowlist.bypass:${IP_ALLOWLIST_BYPASS:}}")
    private String bypassRaw;

    /**
     * 항상 허용할 내부망 대역. <b>기본값은 빈 값이다 (보안감사 S-2 대응).</b>
     *
     * <p>과거 기본값은 {@code 172.16.0.0/12} 였으나 trusted-proxies 와 같은 대역이라,
     * XFF 해석 실패 시 폴백된 nginx 컨테이너 IP 가 곧바로 여기에 매칭되어
     * "차단"이 아니라 <b>전면 허용</b>이 되는 우회로가 됐다.
     * 이제 ClientIpResolver 가 그 상황에서 null 을 돌려주므로(UNRESOLVED fail-open) 이 대역이 필요 없고,
     * 값이 겹치면 위험하므로 비워 둔다. 설정하려면 <b>trusted-proxies 와 겹치지 않는 대역</b>이어야 한다.
     */
    @Value("${playbook.ip-allowlist.internal-networks:}")
    private String internalNetworksRaw;

    /** 겹침 검사(S-2 회귀 방지) 전용. 실제 XFF 판정은 ClientIpResolver 가 한다. */
    @Value("${playbook.ip-allowlist.trusted-proxies:}")
    private String trustedProxiesRaw;

    private List<IpRangeMatcher> bypassMatchers = Collections.emptyList();
    private List<IpRangeMatcher> internalMatchers = Collections.emptyList();

    /** 활성 규칙 스냅샷 (불변 리스트로 통째 교체 → 락 없이 읽기) */
    private volatile List<CachedRule> rules = Collections.emptyList();

    private final AtomicLong lastNoRuleWarnAt = new AtomicLong(0L);

    @PostConstruct
    public void init() {
        this.bypassMatchers = IpRangeMatcher.parseList(bypassRaw);
        this.internalMatchers = IpRangeMatcher.parseList(internalNetworksRaw);
        warnOnProxyOverlap();

        if (!bypassMatchers.isEmpty()) {
            log.warn("[IpAllowlist] 비상 우회(IP_ALLOWLIST_BYPASS) {}건이 활성화되어 있습니다. "
                    + "복구 후 반드시 제거하세요.", bypassMatchers.size());
        }
        reload();
        log.info("[IpAllowlist] 초기화 완료 - enabled={}, 활성규칙 {}건, 내부망 {}건, 우회 {}건",
                enabled, rules.size(), internalMatchers.size(), bypassMatchers.size());
    }

    /** 규칙 캐시를 DB에서 다시 읽는다. CRUD 직후 호출한다. */
    public void reload() {
        try {
            List<AllowedIp> active = allowedIpRepository.findActiveRules();
            List<CachedRule> next = new ArrayList<>(active.size());
            for (AllowedIp a : active) {
                IpRangeMatcher matcher = IpRangeMatcher.parse(a.getIpValue());
                if (matcher == null) {
                    log.warn("[IpAllowlist] 파싱 불가 규칙 무시 - seq={}, value={}", a.getSeqAllowedIp(), a.getIpValue());
                    continue;
                }
                next.add(new CachedRule(a.getSeqAllowedIp(), a.getIpValue(), matcher));
            }
            this.rules = Collections.unmodifiableList(next);
        } catch (Exception e) {
            // DB 장애로 전 캠퍼스가 잠기면 안 된다. 기존 캐시를 유지하고 통과 판정을 이어간다.
            log.error("[IpAllowlist] 규칙 캐시 갱신 실패 - 기존 캐시 {}건 유지(fail-open)", rules.size(), e);
        }
    }

    // ===== 판정 =====

    /**
     * @param respectEnabledFlag true 면 필터 비활성 시 즉시 DISABLED(통과) 반환.
     *                           my-ip 조회는 false 로 호출해 "켜져 있었다면 통과했을지"를 본다.
     */
    public Decision evaluate(String clientIp, boolean respectEnabledFlag) {
        if (respectEnabledFlag && !enabled) {
            return new Decision(Outcome.DISABLED, null, null);
        }
        byte[] addr = IpRangeMatcher.parseAddress(clientIp);
        if (addr == null) {
            // 클라이언트 IP를 해석하지 못하면 차단하지 않는다 (fail-open)
            log.warn("[IpAllowlist] 클라이언트 IP 해석 실패 - 통과 처리: {}", clientIp);
            return new Decision(Outcome.UNRESOLVED, null, null);
        }
        if (IpRangeMatcher.isLoopback(clientIp)) {
            return new Decision(Outcome.LOOPBACK, null, null);
        }
        if (IpRangeMatcher.matchesAny(bypassMatchers, clientIp)) {
            return new Decision(Outcome.BYPASS, null, null);
        }
        if (IpRangeMatcher.matchesAny(internalMatchers, clientIp)) {
            return new Decision(Outcome.INTERNAL, null, null);
        }

        List<CachedRule> snapshot = this.rules;
        if (snapshot.isEmpty()) {
            warnNoRulesThrottled();
            return new Decision(Outcome.NO_RULES, null, null);
        }
        for (CachedRule r : snapshot) {
            if (r.matcher().matches(addr)) {
                return new Decision(Outcome.MATCHED, r.seq(), r.value());
            }
        }
        return new Decision(Outcome.BLOCKED, null, null);
    }

    /**
     * DB 규칙과 무관하게 항상 통과하는 주소인지 (루프백·우회·내부망·해석불가).
     * 자기차단 방지 검사에서 "어차피 잠기지 않는 요청자"를 걸러내는 데 쓴다.
     */
    public boolean isAlwaysAllowed(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return true;
        }
        if (!IpRangeMatcher.isValidLiteral(clientIp)) {
            return true;
        }
        return IpRangeMatcher.isLoopback(clientIp)
                || IpRangeMatcher.matchesAny(bypassMatchers, clientIp)
                || IpRangeMatcher.matchesAny(internalMatchers, clientIp);
    }

    /**
     * internal-networks 가 trusted-proxies 와 겹치면 프록시 IP 자체가 상시허용이 되어
     * 차단이 무력화된다 (보안감사 S-2). 설정으로 되돌아가는 회귀를 기동 시점에 잡는다.
     */
    private void warnOnProxyOverlap() {
        List<IpRangeMatcher> trusted = IpRangeMatcher.parseList(trustedProxiesRaw);
        for (IpRangeMatcher internal : internalMatchers) {
            for (IpRangeMatcher proxy : trusted) {
                if (internal.overlaps(proxy)) {
                    log.error("[IpAllowlist] 설정 충돌 - internal-networks({}) 가 trusted-proxies({}) 와 겹칩니다. "
                                    + "프록시 IP 자체가 상시허용이 되어 IP 차단이 무력화될 수 있습니다. "
                                    + "두 값을 겹치지 않게 분리하세요.",
                            internal, proxy);
                }
            }
        }
    }

    private void warnNoRulesThrottled() {
        long now = System.currentTimeMillis();
        long last = lastNoRuleWarnAt.get();
        if (now - last >= NO_RULE_WARN_INTERVAL_MS && lastNoRuleWarnAt.compareAndSet(last, now)) {
            log.warn("[IpAllowlist] 활성 허용 IP 규칙이 0건입니다 - 모든 접속을 허용합니다(잠김 방지 fail-open).");
        }
    }

    // ===== 상태 조회 =====

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isBypassActive() {
        return !bypassMatchers.isEmpty();
    }

    public int getActiveRuleCount() {
        return rules.size();
    }

    /** 필터 판정 결과 */
    public enum Outcome {
        DISABLED, UNRESOLVED, LOOPBACK, BYPASS, INTERNAL, NO_RULES, MATCHED, BLOCKED
    }

    public record Decision(Outcome outcome, Integer matchedRuleSeq, String matchedRuleValue) {
        public boolean allowed() {
            return outcome != Outcome.BLOCKED;
        }
    }

    private record CachedRule(Integer seq, String value, IpRangeMatcher matcher) {
    }
}

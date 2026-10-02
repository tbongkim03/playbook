package playbook.encore.back.allowip.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import playbook.encore.back.allowip.dao.AllowedIpRepository;
import playbook.encore.back.allowip.entity.AllowedIp;
import playbook.encore.back.allowip.util.IpRangeMatcher;

/**
 * 설치 마법사가 .env 에 넣은 IP_ALLOWLIST_BOOTSTRAP(쉼표 구분 CIDR 목록)을
 * <b>tb_allowed_ip 가 비어 있을 때만</b> is_system = true 규칙으로 시드한다.
 *
 * <p>IntegrationService.seedConfigs() 의 "이미 있으면 건너뛴다" 패턴을 따른다.
 * 중복 생성 방지 판단은 소프트 삭제분까지 포함한 네이티브 카운트로 한다 —
 * 사용자가 시드 규칙을 전부 삭제한 뒤 재기동해도 부활하지 않아야 하기 때문이다.
 *
 * <p>IpAllowlistEvaluator 를 주입받아 이 빈보다 먼저 초기화되도록 하고, 시드 후 캐시를 갱신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AllowedIpBootstrapSeeder {

    private final AllowedIpRepository allowedIpRepository;
    private final IpAllowlistEvaluator evaluator;

    /** 프로퍼티가 없으면 환경변수 IP_ALLOWLIST_BOOTSTRAP 을 본다. */
    @Value("${playbook.ip-allowlist.bootstrap:${IP_ALLOWLIST_BOOTSTRAP:}}")
    private String bootstrapRaw;

    @PostConstruct
    public void init() {
        try {
            if (bootstrapRaw == null || bootstrapRaw.isBlank()) {
                return;
            }
            long existing = allowedIpRepository.countAllIncludingDeleted();
            if (existing > 0) {
                log.info("[AllowedIpBootstrap] tb_allowed_ip 에 이미 {}건이 있어 부트스트랩 시딩을 건너뜁니다.", existing);
                return;
            }

            int seeded = 0;
            for (String token : bootstrapRaw.split(",")) {
                String value = token.trim();
                if (value.isEmpty()) {
                    continue;
                }
                if (IpRangeMatcher.parse(value) == null) {
                    log.warn("[AllowedIpBootstrap] 표기가 올바르지 않아 건너뜁니다: {}", value);
                    continue;
                }
                allowedIpRepository.save(AllowedIp.builder()
                        .ipValue(value)
                        .ipType(IpRangeMatcher.resolveType(value))
                        .seqCampus(null)          // 전역 규칙
                        .description("설치 마법사 초기 등록")
                        .isActive(true)
                        .isSystem(true)
                        .build());
                seeded++;
            }

            if (seeded > 0) {
                evaluator.reload();
                log.warn("[AllowedIpBootstrap] 부트스트랩 허용 IP {}건을 시드했습니다. 관리자탭에서 확인하세요.", seeded);
            }
        } catch (Exception e) {
            // 시딩 실패가 기동을 막으면 안 된다. 규칙 0건이면 필터는 전면 허용으로 동작한다.
            log.error("[AllowedIpBootstrap] 부트스트랩 시딩 실패 - 무시하고 기동을 계속합니다.", e);
        }
    }
}

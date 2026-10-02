package playbook.encore.back.allowip;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import playbook.encore.back.allowip.service.AllowedIpBootstrapSeeder;
import playbook.encore.back.allowip.service.IpAllowlistEvaluator;
import playbook.encore.back.common.BaseIntegrationTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 설치 마법사 6단계의 초기 허용 대역(IP_ALLOWLIST_BOOTSTRAP)이 tb_allowed_ip 로 시드되는지.
 *
 * <p>시더는 컨텍스트 기동 시 한 번 도는데, 공유 테스트 DB 에는 이미 행이 있을 수 있다.
 * 그래서 테이블을 비운 뒤 {@link AllowedIpBootstrapSeeder#init()} 을 직접 다시 호출한다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = "playbook.ip-allowlist.bootstrap=192.0.2.0/24, 198.51.100.7 ,잘못된값")
class AllowedIpBootstrapSeederTest extends BaseIntegrationTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AllowedIpBootstrapSeeder seeder;
    @Autowired private IpAllowlistEvaluator evaluator;

    @BeforeEach
    void wipe() {
        jdbcTemplate.update("DELETE FROM tb_allowed_ip");
    }

    @AfterAll
    void cleanup() {
        jdbcTemplate.update("DELETE FROM tb_allowed_ip");
        evaluator.reload();
    }

    @Test @Order(1)
    @DisplayName("BS1: 테이블이 비어 있으면 부트스트랩 값을 is_system 전역 규칙으로 시드한다 (잘못된 표기는 건너뜀)")
    void BS1_빈테이블_시드() {
        seeder.init();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT ip_value, is_system, is_active, seq_campus, use_yn FROM tb_allowed_ip ORDER BY ip_value");
        assertThat(rows).extracting(r -> r.get("ip_value")).containsExactly("192.0.2.0/24", "198.51.100.7");
        for (Map<String, Object> r : rows) {
            assertThat(truthy(r.get("is_system"))).isTrue();
            assertThat(truthy(r.get("is_active"))).isTrue();
            assertThat(r.get("seq_campus")).isNull();
            assertThat(r.get("use_yn")).isEqualTo("Y");
        }
    }

    /** BIT(1)/TINYINT 어느 쪽으로 매핑돼도 읽는다 */
    private static boolean truthy(Object v) {
        return v instanceof Boolean b ? b : ((Number) v).intValue() == 1;
    }

    @Test @Order(2)
    @DisplayName("BS2: 소프트 삭제분이라도 행이 있으면 다시 시드하지 않는다")
    void BS2_기존행있으면_건너뜀() {
        seeder.init();
        jdbcTemplate.update("UPDATE tb_allowed_ip SET use_yn = 'N'");
        seeder.init();

        Integer active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_allowed_ip WHERE use_yn = 'Y'", Integer.class);
        assertThat(active).isZero();
    }
}

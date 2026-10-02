package playbook.encore.back.monitoring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 캠퍼스 측 모니터링 토큰 회전용 설정.
 * {@code monitoring.token.refresh.enabled=true}(캠퍼스 배포 번들)일 때만 빈이 만들어진다 —
 * dev/local/모니터링 미사용 prod에선 전혀 생성되지 않아 스케줄러도 돌지 않는다.
 */
@Configuration
@ConditionalOnProperty(prefix = "monitoring.token.refresh", name = "enabled", havingValue = "true")
public class MonitoringConfig {

    /** 백그라운드 갱신용 — 짧은 타임아웃으로 멈춤 방지. */
    @Bean
    public RestTemplate monitoringRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(8_000);
        return new RestTemplate(factory);
    }
}

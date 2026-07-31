package playbook.encore.back.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import playbook.encore.back.interceptor.LoginCheckInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    /** 허용 출처 목록 (쉼표 구분). 빈 값이면 CORS 매핑을 등록하지 않는다. */
    @Value("${playbook.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // allowCredentials(true) 와 "*" 를 함께 쓰면 임의 출처에서 세션 쿠키가 실린 요청이 가능해진다.
        // 설정 실수로 다시 열리는 것을 막기 위해 조용히 무시하지 않고 기동을 실패시킨다.
        if (origins.contains("*")) {
            throw new IllegalStateException(
                    "playbook.cors.allowed-origins 에 \"*\" 를 쓸 수 없습니다. " +
                    "allowCredentials(true) 와 함께 쓰면 임의 출처에서 세션 쿠키가 실린 요청이 가능합니다. " +
                    "실제 서비스 도메인을 쉼표로 구분해 지정하세요.");
        }

        if (origins.isEmpty()) {
            // 배포 환경 기본값. nginx 가 프론트와 /api 를 같은 오리진으로 서빙하므로 교차 출처 요청이 없다.
            log.info("[WebConfig] CORS 허용 출처 미설정 — 교차 출처 요청을 차단합니다.");
            return;
        }

        log.info("[WebConfig] CORS 허용 출처: {}", origins);
        registry.addMapping("/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/history/borrow", "/history/return", "/history/book", "/history/me")
                .addPathPatterns("/history/popular/first", "/history/popular/first/*")
                .addPathPatterns("/history/popular/second", "/history/popular/second/*")
                .addPathPatterns("/history/rank", "/history/rank/*")
                .addPathPatterns("/users/me", "/admin/me")
                .addPathPatterns("/users")
                .addPathPatterns("/admin")
                .addPathPatterns("/users/password", "/admin/password")
                .addPathPatterns("/users/update", "/users/admin/reset-password")
                .addPathPatterns("/admin/discord", "/admin/update")
                .addPathPatterns("/users/list")
                .addPathPatterns("/admin/register", "/admin/register/validate")
                .addPathPatterns("/users/course")
                .addPathPatterns("/users/validate", "/admin/validate")
                .addPathPatterns("/admin/list")
                .addPathPatterns("/naver/book-search", "/national-library/isbn")
                .addPathPatterns("/favor")
                .addPathPatterns("/history/me")
                // books 관련 관리자 전용 경로들 추가
                // (GET 공개 조회는 LoginCheckInterceptor에서 메서드 기준으로 허용)
                .addPathPatterns("/books") // POST 요청 포함
                .addPathPatterns("/books/*") // PUT, DELETE 요청 포함
                .addPathPatterns("/books/all")
                .addPathPatterns("/books/count")
                .addPathPatterns("/books/batch/print")
                .addPathPatterns("/books/unprinted")
                .addPathPatterns("/books/check/barcode")
                .addPathPatterns("/books/admin/list")
                .addPathPatterns("/books/export")
                .addPathPatterns("/admin/export")
                .addPathPatterns("/admin/discord/link-message")
                .addPathPatterns("/users/export")
                .addPathPatterns("/history/export")
                .addPathPatterns("/history/book/*")
                // campus 관련 관리자 전용 경로 추가
                .addPathPatterns("/campus/all") // 모든 캠퍼스 조회 (관리자 전용)
                .addPathPatterns("/campus/*") // 캠퍼스 상세, 수정, 삭제 (관리자 전용)
                .addPathPatterns("/campus") // POST 요청 (캠퍼스 생성, 관리자 전용)
                .addPathPatterns("/admin/access-log")
                .addPathPatterns("/admin/audit-log")
                // 연동 관리 (전체관리자 전용) - 인증 컨텍스트 필요
                .addPathPatterns("/integration", "/integration/**")
                .excludePathPatterns("/users/login", "/users/register");
    }
}

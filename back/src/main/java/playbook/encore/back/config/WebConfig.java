package playbook.encore.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import playbook.encore.back.interceptor.LoginCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
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

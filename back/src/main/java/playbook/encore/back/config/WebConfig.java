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
                .allowedOrigins("*") // 모든 접속 허용(test할 때 만 열어두기)
                //.allowedOrigins("http://localhost:8081")  // 허용할 주소
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false);
                // .allowCredentials(true);
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
                .addPathPatterns("/admin/discord", "/admin/update")
                .addPathPatterns("/users/list")
                .addPathPatterns("/admin/register", "/admin/register/validate")
                .addPathPatterns("/users/course")
                .addPathPatterns("/users/validate", "/admin/validate")
                .addPathPatterns("/admin/list")
                .addPathPatterns("/naver/book-search", "/national-library/isbn", "/work24/course")
                .addPathPatterns("/favor")
                .addPathPatterns("/history/me")
                // books 관련 관리자 전용 경로들 추가
                .addPathPatterns("/books") // POST 요청 포함
                .addPathPatterns("/books/*") // PUT, DELETE 요청 포함
                .addPathPatterns("/books/all")
                .addPathPatterns("/books/count")
                .addPathPatterns("/books/batch/print")
                .addPathPatterns("/books/unprinted")
                .addPathPatterns("/books/check/barcode")
                // campus 관련 관리자 전용 경로 추가
                .addPathPatterns("/campus/all") // 모든 캠퍼스 조회 (관리자 전용)
                .addPathPatterns("/campus/*") // 캠퍼스 상세, 수정, 삭제 (관리자 전용)
                .addPathPatterns("/campus") // POST 요청 (캠퍼스 생성, 관리자 전용)
                .excludePathPatterns("/users/login", "/users/register");
    }
}

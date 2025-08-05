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
                .addPathPatterns("/users/me", "/admin/me")
                .addPathPatterns("/users/password", "/admin/password")
                .addPathPatterns("/users/discord", "/admin/discord")
                .addPathPatterns("/users/course")
                .addPathPatterns("/users/validate", "/admin/validate")
                // books 관련 관리자 전용 경로들 추가
                .addPathPatterns("/books") // POST 요청 포함
                .addPathPatterns("/books/*") // PUT, DELETE 요청 포함
                .addPathPatterns("/books/count")
                .addPathPatterns("/books/batch/print")
                .addPathPatterns("/books/unprinted")
                .addPathPatterns("/books/check/barcode")
                .excludePathPatterns("/users/login", "/users/register");
    }
}

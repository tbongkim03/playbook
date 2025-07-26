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
                .addPathPatterns("/**") // 검증해야하는 경우
                .excludePathPatterns("/users/login", "/users/register"); // 검증이 필요 없는 경우
    }
}

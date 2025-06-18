package playbook.encore.back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 모든 접속 허용(test할때만 열어두기)
                //.allowedOrigins("http://localhost:8081")  // 허용할 주소
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false);
                // .allowCredentials(true);
    }
}

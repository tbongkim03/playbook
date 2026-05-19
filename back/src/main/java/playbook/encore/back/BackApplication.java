package playbook.encore.back;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackApplication.class, args);
        log.info("=============================================");
        log.info("  Playbook 서비스가 시작되었습니다.");
        log.info("=============================================");
    }

}

package playbook.encore.back.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.repository.AdminRepository;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MasterAdminInitializer {

    private final AdminRepository adminRepository;

    @Value("${MASTER_ID:admin}")
    private String masterId;

    @Value("${MASTER_PW:admin1234}")
    private String masterPassword;

    @Value("${MASTER_NAME:관리자}")
    private String masterName;

    @Value("${MASTER_DISCORD:bubble_94}")
    private String masterDiscord;

    @Bean
    public ApplicationRunner initializeMasterAdmin() {
        return args -> {
            if (masterId == null || masterId.trim().isEmpty() ||
                    masterPassword == null || masterPassword.trim().isEmpty()) {
                log.warn("마스터 어드민 계정 정보가 설정되지 않았습니다. MASTER_ID와 MASTER_PASSWORD를 확인해주세요.");
                return;
            }

            if (adminRepository.findByIdAdmin(masterId).isPresent()) {
                log.info("마스터 어드민 계정이 이미 존재합니다: {}", masterId);
                return;
            }

            try {
                String hashedPassword = BCrypt.hashpw(masterPassword, BCrypt.gensalt());

                Admin masterAdmin = Admin.builder()
                        .idAdmin(masterId)
                        .pwAdmin(hashedPassword)
                        .nameAdmin(masterName)
                        .dcAdmin(masterDiscord)
                        .agreeTermsAdmin(true)
                        .agreeInfoAdmin(true)
                        .agreeDiscordAlarmAdmin(false)
                        .statusAdmin(Admin.StatusTypeAdmin.available)
                        .build();

                adminRepository.save(masterAdmin);
                log.info("마스터 어드민 계정이 성공적으로 생성되었습니다: {}", masterId);

            } catch (Exception e) {
                log.error("마스터 어드민 계정 생성 중 오류가 발생했습니다: ", e);
            }
        };
    }
}
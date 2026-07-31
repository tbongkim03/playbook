package playbook.encore.back.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.admin.dao.AdminRepository;

import java.util.Optional;

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

            Optional<Admin> existing = adminRepository.findByIdAdmin(masterId);
            if (existing.isPresent()) {
                // 부트스트랩 계정은 전체관리자(seqCampus = null) 여야 함. 캠퍼스가 지정돼 있으면 해제.
                Admin admin = existing.get();
                if (admin.getSeqCampus() != null) {
                    admin.setSeqCampus(null);
                    adminRepository.save(admin);
                    log.info("마스터 어드민 계정을 전체관리자로 변경했습니다: {}", masterId);
                } else {
                    log.info("마스터 어드민 계정이 이미 전체관리자로 존재합니다: {}", masterId);
                }
                return;
            }

            try {
                String hashedPassword = BCrypt.hashpw(masterPassword, BCrypt.gensalt());

                Admin masterAdmin = Admin.builder()
                        .idAdmin(masterId)
                        .pwAdmin(hashedPassword)
                        .nameAdmin(masterName)
                        .dcAdmin(masterDiscord)
                        .seqCampus(null)   // 전체관리자(super-admin)로 생성
                        .agreeTermsAdmin(true)
                        .agreeInfoAdmin(true)
                        .agreeDiscordAlarmAdmin(false)
                        .statusAdmin(Admin.StatusTypeAdmin.available)
                        .build();

                adminRepository.save(masterAdmin);
                log.info("마스터 어드민 계정이 전체관리자로 생성되었습니다: {}", masterId);

            } catch (Exception e) {
                log.error("마스터 어드민 계정 생성 중 오류가 발생했습니다: ", e);
            }
        };
    }
}
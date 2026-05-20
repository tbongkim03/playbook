package playbook.encore.back.terms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import playbook.encore.back.terms.dao.TermsRepository;
import playbook.encore.back.terms.entity.Terms;

@Slf4j
@Component
@RequiredArgsConstructor
public class TermsDataInitializer implements CommandLineRunner {

    private final TermsRepository termsRepository;

    @Override
    public void run(String... args) {
        initTerms("SERVICE", "<p>플레이북 이용약관 내용입니다. 관리자 페이지 &gt; 약관 관리에서 수정해 주세요.</p>");
        initTerms("PRIVACY", "<p>개인정보처리방침 내용입니다. 관리자 페이지 &gt; 약관 관리에서 수정해 주세요.</p>");
        initTerms("DISCORD", "<p>디스코드 알림 동의서 내용입니다. 관리자 페이지 &gt; 약관 관리에서 수정해 주세요.</p>");
    }

    private void initTerms(String type, String defaultContent) {
        if (termsRepository.findByTermsType(type).isEmpty()) {
            log.info("[TermsDataInitializer] 약관 초기 데이터 생성 - type: {}", type);
            termsRepository.save(Terms.builder()
                    .termsType(type)
                    .content(defaultContent)
                    .build());
        }
    }
}

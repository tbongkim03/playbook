package playbook.encore.back.campus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.campus.entity.Campus;

/**
 * 신규 설치 시 최초 캠퍼스 1건을 시딩한다.
 *
 * <p><b>왜 필요한가</b><br>
 * 신규 설치 DB 는 {@code db/init/init.sql} 로 만들어지는데, 기본 캠퍼스를 넣던
 * {@code db/migration/001_add_campus.sql} 은 그 DB 에서 {@code Table 'tb_campus' already exists}
 * 로 <b>첫 구문에서 중단</b>된다({@code mysql < file} 은 첫 에러에서 멈춘다).
 * 그래서 001 뒤쪽의 캠퍼스 INSERT 가 실행되지 않아 {@code tb_campus} 가 0행으로 남는다.
 * 그 상태에서는 관리자 화면의 캠퍼스 선택이 비고, {@code tb_book.seq_campus} 가
 * NOT NULL + FK 라 <b>도서 등록 자체가 막힌다.</b>
 *
 * <p>설치 마법사가 입력받은 캠퍼스명을 {@code .env} 로 넘기면 여기서 1건을 만든다.
 * 하드코딩된 3개 캠퍼스(서초/지밸리/동작)를 넣지 않는 이유는, 캠퍼스 1개짜리 신규 설치가
 * 정상 시나리오이기 때문이다.
 *
 * <p><b>멱등성</b> — 캠퍼스가 <b>한 행이라도</b> 있으면 아무것도 하지 않는다.
 * Soft Delete 된 행까지 세므로({@link CampusRepository#countAllIncludingDeleted()}),
 * 운영 중 캠퍼스를 전부 삭제한 DB 에서 시더가 되살아나는 일도 없다.
 * 기존 캠퍼스가 있는 운영 DB 에서는 추가·수정이 절대 일어나지 않는다.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class InitialCampusInitializer {

    private final CampusRepository campusRepository;

    /** 설치 마법사가 채운다. 비어 있으면 시딩하지 않는다 (임의의 기본 캠퍼스명을 지어내지 않는다). */
    @Value("${INITIAL_CAMPUS_NAME:}")
    private String initialCampusName;

    @Value("${INITIAL_CAMPUS_LOCATION:}")
    private String initialCampusLocation;

    @Bean
    public ApplicationRunner initializeInitialCampus() {
        return args -> {
            try {
                if (initialCampusName == null || initialCampusName.isBlank()) {
                    log.info("[InitialCampus] INITIAL_CAMPUS_NAME 미설정 — 캠퍼스 시딩을 건너뜁니다.");
                    return;
                }

                long existing = campusRepository.countAllIncludingDeleted();
                if (existing > 0) {
                    log.info("[InitialCampus] 캠퍼스가 이미 {}건 있어 시딩하지 않습니다.", existing);
                    return;
                }

                Campus campus = Campus.builder()
                        .nameCampus(initialCampusName.trim())
                        .locationCampus(initialCampusLocation == null || initialCampusLocation.isBlank()
                                ? null : initialCampusLocation.trim())
                        .isActive(true)
                        .build();
                campusRepository.save(campus);
                log.info("[InitialCampus] 최초 캠퍼스를 생성했습니다: {}", campus.getNameCampus());

            } catch (Exception e) {
                // 시딩 실패가 기동을 막으면 안 된다. 캠퍼스는 관리자 화면에서 직접 등록할 수 있다.
                log.error("[InitialCampus] 최초 캠퍼스 시딩 중 오류가 발생했습니다. "
                        + "관리자 화면의 캠퍼스 관리에서 직접 등록하세요.", e);
            }
        };
    }
}

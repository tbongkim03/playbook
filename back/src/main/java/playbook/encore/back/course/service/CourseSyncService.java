package playbook.encore.back.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.course.dao.CourseDAO;
import playbook.encore.back.course.dao.CourseRepository;
import playbook.encore.back.course.entity.Course;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Work24 훈련과정 → DB 동기화.
 * 기존에 PageRegister.vue(getCourseList) 가 프론트에서 회원가입 페이지 로드마다 수행하던
 * insert/update/delete diff 로직을 백엔드로 이식. CourseSyncScheduler 가 매일 09:00 호출한다.
 * 캠퍼스(seqCampus) 매핑은 건드리지 않고 이름/기간만 동기화한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSyncService {

    private final Work24CourseClient work24CourseClient;
    private final CourseRepository courseRepository;
    private final CourseDAO courseDAO;

    @Transactional
    public SyncResult sync() {
        List<Work24CourseClient.Work24Course> apiCourses;
        try {
            apiCourses = work24CourseClient.fetchCourses();
        } catch (Exception e) {
            log.error("[CourseSync] Work24 조회 실패: {}", e.getMessage());
            throw new IllegalStateException("Work24 과정 조회 실패: " + e.getMessage(), e);
        }

        if (apiCourses.isEmpty()) {
            log.warn("[CourseSync] Work24 응답에 과정이 없어 동기화를 건너뜁니다(전체 삭제 방지).");
            return new SyncResult(0, 0, 0);
        }

        List<Course> dbCourses = courseDAO.selectAllCourse();
        Map<String, Course> dbByName = dbCourses.stream()
                .collect(Collectors.toMap(Course::getNameCourse, c -> c, (a, b) -> a));
        Map<String, Work24CourseClient.Work24Course> apiByName = apiCourses.stream()
                .collect(Collectors.toMap(Work24CourseClient.Work24Course::nameCourse, c -> c, (a, b) -> a));

        int inserted = 0, updated = 0, deleted = 0;

        // 추가: API 에만 있는 과정
        for (Work24CourseClient.Work24Course api : apiByName.values()) {
            if (!dbByName.containsKey(api.nameCourse())) {
                Course course = Course.builder()
                        .nameCourse(api.nameCourse())
                        .startDtCourse(api.startDtCourse())
                        .finishDtCourse(api.finishDtCourse())
                        .build();
                courseDAO.insertCourse(course);
                inserted++;
            }
        }

        // 삭제: DB 에만 있는 과정 (소프트 삭제)
        for (Course db : dbCourses) {
            if (!apiByName.containsKey(db.getNameCourse())) {
                courseDAO.deleteCourse(db);
                deleted++;
            }
        }

        // 수정: 양쪽에 있고 기간이 달라진 과정
        for (Work24CourseClient.Work24Course api : apiByName.values()) {
            Course db = dbByName.get(api.nameCourse());
            if (db != null) {
                boolean changed = !api.startDtCourse().equals(db.getStartDtCourse())
                        || !api.finishDtCourse().equals(db.getFinishDtCourse());
                if (changed) {
                    db.setStartDtCourse(api.startDtCourse());
                    db.setFinishDtCourse(api.finishDtCourse());
                    courseRepository.save(db);
                    updated++;
                }
            }
        }

        log.info("[CourseSync] 동기화 완료 - 추가 {}, 수정 {}, 삭제 {}", inserted, updated, deleted);
        return new SyncResult(inserted, updated, deleted);
    }

    public record SyncResult(int inserted, int updated, int deleted) {}
}

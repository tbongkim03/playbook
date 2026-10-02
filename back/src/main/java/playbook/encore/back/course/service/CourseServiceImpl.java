package playbook.encore.back.course.service;

import playbook.encore.back.common.excel.ExcelUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import playbook.encore.back.course.dao.CourseDAO;
import playbook.encore.back.course.dto.CourseRequestDto;
import playbook.encore.back.course.dto.CourseResponseDto;
import playbook.encore.back.course.entity.Course;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.course.dao.CourseRepository;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.course.service.CourseService;
import playbook.encore.back.auditlog.annotation.AuditAction;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService{

    private final CourseDAO courseDAO;
    private final CourseRepository courseRepository;
    private final CampusRepository campusRepository;

    @Autowired
    public CourseServiceImpl(CourseDAO courseDAO, CourseRepository courseRepository, CampusRepository campusRepository) {
        this.courseDAO = courseDAO;
        this.courseRepository = courseRepository;
        this.campusRepository = campusRepository;
    }

    private CourseResponseDto convertToDto(Course entity) {
        Integer seqCampus = entity.getSeqCampus() != null ? entity.getSeqCampus().getSeqCampus() : null;
        String campusName = entity.getSeqCampus() != null ? entity.getSeqCampus().getNameCampus() : null;
        return new CourseResponseDto(
            entity.getSeqCourse(),
            seqCampus,
            campusName,
            entity.getNameCourse(),
            entity.getStartDtCourse(),
            entity.getFinishDtCourse()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponseDto> getAllCourse(Integer campusId) {
        log.info("[CourseService] 전체 과정 조회 - campusId: {}", campusId);
        List<Course> courses;
        if (campusId != null) {
            courses = courseRepository.findAllWithCampusByCampusId(campusId);
        } else {
            courses = courseDAO.selectAllCourse();
        }
        List<CourseResponseDto> responseList = new ArrayList<>();

        for (Course course : courses) {
            CourseResponseDto courseResponseDto = convertToDto(course);
            responseList.add(courseResponseDto);
        }

        return responseList;
    }

    @Override
    @AuditAction(action = "COURSE_CREATE", targetType = "COURSE")
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseDto insertCourse(CourseRequestDto courseRequestDto) {
        log.info("[CourseService] 과정 등록 - name: {}", courseRequestDto.getNameCourse());
        Campus campus = null;
        if (courseRequestDto.getSeqCampus() != null) {
            campus = campusRepository.findById(courseRequestDto.getSeqCampus())
                .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다."));
        }
        
        Course course = Course.builder()
            .seqCampus(campus)
            .nameCourse(courseRequestDto.getNameCourse())
            .startDtCourse(courseRequestDto.getStartDtCourse())
            .finishDtCourse(courseRequestDto.getFinishDtCourse())
            .build();
        Course savedCourse = courseDAO.insertCourse(course);
        CourseResponseDto courseResponseDto = convertToDto(savedCourse);
        return courseResponseDto;
    }

    @Override
    @AuditAction(action = "COURSE_UPDATE", targetType = "COURSE")
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseDto changeCourse(Integer courseId, CourseRequestDto courseRequestDto) {
        log.info("[CourseService] 과정 수정 - courseId: {}", courseId);
        Course existingCourse = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("해당 과정은 존재하지 않습니다."));
        
        Campus campus = null;
        if (courseRequestDto.getSeqCampus() != null) {
            campus = campusRepository.findById(courseRequestDto.getSeqCampus())
                .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다."));
        }
        
        existingCourse.setSeqCampus(campus);
        existingCourse.setNameCourse(courseRequestDto.getNameCourse());
        existingCourse.setStartDtCourse(courseRequestDto.getStartDtCourse());
        existingCourse.setFinishDtCourse(courseRequestDto.getFinishDtCourse());

        Course changedCourse = courseRepository.save(existingCourse);

        CourseResponseDto courseResponseDto = convertToDto(changedCourse);

        return courseResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportExcel(Integer campusId) throws IOException {
        log.info("[CourseService] 과정 엑셀 내보내기 - campusId: {}", campusId);
        List<CourseResponseDto> courses = getAllCourse(campusId);

        List<String> headers = Arrays.asList("과정명", "캠퍼스", "시작일", "종료일");
        List<List<Object>> rows = courses.stream().map(c -> Arrays.<Object>asList(
                c.getNameCourse(),
                c.getCampusName() != null ? c.getCampusName() : "-",
                c.getStartDtCourse() != null ? c.getStartDtCourse().toString() : "-",
                c.getFinishDtCourse() != null ? c.getFinishDtCourse().toString() : "-"
        )).collect(Collectors.toList());

        Workbook wb = ExcelUtil.createWorkbook(headers, rows);
        return ExcelUtil.toResponse(wb, "과정목록").getBody();
    }

    @Override
    @AuditAction(action = "COURSE_DELETE", targetType = "COURSE")
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseById(Integer courseId) {
        log.info("[CourseService] 과정 삭제 - courseId: {}", courseId);
        Course selectedCourse = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("삭제에 실패하였습니다. 해당 과정은 존재하지 않습니다."));
        
        courseDAO.deleteCourse(selectedCourse);
    }
    
}

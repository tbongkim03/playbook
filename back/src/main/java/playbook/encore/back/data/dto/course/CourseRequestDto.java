package playbook.encore.back.data.dto.course;

import lombok.*;

import java.time.LocalDate;

@Data
public class CourseRequestDto {
    private Integer seqCourse;
    private String nameCourse;
    private LocalDate startDtCourse;
    private LocalDate finishDtCourse;

    public CourseRequestDto(Integer seqCourse, String nameCourse, LocalDate startDtCourse, LocalDate finishDtCourse) {
        this.seqCourse = seqCourse;
        this.nameCourse = nameCourse;
        this.startDtCourse = startDtCourse;
        this.finishDtCourse = finishDtCourse;
    }
}

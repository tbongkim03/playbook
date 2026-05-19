package playbook.encore.back.course.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDto {
    private Integer seqCourse;
    private Integer seqCampus;
    private String nameCourse;
    private LocalDate startDtCourse;
    private LocalDate finishDtCourse;
}

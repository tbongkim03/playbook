package playbook.encore.back.course.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDto {
    private Integer seqCourse;
    private Integer seqCampus;
    private String campusName;
    private String nameCourse;
    private LocalDate startDtCourse;
    private LocalDate finishDtCourse;
}

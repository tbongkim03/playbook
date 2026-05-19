package playbook.encore.back.data.dto.course;

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

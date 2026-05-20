package playbook.encore.back.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDto {
    private Integer seqCourse;
    @NotNull private Integer seqCampus;
    @NotBlank private String nameCourse;
    private LocalDate startDtCourse;
    private LocalDate finishDtCourse;
}

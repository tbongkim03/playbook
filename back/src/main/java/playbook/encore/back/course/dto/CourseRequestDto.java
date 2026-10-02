package playbook.encore.back.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDto {
    private Integer seqCourse;
    @NotNull private Integer seqCampus;
    @NotBlank @Size(max = 30, message = "과정명은 30자 이하여야 합니다.") private String nameCourse;
    private LocalDate startDtCourse;
    private LocalDate finishDtCourse;
}

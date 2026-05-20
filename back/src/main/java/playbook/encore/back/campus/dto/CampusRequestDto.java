package playbook.encore.back.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusRequestDto {
    private Integer seqCampus;
    @NotBlank private String nameCampus;
    @NotBlank private String locationCampus;
    @NotNull private Boolean isActive;
}

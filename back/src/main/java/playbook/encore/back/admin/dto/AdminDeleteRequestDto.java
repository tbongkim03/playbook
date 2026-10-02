package playbook.encore.back.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminDeleteRequestDto {
    @NotBlank private String idAdmin;
}

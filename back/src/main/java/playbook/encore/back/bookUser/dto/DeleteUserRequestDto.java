package playbook.encore.back.bookUser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteUserRequestDto {
    @NotBlank String idUser;
}

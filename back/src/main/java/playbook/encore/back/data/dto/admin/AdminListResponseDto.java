package playbook.encore.back.data.dto.admin;

import playbook.encore.back.data.entity.Admin;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
public class AdminListResponseDto {
    private List<Admin> content;
}

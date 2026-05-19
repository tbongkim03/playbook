package playbook.encore.back.admin.dto;

import playbook.encore.back.admin.entity.Admin;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
public class AdminListResponseDto {
    private List<AdminResponseDto> content;
}

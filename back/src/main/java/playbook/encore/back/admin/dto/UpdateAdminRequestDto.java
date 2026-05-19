package playbook.encore.back.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminRequestDto {
    private String idAdmin;           // 수정할 관리자 ID
    private String currentPassword;    // 현재 비밀번호 (검증용)
    private String newPassword;        // 새 비밀번호 (선택적, null 가능)
    private String newDiscord;         // 새 디스코드 ID (선택적, null 가능)
}


package playbook.encore.back.service;

import playbook.encore.back.data.dto.admin.LoginAdminRequestDto;
import playbook.encore.back.data.dto.admin.LoginAdminResponseDto;
import playbook.encore.back.data.dto.admin.RegisterAdminRequestDto;
import playbook.encore.back.data.dto.admin.RegisterAdminResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.entity.Admin;

public interface AdminService {
    RegisterAdminResponseDto createAdmin(Admin user, RegisterAdminRequestDto registerAdminRequestDto);

    RegisterIdValidateResponseDto checkUserId(String idAdmin);

    LoginAdminResponseDto loginServiceAdmin(LoginAdminRequestDto loginAdminRequestDto);

    boolean validatePassword(Admin user, String password);

    boolean updatePassword(Admin user, String newPassword);

    boolean updateDiscord(Admin user, String newDiscord);
}

package playbook.encore.back.admin.service;

import playbook.encore.back.admin.dto.*;
import playbook.encore.back.bookUser.dto.RegisterIdValidateResponseDto;
import playbook.encore.back.admin.entity.Admin;

public interface AdminService {
    RegisterAdminResponseDto createAdmin(Admin user, RegisterAdminRequestDto registerAdminRequestDto);

    RegisterIdValidateResponseDto checkUserId(String idAdmin);

    String loginServiceAdmin(LoginAdminRequestDto loginAdminRequestDto);

    boolean validatePassword(Admin user, String idAdmin, String password);

    boolean updateAdmin(Admin currentUser, UpdateAdminRequestDto updateRequest);

    AdminListResponseDto getAdminList(Integer campusId);

    boolean deleteAdmin(String idAdmin);

    byte[] exportExcel(Integer campusId) throws java.io.IOException;
}

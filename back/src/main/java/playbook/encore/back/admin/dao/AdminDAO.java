package playbook.encore.back.admin.dao;

import playbook.encore.back.admin.dto.AdminListResponseDto;
import playbook.encore.back.admin.dto.AdminResponseDto;
import playbook.encore.back.admin.entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminDAO {
    Optional<Admin> createAdmin(Admin user, Admin admin);

    Optional<Admin> searchBookUserResultExact(String idAdmin);

    Optional<Admin> loginIdPwCheck(String id, String pw);

    Optional<Admin> changeDiscord(Admin user, String newDiscord);

    Optional<Admin> changePw(Admin user, String hashedPassword);

    Optional<Admin> changeDiscordById(String targetIdAdmin, String newDiscord);

    Optional<Admin> changePwById(String targetIdAdmin, String hashedPassword);

    Optional<Admin> pwValidate(Admin user, String idAdmin, String password);

    AdminListResponseDto getAdminList(Integer campusId);

    Optional<Admin> updateStatus(Admin user, Admin.StatusTypeAdmin status);

    Optional<Admin> deleteAdmin(String idAdmin);
}

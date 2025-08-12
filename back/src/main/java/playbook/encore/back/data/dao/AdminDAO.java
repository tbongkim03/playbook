package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminDAO {
    Optional<Admin> createAdmin(Admin user, Admin admin);

    Optional<Admin> searchBookUserResultExact(String idAdmin);

    Optional<Admin> loginIdPwCheck(String id, String pw);

    Optional<Admin> changeDiscord(Admin user, String newDiscord);

    Optional<Admin> changePw(Admin user, String hashedPassword);

    Optional<Admin> pwValidate(Admin user, String password);

    List<Admin> getAdminList();

    Optional<Admin> updateStatus(Admin user, Admin.StatusTypeAdmin status);
}

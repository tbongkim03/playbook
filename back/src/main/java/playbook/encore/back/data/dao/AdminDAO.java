package playbook.encore.back.data.dao;

import playbook.encore.back.data.entity.Admin;

import java.util.Optional;

public interface AdminDAO {
    Admin createAdmin(Admin admin);

    Optional<Admin> searchBookUserResultExact(String idAdmin);

    Optional<Admin> loginIdPwCheck(String id, String pw);

    Optional<Admin> changeDiscord(Admin user, String newDiscord);

    Optional<Admin> changePw(Admin user, String hashedPassword);

    Optional<Admin> pwValidate(Admin user, String password);
}

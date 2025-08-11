package playbook.encore.back.data.dao.impl;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.AdminDAO;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.repository.AdminRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminDAOImpl implements AdminDAO {

    private final AdminRepository adminRepository;

    @Override
    public Optional<Admin> createAdmin(Admin user, Admin admin) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(user.getIdAdmin());
        if (optionalAdmin.isEmpty()) return Optional.empty();
        adminRepository.save(admin);
        return Optional.of(admin);
    }

    @Override
    public Optional<Admin> searchBookUserResultExact(String idAdmin) {
        return adminRepository.findByIdAdmin(idAdmin);
    }

    @Override
    public Optional<Admin> loginIdPwCheck(String id, String pw) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(id);
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin admin = optionalAdmin.get();
        if (!BCrypt.checkpw(pw, admin.getPwAdmin())) return Optional.empty();
        return Optional.of(admin);
    }

    @Override
    public Optional<Admin> changeDiscord(Admin user, String newDiscord) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(user.getIdAdmin());
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedAdmin = optionalAdmin.get();
        Admin updatedAdmin;
        selectedAdmin.setDcAdmin(newDiscord);
        updatedAdmin = adminRepository.save(selectedAdmin);
        return Optional.of(updatedAdmin);
    }

    @Override
    public Optional<Admin> changePw(Admin user, String hashedPassword) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(user.getIdAdmin());
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedAdmin = optionalAdmin.get();
        Admin updatedAdmin;
        selectedAdmin.setPwAdmin(hashedPassword);
        updatedAdmin = adminRepository.save(selectedAdmin);
        return Optional.of(updatedAdmin);
    }

    @Override
    public Optional<Admin> pwValidate(Admin user, String password) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(user.getIdAdmin());
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedUser = optionalAdmin.get();
        if (BCrypt.checkpw(password, selectedUser.getPwAdmin())) {
            return Optional.of(selectedUser);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Admin> getAdminList() {
        List<Admin> adminList = adminRepository.findAll();
        if (adminList.isEmpty()) {
            throw new IllegalArgumentException("등록된 어드민이 없습니다.");
        }
        return adminList;
    }
}

package playbook.encore.back.data.dao.impl;

import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;
import playbook.encore.back.data.dao.AdminDAO;
import playbook.encore.back.data.dto.admin.AdminListResponseDto;
import playbook.encore.back.data.dto.admin.AdminResponseDto;
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
    public Optional<Admin> changeDiscordById(String targetIdAdmin, String newDiscord) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(targetIdAdmin);
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedAdmin = optionalAdmin.get();
        selectedAdmin.setDcAdmin(newDiscord);
        Admin updatedAdmin = adminRepository.save(selectedAdmin);
        return Optional.of(updatedAdmin);
    }

    @Override
    public Optional<Admin> changePwById(String targetIdAdmin, String hashedPassword) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(targetIdAdmin);
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedAdmin = optionalAdmin.get();
        selectedAdmin.setPwAdmin(hashedPassword);
        Admin updatedAdmin = adminRepository.save(selectedAdmin);
        return Optional.of(updatedAdmin);
    }

    @Override
    public Optional<Admin> pwValidate(Admin user, String idAdmin, String password) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(user.getIdAdmin());
        Optional<Admin> optionalUser = adminRepository.findByIdAdmin(idAdmin);
        if (optionalAdmin.isEmpty()) return Optional.empty();
        if (optionalUser.isEmpty()) return Optional.empty();
        Admin selectedUser = optionalUser.get();
        if (BCrypt.checkpw(password, selectedUser.getPwAdmin())) {
            return Optional.of(selectedUser);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public AdminListResponseDto getAdminList(Integer campusId) {
        List<Admin> adminList;
        if (campusId != null) {
            adminList = adminRepository.findAllWithCampusByCampusId(campusId);
        } else {
            adminList = adminRepository.findAllWithCampus();
        }
        List<AdminResponseDto> content = adminList.stream()
                .map(admin -> new AdminResponseDto(
                        admin.getSeqAdmin(),
                        admin.getIdAdmin(),
                        admin.getNameAdmin(),
                        admin.getDcAdmin(),
                        admin.getStatusAdmin().name(),
                        admin.getCreatedAt(),
                        admin.getSeqCampus() != null ? admin.getSeqCampus().getSeqCampus() : null,
                        admin.getSeqCampus() != null ? admin.getSeqCampus().getNameCampus() : null  // campusName 추가
                ))
                .toList();
        return new AdminListResponseDto(content);
    }

    @Override
    public Optional<Admin> updateStatus(Admin user, Admin.StatusTypeAdmin status) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(user.getIdAdmin());
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedAdmin = optionalAdmin.get();
        Admin updatedAdmin;
        selectedAdmin.setStatusAdmin(status);
        updatedAdmin = adminRepository.save(selectedAdmin);
        return Optional.of(updatedAdmin);
    }

    @Override
    public Optional<Admin> deleteAdmin(String idAdmin) {
        Optional<Admin> optionalAdmin = adminRepository.findByIdAdmin(idAdmin);
        if (optionalAdmin.isEmpty()) return Optional.empty();
        Admin selectedAdmin = optionalAdmin.get();
        adminRepository.delete(selectedAdmin);
        return Optional.of(selectedAdmin);
    }
}

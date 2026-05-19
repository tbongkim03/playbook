package playbook.encore.back.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import playbook.encore.back.admin.dao.AdminDAO;
import playbook.encore.back.bookUser.dao.BookUserDAO;
import playbook.encore.back.admin.dto.*;
import playbook.encore.back.bookUser.dto.RegisterIdValidateResponseDto;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.jwt.jwtUtil;
import playbook.encore.back.admin.service.AdminService;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final AdminDAO adminDAO;
    private final BookUserDAO bookUserDAO;
    private final jwtUtil jwtUtil;
    private final CampusRepository campusRepository;

    @Autowired
    public AdminServiceImpl(AdminDAO adminDAO, BookUserDAO bookUserDAO, jwtUtil jwtUtil, CampusRepository campusRepository) {
        this.adminDAO = adminDAO;
        this.bookUserDAO = bookUserDAO;
        this.jwtUtil = jwtUtil;
        this.campusRepository = campusRepository;
    }

    @Override
    public RegisterAdminResponseDto createAdmin(Admin user, RegisterAdminRequestDto registerAdminRequestDto) {
        log.info("[AdminService] 관리자 등록 - id: {}", registerAdminRequestDto.getIdAdmin());
        String hashedPassword = BCrypt.hashpw(registerAdminRequestDto.getPwAdmin(), BCrypt.gensalt());

        // 캠퍼스 설정 (null이면 전체 관리자)
        Campus campus = null;
        if (registerAdminRequestDto.getSeqCampus() != null) {
            campus = campusRepository.findById(registerAdminRequestDto.getSeqCampus())
                    .orElseThrow(() -> new IllegalArgumentException("해당 캠퍼스는 존재하지 않습니다"));
        }

        Admin admin = Admin.builder()
                .seqCampus(campus)  // null이면 전체 관리자
                .idAdmin(registerAdminRequestDto.getIdAdmin())
                .pwAdmin(hashedPassword)
                .nameAdmin(registerAdminRequestDto.getNameAdmin())
                .dcAdmin(registerAdminRequestDto.getDcAdmin())
                .agreeTermsAdmin(registerAdminRequestDto.isAgreeTermsAdmin())
                .agreeInfoAdmin(registerAdminRequestDto.isAgreeInfoAdmin())
                .agreeDiscordAlarmAdmin(registerAdminRequestDto.isAgreeDiscordAlarmAdmin())
                .statusAdmin(Admin.StatusTypeAdmin.available)
                .createdAt(LocalDate.now())
                .build();

        boolean savedAdmin = adminDAO.createAdmin(user, admin).isPresent();
        if (!savedAdmin) {
            throw new IllegalArgumentException("존재하지 않는 어드민입니다.");
        }

        return new RegisterAdminResponseDto(200, HttpStatus.OK, "회원가입을 완료하였습니다", null);
    }

    @Override
    public RegisterIdValidateResponseDto checkUserId(String idAdmin) {
        log.info("[AdminService] 아이디 중복 확인 - id: {}", idAdmin);
        boolean flag = adminDAO.searchBookUserResultExact(idAdmin).isPresent() || bookUserDAO.searchBookUserResultExact(idAdmin).isPresent();
        return new RegisterIdValidateResponseDto(flag);
    }

    @Override
    public LoginAdminResponseDto loginServiceAdmin(LoginAdminRequestDto loginAdminRequestDto) {
        log.info("[AdminService] 관리자 로그인 시도 - id: {}", loginAdminRequestDto.getIdAdmin());
        String id = loginAdminRequestDto.getIdAdmin();
        String pw = loginAdminRequestDto.getPwAdmin();
        boolean isLoginSuccess = adminDAO.loginIdPwCheck(id, pw).isPresent();
        if (!isLoginSuccess) {
            throw new IllegalArgumentException("로그인에 실패하였습니다. 아이디와 비밀번호를 확인해 주세요.");
        }
        return new LoginAdminResponseDto(jwtUtil.generateToken(id, "admin"));
    }

    @Override
    public boolean validatePassword(Admin user, String idAdmin, String password) {
        log.info("[AdminService] 비밀번호 검증 - id: {}", idAdmin);
        boolean isPasswordExist = adminDAO.pwValidate(user, idAdmin, password).isPresent();
        if (!isPasswordExist) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다. 다시 입력해 주세요");
        }
        return true;
    }

    @Override
    public boolean updatePassword(Admin user, String newPassword) {
        log.info("[AdminService] 비밀번호 변경 - id: {}", user.getIdAdmin());
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        boolean isPasswordChanged = adminDAO.changePw(user, hashedPassword).isPresent();
        if (!isPasswordChanged) {
            throw new IllegalArgumentException("비밀번호 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }

    @Override
    public boolean updateDiscord(Admin user, String newDiscord) {
        log.info("[AdminService] 디스코드 변경 - id: {}", user.getIdAdmin());
        boolean isDiscordChanged = adminDAO.changeDiscord(user, newDiscord).isPresent();
        if (!isDiscordChanged) {
            throw new IllegalArgumentException("과정 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }

    @Override
    public boolean updateAdmin(Admin currentUser, UpdateAdminRequestDto updateRequest) {
        log.info("[AdminService] 관리자 정보 수정 - id: {}", updateRequest.getIdAdmin());
        // 전체 관리자 체크 (seqCampus가 null이면 전체 관리자)
        boolean isSuperAdmin = currentUser.getSeqCampus() == null;
        
        // 권한 체크: 전체 관리자가 아니면 본인 계정만 수정 가능
        if (!isSuperAdmin && !currentUser.getIdAdmin().equals(updateRequest.getIdAdmin())) {
            throw new IllegalArgumentException("본인 계정만 수정할 수 있습니다.");
        }

        // 수정 대상 관리자 존재 확인
        Optional<Admin> targetAdminOpt = adminDAO.searchBookUserResultExact(updateRequest.getIdAdmin());
        if (targetAdminOpt.isEmpty()) {
            throw new IllegalArgumentException("수정할 관리자를 찾을 수 없습니다.");
        }
        Admin targetAdmin = targetAdminOpt.get();

        // 현재 비밀번호 검증 (로그인 중인 사용자의 비밀번호로 검증)
        boolean isPasswordValid = adminDAO.pwValidate(currentUser, currentUser.getIdAdmin(), updateRequest.getCurrentPassword()).isPresent();
        if (!isPasswordValid) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다. 다시 입력해 주세요");
        }

        // 디스코드 ID 업데이트 (값이 제공된 경우)
        if (updateRequest.getNewDiscord() != null && !updateRequest.getNewDiscord().trim().isEmpty()) {
            boolean isDiscordChanged = adminDAO.changeDiscordById(updateRequest.getIdAdmin(), updateRequest.getNewDiscord().trim()).isPresent();
            if (!isDiscordChanged) {
                throw new IllegalArgumentException("디스코드 ID 변경에 실패하였습니다. 다시 시도해 주세요");
            }
        }

        // 비밀번호 업데이트 (값이 제공된 경우)
        if (updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().trim().isEmpty()) {
            String hashedPassword = BCrypt.hashpw(updateRequest.getNewPassword().trim(), BCrypt.gensalt());
            boolean isPasswordChanged = adminDAO.changePwById(updateRequest.getIdAdmin(), hashedPassword).isPresent();
            if (!isPasswordChanged) {
                throw new IllegalArgumentException("비밀번호 변경에 실패하였습니다. 다시 시도해 주세요");
            }
        }

        // 디스코드 ID와 비밀번호 둘 다 변경하지 않은 경우
        boolean hasDiscordUpdate = updateRequest.getNewDiscord() != null && !updateRequest.getNewDiscord().trim().isEmpty();
        boolean hasPasswordUpdate = updateRequest.getNewPassword() != null && !updateRequest.getNewPassword().trim().isEmpty();
        if (!hasDiscordUpdate && !hasPasswordUpdate) {
            throw new IllegalArgumentException("디스코드 ID 또는 비밀번호 중 하나는 변경해야 합니다.");
        }

        return true;
    }

    @Override
    public AdminListResponseDto getAdminList(Integer campusId) {
        log.info("[AdminService] 관리자 목록 조회 - campusId: {}", campusId);
        AdminListResponseDto adminListResponseDto = adminDAO.getAdminList(campusId);
        if (adminListResponseDto.getContent().isEmpty()) {
            throw new IllegalArgumentException("등록된 어드민이 없습니다.");
        }
        return adminListResponseDto;
    }

    @Override
    public boolean deleteAdmin(String idAdmin) {
        log.info("[AdminService] 관리자 삭제 - id: {}", idAdmin);
        boolean isAdminDeleted = adminDAO.deleteAdmin(idAdmin).isPresent();
        if (!isAdminDeleted) {
            throw new IllegalArgumentException("어드민 삭제에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }
}

package playbook.encore.back.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import playbook.encore.back.data.dao.AdminDAO;
import playbook.encore.back.data.dao.BookUserDAO;
import playbook.encore.back.data.dto.admin.LoginAdminRequestDto;
import playbook.encore.back.data.dto.admin.LoginAdminResponseDto;
import playbook.encore.back.data.dto.admin.RegisterAdminRequestDto;
import playbook.encore.back.data.dto.admin.RegisterAdminResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.jwt.jwtUtil;
import playbook.encore.back.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminDAO adminDAO;
    private final BookUserDAO bookUserDAO;
    private final jwtUtil jwtUtil;

    @Autowired
    public AdminServiceImpl(AdminDAO adminDAO, BookUserDAO bookUserDAO, jwtUtil jwtUtil) {
        this.adminDAO = adminDAO;
        this.bookUserDAO = bookUserDAO;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RegisterAdminResponseDto createAdmin(RegisterAdminRequestDto registerAdminRequestDto) {
        String hashedPassword = BCrypt.hashpw(registerAdminRequestDto.getPwAdmin(), BCrypt.gensalt());

        Admin admin = Admin.builder()
                .idAdmin(registerAdminRequestDto.getIdAdmin())
                .pwAdmin(registerAdminRequestDto.getPwAdmin())
                .nameAdmin(registerAdminRequestDto.getNameAdmin())
                .dcAdmin(registerAdminRequestDto.getDcAdmin())
                .agreeTermsAdmin(registerAdminRequestDto.isAgreeTermsAdmin())
                .agreeInfoAdmin(registerAdminRequestDto.isAgreeInfoAdmin())
                .agreeDiscordAlarmAdmin(registerAdminRequestDto.isAgreeDiscordAlarmAdmin())
                .statusAdmin(Admin.StatusTypeAdmin.available)
                .build();

        Admin savedAdmin = adminDAO.createAdmin(admin);

        return new RegisterAdminResponseDto(200, HttpStatus.OK, "회원가입을 완료하였습니다", null);
    }

    @Override
    public RegisterIdValidateResponseDto checkUserId(String idAdmin) {
        boolean flag = adminDAO.searchBookUserResultExact(idAdmin).isPresent() || bookUserDAO.searchBookUserResultExact(idAdmin).isPresent();
        return new RegisterIdValidateResponseDto(flag);
    }

    @Override
    public LoginAdminResponseDto loginServiceAdmin(LoginAdminRequestDto loginAdminRequestDto) {
        String id = loginAdminRequestDto.getIdAdmin();
        String pw = loginAdminRequestDto.getPwAdmin();
        boolean isLoginSuccess = adminDAO.loginIdPwCheck(id, pw).isPresent();
        if (!isLoginSuccess) {
            throw new IllegalArgumentException("로그인에 실패하였습니다. 아이디와 비밀번호를 확인해 주세요.");
        }
        return new LoginAdminResponseDto(jwtUtil.generateToken(id, "admin"));
    }

    @Override
    public boolean validatePassword(Admin user, String password) {
        boolean isPasswordExist = adminDAO.pwValidate(user, password).isPresent();
        if (!isPasswordExist) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다. 다시 입력해 주세요");
        }
        return true;
    }

    @Override
    public boolean updatePassword(Admin user, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        boolean isPasswordChanged = adminDAO.changePw(user, hashedPassword).isPresent();
        if (!isPasswordChanged) {
            throw new IllegalArgumentException("비밀번호 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }

    @Override
    public boolean updateDiscord(Admin user, String newDiscord) {
        boolean isDiscordChanged = adminDAO.changeDiscord(user, newDiscord).isPresent();
        if (!isDiscordChanged) {
            throw new IllegalArgumentException("과정 변경에 실패하였습니다. 다시 시도해 주세요");
        }
        return true;
    }
}

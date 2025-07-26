package playbook.encore.back.service;

import playbook.encore.back.data.dto.bookUser.LoginUserRequestDto;
import playbook.encore.back.data.dto.bookUser.LoginUserResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserRequestDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserResponseDto;
import playbook.encore.back.data.entity.BookUser;

public interface BookUserService {

    RegisterUserResponseDto createUser(RegisterUserRequestDto registerUserRequestDto);

    RegisterIdValidateResponseDto checkUserId(String idUser);

    LoginUserResponseDto loginServiceUser(LoginUserRequestDto loginUserRequestDto);

    boolean validatePassword(BookUser user, String password);

    boolean updatePassword(BookUser user, String newPassword);

    boolean updateDiscord(BookUser user, String newDiscord);

    boolean updateCourse(BookUser user, Integer newSeqCourse);

}

package playbook.encore.back.bookUser.service;

import playbook.encore.back.bookUser.dto.LoginUserRequestDto;
import playbook.encore.back.bookUser.dto.RegisterIdValidateResponseDto;
import playbook.encore.back.bookUser.dto.RegisterUserRequestDto;
import playbook.encore.back.bookUser.dto.RegisterUserResponseDto;
import playbook.encore.back.bookUser.dto.UpdateUserRequestDto;
import playbook.encore.back.bookUser.entity.BookUser;

import java.util.List;

public interface BookUserService {

    RegisterUserResponseDto createUser(RegisterUserRequestDto registerUserRequestDto);

    RegisterIdValidateResponseDto checkUserId(String idUser);

    BookUser loginServiceUser(LoginUserRequestDto loginUserRequestDto);

    boolean validatePassword(BookUser user, String password);

    boolean updatePassword(BookUser user, String newPassword);

    boolean updateUser(BookUser user, UpdateUserRequestDto dto);

    boolean resetUserPassword(String idUser, String newPassword);

//    boolean updateDiscord(BookUser user, String newDiscord);

//    boolean updateCourse(BookUser user, Integer newSeqCourse);

    List<Object[]> getBookUserList(Integer campusId);

    boolean deleteUserByAdmin(String idUser);

    boolean deleteUserBySelf(BookUser user);

    byte[] exportExcel(Integer campusId) throws java.io.IOException;
}

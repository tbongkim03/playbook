package playbook.encore.back.service;

import playbook.encore.back.data.dto.bookUser.RegisterIdValidateResponseDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserRequestDto;
import playbook.encore.back.data.dto.bookUser.RegisterUserResponseDto;

public interface BookUserService {

    RegisterUserResponseDto createUser(RegisterUserRequestDto registerUserRequestDto);

    RegisterIdValidateResponseDto checkUserId(String idUser);
    
}

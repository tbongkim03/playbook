package playbook.encore.back.data.dto.bookUser;

import lombok.*;

@Data
public class LoginUserDataResponseDto {
    private int seqCourse;
    private String idUser;
    private String nameUser;
    private String dcUser;

    public LoginUserDataResponseDto(int seqCourse, String idUser, String nameUser, String dcUser) {
        this.seqCourse = seqCourse;
        this.idUser = idUser;
        this.nameUser = nameUser;
        this.dcUser = dcUser;
    }
}

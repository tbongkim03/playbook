package playbook.encore.back.bookUser.dto;

import lombok.*;
import playbook.encore.back.bookUser.entity.BookUser;

@Data
public class LoginUserDataResponseDto {
    private Integer seqCourse;
    private Integer seqCampus;
    private String campusName;
    private String idUser;
    private String nameUser;
    private String dcUser;
    private String statusUser;

    public LoginUserDataResponseDto(Integer seqCourse, Integer seqCampus, String campusName, String idUser, String nameUser, String dcUser, String statusUser) {
        this.seqCourse = seqCourse;
        this.seqCampus = seqCampus;
        this.campusName = campusName;
        this.idUser = idUser;
        this.nameUser = nameUser;
        this.dcUser = dcUser;
        this.statusUser = statusUser;
    }
}

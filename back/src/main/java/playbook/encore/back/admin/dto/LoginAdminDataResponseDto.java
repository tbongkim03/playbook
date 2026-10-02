package playbook.encore.back.admin.dto;

import lombok.*;
import playbook.encore.back.campus.entity.Campus;

@Data
public class LoginAdminDataResponseDto {
    private CampusDto seqCampus;  // Campus 정보 (null이면 전체 관리자)
    private String idAdmin;
    private String nameAdmin;
    private String dcAdmin;

    @Data
    @AllArgsConstructor
    public static class CampusDto {
        private Integer seqCampus;
        private String nameCampus;
    }

    public LoginAdminDataResponseDto(Campus campus, String idAdmin, String nameAdmin, String dcAdmin) {
        this.seqCampus = campus != null ?
            new CampusDto(campus.getSeqCampus(), campus.getNameCampus()) : null;
        this.idAdmin = idAdmin;
        this.nameAdmin = nameAdmin;
        this.dcAdmin = dcAdmin;
    }
}

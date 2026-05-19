package playbook.encore.back.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminResponseDto {
    private Integer seqAdmin;
    private String idAdmin;
    private String nameAdmin;
    private String dcAdmin;
    private String statusAdmin;
    private LocalDate createdAt;
    private Integer campusId;
    private String campusName;  // 캠퍼스 이름 (null이면 "전체")

    public AdminResponseDto(Integer seqAdmin, String idAdmin, String nameAdmin, String dcAdmin, String statusAdmin, LocalDate createdAt, Integer campusId, String campusName) {
        this.seqAdmin = seqAdmin;
        this.idAdmin = idAdmin;
        this.nameAdmin = nameAdmin;
        this.dcAdmin = dcAdmin;
        this.statusAdmin = statusAdmin;
        this.createdAt = createdAt;
        this.campusId = campusId;
        this.campusName = campusName != null ? campusName : "전체";  // null이면 "전체"로 표시
    }
}

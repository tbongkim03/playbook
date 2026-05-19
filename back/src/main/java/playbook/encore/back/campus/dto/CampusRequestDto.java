package playbook.encore.back.campus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusRequestDto {
    private Integer seqCampus;
    private String nameCampus;
    private String locationCampus;
    private Boolean isActive;
}


package playbook.encore.back.history.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class UserReadingRankDto {
    private String userName;
    private Long bookCount;
}

package playbook.encore.back.data.dto.history;

import lombok.*;

@Data
@AllArgsConstructor
public class UserReadingRankDto {
    private String userName;
    private Long bookCount;
}

package playbook.encore.back.data.dto.history;

import lombok.*;

@Data
@AllArgsConstructor
public class PopularLabelDto {
    private String korSortLabel;
    private Long rentalCount;
}

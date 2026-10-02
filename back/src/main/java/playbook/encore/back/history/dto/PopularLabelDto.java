package playbook.encore.back.history.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class PopularLabelDto {
    private String korSortLabel;
    private Long rentalCount;
}

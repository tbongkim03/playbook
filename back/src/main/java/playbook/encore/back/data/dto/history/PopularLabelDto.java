package playbook.encore.back.data.dto.history;

import lombok.*;

@Data
@AllArgsConstructor
public class PopularLabelDto {
    private String nameSortFirst;
    private Long rentalCount;
}

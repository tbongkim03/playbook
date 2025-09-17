package playbook.encore.back.data.dto.history;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
public class HistoryBookResponseDto {
    private RentalSummaryDto summary;
    private List<RentalHistoryDto> history;
}

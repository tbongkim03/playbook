package playbook.encore.back.history.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
public class HistoryBookResponseDto {
    private RentalSummaryDto summary;
    private List<RentalHistoryDto> history;
}

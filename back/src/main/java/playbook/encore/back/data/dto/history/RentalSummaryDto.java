package playbook.encore.back.data.dto.history;

import lombok.*;

@Data
@AllArgsConstructor
public class RentalSummaryDto {
    private int totalBorrowed;
    private int totalReturned;
    private int currentlyBorrowed;
    private int overdueCount;
}

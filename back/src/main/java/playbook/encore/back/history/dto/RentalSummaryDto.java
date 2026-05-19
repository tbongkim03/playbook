package playbook.encore.back.history.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class RentalSummaryDto {
    private int totalBorrowed;
    private int totalReturned;
    private int currentlyBorrowed;
    private int overdueCount;
}

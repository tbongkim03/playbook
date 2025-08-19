package playbook.encore.back.data.dto.favor;

import lombok.*;

@Data
@AllArgsConstructor
public class FavorResponseDto {
    private int seqFavor;
    private int seqBook;
    private String titleBook;
    private String authorBook;
}

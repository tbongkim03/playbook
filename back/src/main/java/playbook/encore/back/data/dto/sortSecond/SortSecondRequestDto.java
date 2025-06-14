package playbook.encore.back.data.dto.sortSecond;

import lombok.*;
import playbook.encore.back.data.entity.SortFirst;

@Data
public class SortSecondRequestDto {

    private SortFirst seqSortFirst;
    private String korSortSecond;
    private String nameSortSecond;

    public SortSecondRequestDto(SortFirst seqSortFirst, String korSortSecond, String nameSortSecond) {
        this.seqSortFirst = seqSortFirst;
        this.korSortSecond = korSortSecond;
        this.nameSortSecond = nameSortSecond;
    }
}

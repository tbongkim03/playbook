package playbook.encore.back.data.dto.sortSecond;

import lombok.*;
import playbook.encore.back.data.entity.SortFirst;

@Data
public class SortSecondResponseDto {

    private Integer seqSortSecond;
    private Integer seqSortFirst;
    private String korSortSecond;
    private String nameSortSecond;

    public SortSecondResponseDto(Integer seqSortSecond, Integer seqSortFirst, String korSortSecond, String nameSortSecond) {
        this.seqSortSecond = seqSortSecond;
        this.seqSortFirst = seqSortFirst;
        this.korSortSecond = korSortSecond;
        this.nameSortSecond = nameSortSecond;
    }
}

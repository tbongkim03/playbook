package playbook.encore.back.sort.dto;

import lombok.*;
import playbook.encore.back.sort.entity.SortFirst;

@Data
public class SortSecondRequestDto {

    private Integer seqSortFirst;
    private String korSortSecond;
    private String nameSortSecond;

    public SortSecondRequestDto(Integer seqSortFirst, String korSortSecond, String nameSortSecond) {
        this.seqSortFirst = seqSortFirst;
        this.korSortSecond = korSortSecond;
        this.nameSortSecond = nameSortSecond;
    }
}

package playbook.encore.back.sort.dto;

import lombok.*;

@Data
public class SortFirstResponseDto {

    private Integer seqSortFirst;
    private String korSortFirst;
    private String nameSortFirst;

    public SortFirstResponseDto(Integer seqSortFirst, String korSortFirst, String nameSortFirst) {
        this.seqSortFirst = seqSortFirst;
        this.korSortFirst = korSortFirst;
        this.nameSortFirst = nameSortFirst;
    }
}

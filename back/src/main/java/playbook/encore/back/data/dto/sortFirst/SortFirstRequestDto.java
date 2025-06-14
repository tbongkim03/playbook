package playbook.encore.back.data.dto.sortFirst;

import lombok.*;

@Data
public class SortFirstRequestDto {

    private Integer seqSortFirst;
    private String korSortFirst;
    private String nameSortFirst;

    public SortFirstRequestDto(Integer seqSortFirst, String korSortFirst, String nameSortFirst) {
        this.seqSortFirst = seqSortFirst;
        this.korSortFirst = korSortFirst;
        this.nameSortFirst = nameSortFirst;
    }
}

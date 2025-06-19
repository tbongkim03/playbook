package playbook.encore.back.data.dto.sortFirst;

import lombok.*;

@Data
public class SortFirstRequestDto {

    private String korSortFirst;
    private String nameSortFirst;

    public SortFirstRequestDto(String korSortFirst, String nameSortFirst) {
        this.korSortFirst = korSortFirst;
        this.nameSortFirst = nameSortFirst;
    }
}

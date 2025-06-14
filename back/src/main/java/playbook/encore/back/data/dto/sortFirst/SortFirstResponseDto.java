package playbook.encore.back.data.dto.sortFirst;

import lombok.*;

@Data
public class SortFirstResponseDto {
    private String korSortFirst;
    private String nameSortFirst;

    public SortFirstResponseDto(String korSortFirst, String nameSortFirst) {
        this.korSortFirst = korSortFirst;
        this.nameSortFirst = nameSortFirst;
    }
}

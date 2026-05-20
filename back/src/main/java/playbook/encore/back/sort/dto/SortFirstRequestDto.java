package playbook.encore.back.sort.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class SortFirstRequestDto {

    @NotBlank private String korSortFirst;
    @NotBlank private String nameSortFirst;

    public SortFirstRequestDto(String korSortFirst, String nameSortFirst) {
        this.korSortFirst = korSortFirst;
        this.nameSortFirst = nameSortFirst;
    }
}

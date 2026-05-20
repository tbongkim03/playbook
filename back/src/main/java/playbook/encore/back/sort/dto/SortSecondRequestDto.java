package playbook.encore.back.sort.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
public class SortSecondRequestDto {

    @NotNull private Integer seqSortFirst;
    @NotBlank private String korSortSecond;
    @NotBlank private String nameSortSecond;

    public SortSecondRequestDto(Integer seqSortFirst, String korSortSecond, String nameSortSecond) {
        this.seqSortFirst = seqSortFirst;
        this.korSortSecond = korSortSecond;
        this.nameSortSecond = nameSortSecond;
    }
}

package playbook.encore.back.terms.service;

import playbook.encore.back.terms.dto.TermsRequestDto;
import playbook.encore.back.terms.dto.TermsResponseDto;

public interface TermsService {
    TermsResponseDto getTerms(String type);
    TermsResponseDto updateTerms(String type, TermsRequestDto dto);
}

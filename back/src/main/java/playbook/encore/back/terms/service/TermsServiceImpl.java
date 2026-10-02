package playbook.encore.back.terms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import playbook.encore.back.terms.dao.TermsRepository;
import playbook.encore.back.terms.dto.TermsRequestDto;
import playbook.encore.back.terms.dto.TermsResponseDto;
import playbook.encore.back.terms.entity.Terms;

@Slf4j
@Service
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {

    private final TermsRepository termsRepository;

    @Override
    public TermsResponseDto getTerms(String type) {
        log.info("[TermsService] 약관 조회 - type: {}", type);
        Terms terms = termsRepository.findByTermsType(type.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관 유형: " + type));
        return convertToDto(terms);
    }

    @Override
    @Transactional
    public TermsResponseDto updateTerms(String type, TermsRequestDto dto) {
        log.info("[TermsService] 약관 수정 - type: {}", type);
        Terms terms = termsRepository.findByTermsType(type.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 약관 유형: " + type));
        terms.setContent(dto.getContent());
        return convertToDto(termsRepository.save(terms));
    }

    private TermsResponseDto convertToDto(Terms terms) {
        return new TermsResponseDto(
                terms.getSeqTerms(),
                terms.getTermsType(),
                terms.getContent(),
                terms.getUpdatedAt()
        );
    }
}

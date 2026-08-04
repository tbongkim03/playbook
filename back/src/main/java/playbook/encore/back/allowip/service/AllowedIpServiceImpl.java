package playbook.encore.back.allowip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import playbook.encore.back.allowip.dao.AllowedIpRepository;
import playbook.encore.back.allowip.dto.AllowedIpDeleteResultDto;
import playbook.encore.back.allowip.dto.AllowedIpRequestDto;
import playbook.encore.back.allowip.dto.AllowedIpResponseDto;
import playbook.encore.back.allowip.dto.MyIpResponseDto;
import playbook.encore.back.allowip.entity.AllowedIp;
import playbook.encore.back.allowip.exception.AllowedIpException;
import playbook.encore.back.allowip.util.IpRangeMatcher;
import playbook.encore.back.auditlog.annotation.AuditAction;
import playbook.encore.back.campus.dao.CampusRepository;
import playbook.encore.back.campus.entity.Campus;
import playbook.encore.back.common.response.ResponseCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllowedIpServiceImpl implements AllowedIpService {

    private static final String MSG_SYSTEM_RULE =
            "설치 마법사가 등록한 기본 규칙은 삭제할 수 없습니다. 비활성만 가능합니다.";
    private static final String MSG_SYSTEM_RULE_IP_CHANGE =
            "설치 마법사가 등록한 기본 규칙은 IP 값을 변경할 수 없습니다. 비활성만 가능합니다.";
    private static final String MSG_SELF_BLOCK_UPDATE =
            "현재 접속 중인 IP가 차단됩니다. 먼저 다른 허용 규칙을 등록하세요.";
    private static final String MSG_SELF_BLOCK_DELETE =
            "현재 접속 중인 IP를 허용하는 마지막 규칙입니다.";

    private final AllowedIpRepository allowedIpRepository;
    private final CampusRepository campusRepository;
    private final IpAllowlistEvaluator evaluator;

    // ===== 1. 목록 =====
    @Override
    @Transactional(readOnly = true)
    public List<AllowedIpResponseDto> getAllowedIps(AllowedIpActor actor, Integer filterSeqCampus,
                                                    boolean includeInactive, String requesterIp) {
        List<AllowedIp> all = allowedIpRepository.findAllWithCampus();
        List<AllowedIp> visible = new ArrayList<>();
        for (AllowedIp a : all) {
            Integer campusId = campusIdOf(a);
            // 캠퍼스 관리자는 자기 캠퍼스 + 전역 규칙만 조회 가능
            if (!actor.superAdmin() && campusId != null && !campusId.equals(actor.campusId())) {
                continue;
            }
            // seqCampus 필터는 전체관리자 전용
            if (actor.superAdmin() && filterSeqCampus != null && !filterSeqCampus.equals(campusId)) {
                continue;
            }
            if (!includeInactive && !a.isActive()) {
                continue;
            }
            visible.add(a);
        }

        // 정렬: seqCampus NULL 우선 → seqCampus 오름차순 → seqAllowedIp 오름차순
        visible.sort(Comparator
                .<AllowedIp, Integer>comparing(this::campusIdOf, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(AllowedIp::getSeqAllowedIp, Comparator.nullsLast(Comparator.naturalOrder())));

        List<AllowedIpResponseDto> result = new ArrayList<>(visible.size());
        for (AllowedIp a : visible) {
            result.add(toDto(a, requesterIp));
        }
        return result;
    }

    // ===== 2. 등록 =====
    @Override
    @Transactional
    @AuditAction(action = "ALLOWED_IP_CREATE", targetType = "ALLOWED_IP")
    public AllowedIpResponseDto createAllowedIp(AllowedIpActor actor, AllowedIpRequestDto dto, String requesterIp) {
        String ipValue = normalizeIpValue(dto.getIpValue());
        Integer targetCampusId = resolveTargetCampus(actor, dto.getSeqCampus());
        assertDuplicate(ipValue, targetCampusId, null);

        Campus campus = loadCampus(targetCampusId);
        boolean active = dto.getIsActive() == null || dto.getIsActive();

        // 같은 값으로 소프트 삭제된 규칙이 있으면 INSERT 대신 되살린다.
        // uk_allowed_ip_value_campus(ip_value, seq_campus) 가 삭제분까지 잡아 유니크 위반이 나기 때문이다.
        AllowedIp entity = findSoftDeleted(ipValue, targetCampusId)
                .orElseGet(() -> AllowedIp.builder().isSystem(false).build());
        entity.setUseYn("Y");
        entity.setIpValue(ipValue);
        entity.setIpType(IpRangeMatcher.resolveType(ipValue));
        entity.setSeqCampus(campus);
        entity.setDescription(dto.getDescription());
        entity.setActive(active);

        AllowedIp saved;
        try {
            saved = allowedIpRepository.save(entity);
        } catch (Exception e) {
            log.error("[AllowedIp] 등록 실패 - ipValue={}", ipValue, e);
            throw new AllowedIpException(ResponseCode.FAIL_INSERT);
        }
        reloadCacheAfterCommit();
        return toDto(saved, requesterIp);
    }

    // ===== 3. 수정 =====
    @Override
    @Transactional
    @AuditAction(action = "ALLOWED_IP_UPDATE", targetType = "ALLOWED_IP")
    public AllowedIpResponseDto updateAllowedIp(AllowedIpActor actor, Integer seqAllowedIp,
                                                AllowedIpRequestDto dto, String requesterIp) {
        AllowedIp entity = loadOrThrow(seqAllowedIp);
        assertScopeWritable(actor, campusIdOf(entity));

        String ipValue = normalizeIpValue(dto.getIpValue());
        Integer targetCampusId = resolveTargetCampus(actor, dto.getSeqCampus());
        assertScopeWritable(actor, targetCampusId);
        assertDuplicate(ipValue, targetCampusId, seqAllowedIp);

        // 시스템 규칙의 ipValue 변경은 "삭제 금지"를 우회하는 것과 같다 (보안감사 S-8).
        // 설명·활성 변경은 계속 허용한다 — 비활성화는 계약서가 명시적으로 허용하는 동작이다.
        if (entity.isSystem() && !ipValue.equals(entity.getIpValue())) {
            throw new AllowedIpException(ResponseCode.NOT_AUTHORIZED, MSG_SYSTEM_RULE_IP_CHANGE);
        }

        boolean active = dto.getIsActive() == null || dto.getIsActive();
        assertNotSelfBlocked(requesterIp, seqAllowedIp, ipValue, active, false,
                ResponseCode.FAIL_UPDATE, MSG_SELF_BLOCK_UPDATE);

        entity.setIpValue(ipValue);
        entity.setIpType(IpRangeMatcher.resolveType(ipValue));
        entity.setSeqCampus(loadCampus(targetCampusId));
        entity.setDescription(dto.getDescription());
        entity.setActive(active);

        AllowedIp saved;
        try {
            saved = allowedIpRepository.save(entity);
        } catch (Exception e) {
            log.error("[AllowedIp] 수정 실패 - seq={}", seqAllowedIp, e);
            throw new AllowedIpException(ResponseCode.FAIL_UPDATE);
        }
        reloadCacheAfterCommit();
        return toDto(saved, requesterIp);
    }

    // ===== 4. 삭제 (Soft Delete) =====
    @Override
    @Transactional
    @AuditAction(action = "ALLOWED_IP_DELETE", targetType = "ALLOWED_IP")
    public AllowedIpDeleteResultDto deleteAllowedIp(AllowedIpActor actor, Integer seqAllowedIp, String requesterIp) {
        AllowedIp entity = loadOrThrow(seqAllowedIp);
        // 권한 검사를 isSystem 검사보다 먼저 한다 (보안감사 S-9).
        // 순서가 반대면 타 캠퍼스 규칙의 isSystem 여부가 에러 메시지 차이로 새어나간다.
        assertScopeWritable(actor, campusIdOf(entity));
        if (entity.isSystem()) {
            throw new AllowedIpException(ResponseCode.NOT_AUTHORIZED, MSG_SYSTEM_RULE);
        }
        assertNotSelfBlocked(requesterIp, seqAllowedIp, null, false, true,
                ResponseCode.FAIL_DELETE, MSG_SELF_BLOCK_DELETE);

        try {
            entity.setUseYn("N");
            allowedIpRepository.save(entity);
        } catch (Exception e) {
            log.error("[AllowedIp] 삭제 실패 - seq={}", seqAllowedIp, e);
            throw new AllowedIpException(ResponseCode.FAIL_DELETE);
        }
        reloadCacheAfterCommit();
        return AllowedIpDeleteResultDto.builder()
                .seqAllowedIp(seqAllowedIp)
                .deleted(true)
                .build();
    }

    // ===== 5. 활성 토글 =====
    @Override
    @Transactional
    @AuditAction(action = "ALLOWED_IP_TOGGLE", targetType = "ALLOWED_IP")
    public AllowedIpResponseDto toggleActive(AllowedIpActor actor, Integer seqAllowedIp,
                                             Boolean isActive, String requesterIp) {
        AllowedIp entity = loadOrThrow(seqAllowedIp);
        assertScopeWritable(actor, campusIdOf(entity));

        boolean active = isActive != null && isActive;
        assertNotSelfBlocked(requesterIp, seqAllowedIp, entity.getIpValue(), active, false,
                ResponseCode.FAIL_UPDATE, MSG_SELF_BLOCK_UPDATE);

        AllowedIp saved;
        try {
            entity.setActive(active);
            saved = allowedIpRepository.save(entity);
        } catch (Exception e) {
            log.error("[AllowedIp] 활성 토글 실패 - seq={}", seqAllowedIp, e);
            throw new AllowedIpException(ResponseCode.FAIL_UPDATE);
        }
        reloadCacheAfterCommit();
        return toDto(saved, requesterIp);
    }

    // ===== 6. 내 IP =====
    @Override
    @Transactional(readOnly = true)
    public MyIpResponseDto getMyIp(String requesterIp) {
        // enabled 플래그와 무관하게 "규칙 기준으로 통과하는지"를 본다
        IpAllowlistEvaluator.Decision decision = evaluator.evaluate(requesterIp, false);
        return MyIpResponseDto.builder()
                .clientIp(requesterIp)
                .allowed(decision.allowed())
                .matchedRuleSeq(decision.matchedRuleSeq())
                .matchedRuleValue(decision.matchedRuleValue())
                .filterEnabled(evaluator.isEnabled())
                .activeRuleCount(evaluator.getActiveRuleCount())
                .bypassActive(evaluator.isBypassActive())
                .suggestedCidr(IpRangeMatcher.suggestCidr(requesterIp))
                .build();
    }

    // ===== 내부 헬퍼 =====

    private AllowedIp loadOrThrow(Integer seqAllowedIp) {
        return allowedIpRepository.findByIdWithCampus(seqAllowedIp)
                .orElseThrow(() -> new AllowedIpException(ResponseCode.NO_DATA));
    }

    private Integer campusIdOf(AllowedIp a) {
        return a.getSeqCampus() != null ? a.getSeqCampus().getSeqCampus() : null;
    }

    /** IP 표기 검증 후 트림된 값을 돌려준다. 표기 오류면 2003. */
    private String normalizeIpValue(String raw) {
        String value = raw == null ? null : raw.trim();
        if (value == null || value.isEmpty() || IpRangeMatcher.parse(value) == null) {
            throw new AllowedIpException(ResponseCode.INVALID_PARAM_PATTERN,
                    ResponseCode.INVALID_PARAM_PATTERN.getMessage("ipValue"));
        }
        return value;
    }

    /**
     * 저장 대상 캠퍼스를 확정한다.
     * 캠퍼스 관리자는 "키 없음"과 "명시적 null"을 구분하지 않고 자기 캠퍼스로 강제하며,
     * 다른 캠퍼스 값을 명시했을 때만 3002 로 거부한다 (계약서 2번 항목).
     */
    private Integer resolveTargetCampus(AllowedIpActor actor, Integer requested) {
        if (actor.superAdmin()) {
            return requested;
        }
        if (requested != null && !requested.equals(actor.campusId())) {
            throw new AllowedIpException(ResponseCode.NOT_AUTHORIZED);
        }
        return actor.campusId();
    }

    /** 캠퍼스 관리자는 전역 규칙·타 캠퍼스 규칙을 수정할 수 없다 (조회는 가능). */
    private void assertScopeWritable(AllowedIpActor actor, Integer targetCampusId) {
        if (actor.superAdmin()) {
            return;
        }
        if (targetCampusId == null || !targetCampusId.equals(actor.campusId())) {
            throw new AllowedIpException(ResponseCode.NOT_AUTHORIZED);
        }
    }

    private Campus loadCampus(Integer campusId) {
        if (campusId == null) {
            return null;
        }
        return campusRepository.findById(campusId)
                .orElseThrow(() -> new AllowedIpException(ResponseCode.NO_DATA, "존재하지 않는 캠퍼스입니다."));
    }

    /** 같은 스코프에 소프트 삭제된 동일 규칙(되살릴 대상)을 찾는다. */
    private Optional<AllowedIp> findSoftDeleted(String ipValue, Integer targetCampusId) {
        List<AllowedIp> found = (targetCampusId == null)
                ? allowedIpRepository.findSoftDeletedGlobal(ipValue)
                : allowedIpRepository.findSoftDeletedByCampus(ipValue, targetCampusId);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    /** 같은 스코프(seqCampus)에 동일 ipValue 가 이미 있으면 1002. */
    private void assertDuplicate(String ipValue, Integer targetCampusId, Integer excludeSeq) {
        for (AllowedIp a : allowedIpRepository.findByIpValue(ipValue)) {
            if (excludeSeq != null && excludeSeq.equals(a.getSeqAllowedIp())) {
                continue;
            }
            if (Objects.equals(campusIdOf(a), targetCampusId)) {
                throw new AllowedIpException(ResponseCode.EXIST_INFO);
            }
        }
    }

    /**
     * 변경 후 규칙 집합이 요청자 자신을 차단하는지 검사한다 (잠김 방지).
     *
     * <p>다음 경우엔 검사를 건너뛴다 — 어차피 잠기지 않기 때문이다.
     * <ul>
     *   <li>요청자가 루프백·비상우회·Docker 내부망 (항상 통과)</li>
     *   <li>변경 후 활성 규칙이 0건 (필터가 전면 허용으로 fail-open)</li>
     * </ul>
     *
     * @param changedSeq  변경 대상 규칙 seq (결과 집합에서 일단 제외한 뒤 아래 인자로 다시 반영)
     * @param newIpValue  변경 후 ipValue (삭제면 무시)
     * @param newActive   변경 후 활성 여부
     * @param removed     삭제 여부
     */
    private void assertNotSelfBlocked(String requesterIp, Integer changedSeq, String newIpValue,
                                      boolean newActive, boolean removed,
                                      ResponseCode code, String message) {
        if (evaluator.isAlwaysAllowed(requesterIp)) {
            return;
        }
        List<IpRangeMatcher> resulting = new ArrayList<>();
        for (AllowedIp a : allowedIpRepository.findActiveRules()) {
            if (changedSeq != null && changedSeq.equals(a.getSeqAllowedIp())) {
                continue;
            }
            IpRangeMatcher m = IpRangeMatcher.parse(a.getIpValue());
            if (m != null) {
                resulting.add(m);
            }
        }
        if (!removed && newActive && newIpValue != null) {
            IpRangeMatcher m = IpRangeMatcher.parse(newIpValue);
            if (m != null) {
                resulting.add(m);
            }
        }
        if (resulting.isEmpty()) {
            return; // 규칙 0건 → 필터가 전면 허용
        }
        if (IpRangeMatcher.matchesAny(resulting, requesterIp)) {
            return;
        }
        throw new AllowedIpException(code, message);
    }

    /** 커밋 이후에 캐시를 갱신한다. 롤백 시 캐시가 오염되지 않게 한다. */
    private void reloadCacheAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evaluator.reload();
                }
            });
        } else {
            evaluator.reload();
        }
    }

    private AllowedIpResponseDto toDto(AllowedIp a, String requesterIp) {
        IpRangeMatcher matcher = IpRangeMatcher.parse(a.getIpValue());
        boolean matched = matcher != null && requesterIp != null && matcher.matches(requesterIp);
        Campus campus = a.getSeqCampus();
        return AllowedIpResponseDto.builder()
                .seqAllowedIp(a.getSeqAllowedIp())
                .ipValue(a.getIpValue())
                .ipType(a.getIpType())
                .seqCampus(campus != null ? campus.getSeqCampus() : null)
                .campusName(campus != null ? campus.getNameCampus() : null)
                .description(a.getDescription())
                .isActive(a.isActive())
                .isSystem(a.isSystem())
                .matchedByRequester(matched)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}

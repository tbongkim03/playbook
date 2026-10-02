package playbook.encore.back.allowip.service;

import playbook.encore.back.allowip.dto.AllowedIpDeleteResultDto;
import playbook.encore.back.allowip.dto.AllowedIpRequestDto;
import playbook.encore.back.allowip.dto.AllowedIpResponseDto;
import playbook.encore.back.allowip.dto.MyIpResponseDto;

import java.util.List;

/**
 * 허용 IP 규칙 CRUD.
 *
 * <p>권한 스코프는 컨트롤러가 {@code AuthUtil} 로 확정해 {@link AllowedIpActor} 로 넘긴다.
 * LAZY 연관(Admin#seqCampus)을 서비스에서 다시 건드리지 않기 위해 엔티티가 아닌 값 타입으로 받는다.
 *
 * <p>변경 메서드는 {@code actor} 를 첫 인자로 두어 {@code seqAllowedIp} 가 감사로그 targetId 로
 * 정확히 잡히게 한다 (보안감사 S-4 — 자세한 내용은 {@link AllowedIpActor} 참조).
 */
public interface AllowedIpService {

    List<AllowedIpResponseDto> getAllowedIps(AllowedIpActor actor, Integer filterSeqCampus,
                                             boolean includeInactive, String requesterIp);

    AllowedIpResponseDto createAllowedIp(AllowedIpActor actor, AllowedIpRequestDto dto, String requesterIp);

    AllowedIpResponseDto updateAllowedIp(AllowedIpActor actor, Integer seqAllowedIp,
                                         AllowedIpRequestDto dto, String requesterIp);

    AllowedIpDeleteResultDto deleteAllowedIp(AllowedIpActor actor, Integer seqAllowedIp, String requesterIp);

    AllowedIpResponseDto toggleActive(AllowedIpActor actor, Integer seqAllowedIp,
                                      Boolean isActive, String requesterIp);

    MyIpResponseDto getMyIp(String requesterIp);
}

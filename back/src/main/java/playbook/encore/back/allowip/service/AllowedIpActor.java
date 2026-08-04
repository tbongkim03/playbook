package playbook.encore.back.allowip.service;

/**
 * 허용 IP 작업의 요청자 권한 스코프.
 *
 * <p>컨트롤러가 {@code AuthUtil} 로 확정한 값을 서비스로 넘기는 내부 값 타입이다.
 * API 응답에 직렬화되지 않으므로 dto 패키지에 두지 않는다.
 *
 * <p><b>왜 Integer 두 개를 그냥 넘기지 않고 묶었나 (보안감사 S-4)</b><br>
 * {@code AuditLogAspect.extractTargetId} 는 파라미터 이름을 앞에서부터 훑어
 * {@code startsWith("seq") || endsWith("id")} 인 첫 인자를 감사로그 targetId 로 삼는다.
 * 이전 시그니처의 {@code Integer adminCampusId} 는 {@code "admincampusid".endsWith("id")} 에 걸리고
 * {@code seqAllowedIp} 보다 앞에 있어, <b>캠퍼스 관리자의 변경 4개 메서드 전부에서
 * targetId 에 규칙 ID 대신 캠퍼스 ID 가 기록됐다.</b>
 * 이 record 로 감싸면 이름 규칙에도 걸리지 않고 Integer 폴백에도 잡히지 않아,
 * {@code seqAllowedIp} 가 정확히 targetId 로 잡힌다.
 * {@code AuditLogAspect} 는 전 도메인 공용 코드라 건드리지 않고 호출부에서 해결했다.
 *
 * @param superAdmin 전체관리자 여부 (seqCampus == null)
 * @param campusId   요청자의 캠퍼스 ID. 전체관리자면 null
 */
public record AllowedIpActor(boolean superAdmin, Integer campusId) {
}

package playbook.encore.back.allowip.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * nginx auth_request 게이트. 최종 경로는 {@code /api/ip-gate} (context-path=/api).
 *
 * <p>prod 에서 nginx 가 프론트 정적 파일을 직접 서빙하므로 그 요청은 Spring 을 거치지 않는다.
 * nginx 가 정적 요청마다 이 경로로 서브요청을 보내 IP 판정을 위임한다.
 *
 * <p><b>판정은 전적으로 {@code IpAllowlistFilter} 가 한다.</b>
 * 차단 대상이면 필터가 403 으로 먼저 끊어 이 메서드에 도달하지 않는다.
 * 즉 여기까지 왔다는 것은 이미 "통과"라는 뜻이므로 204 만 반환한다.
 *
 * <p><b>인증을 붙이지 않는다 (의도된 설계).</b>
 * WebConfig 의 인터셉터 addPathPatterns 는 명시된 경로만 가로채는데 {@code /ip-gate} 는
 * 그 목록에 없다. nginx 서브요청에는 세션 쿠키가 없으므로, 인증이 걸리면 401 이 나가고
 * auth_request 가 이를 거부로 해석해 <b>전 사용자가 차단된다.</b>
 * 따라서 이 경로를 인터셉터 목록에 추가해서는 안 된다.
 *
 * <p>노출되는 정보는 "요청자 IP가 허용 대상인지" 뿐이며, 이는 요청자가 서비스 접속 가능 여부로
 * 이미 알 수 있는 사실이다. 세션·DB 조회를 하지 않아 정적 자산마다 호출돼도 부담이 없다.
 */
@RestController
public class IpGateController {

    @GetMapping("/ip-gate")
    public ResponseEntity<Void> gate() {
        return ResponseEntity.noContent().build();
    }
}

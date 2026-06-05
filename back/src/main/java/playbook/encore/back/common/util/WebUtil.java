package playbook.encore.back.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class WebUtil {

    /**
     * 클라이언트 IP 추출 (리버스 프록시 X-Forwarded-For 우선)
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            // 다중 IP일 경우 첫 번째(원본 클라이언트)만 사용
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

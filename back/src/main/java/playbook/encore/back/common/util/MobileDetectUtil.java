package playbook.encore.back.common.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.regex.Pattern;

public class MobileDetectUtil {

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isMobile(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) return false;
        return MOBILE_PATTERN.matcher(userAgent).find();
    }
}

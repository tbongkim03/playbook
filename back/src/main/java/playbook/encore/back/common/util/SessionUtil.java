package playbook.encore.back.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

public class SessionUtil {

    public static void createSession(
            HttpServletRequest request,
            RedisIndexedSessionRepository sessionRepository,
            String userId,
            String role) {

        sessionRepository.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, userId)
                .keySet().forEach(sessionRepository::deleteById);

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", userId);
        session.setAttribute("role", role);
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, userId);
        session.setMaxInactiveInterval(3600);
    }
}

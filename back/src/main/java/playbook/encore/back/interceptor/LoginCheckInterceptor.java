package playbook.encore.back.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.jwt.jwtUtil;

import java.util.Optional;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final jwtUtil jwtUtil;
    private final BookUserRepository bookUserRepository;

    @Autowired
    public LoginCheckInterceptor(jwtUtil jwtUtil, BookUserRepository bookUserRepository) {
        this.jwtUtil = jwtUtil;
        this.bookUserRepository = bookUserRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        // 1. 비로그인 상태인 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (request.getRequestURI().startsWith("/???") || request.getRequestURI().startsWith("/????")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("로그인이 필요합니다.");
                return false;
            }
            // 로그인 필요없는 api 통과
            return true;
        }
        // 2. 로그인 상태인 경우
        else {
            String token = authHeader.substring(7);
            String reason = jwtUtil.validateAndGetReason(token);

            if (!reason.equals("VALID")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("토큰이 유효하지 않습니다: " + reason);
                return false;
            }

            String userId = jwtUtil.getIdUserFromToken(token);
            Optional<BookUser> userOpt = bookUserRepository.findByIdUser(userId);
            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("해당 사용자가 존재하지 않습니다.");
                return false;
            } else {
                request.setAttribute("user", userOpt.get());
                return true;
            }

        }
    }
}

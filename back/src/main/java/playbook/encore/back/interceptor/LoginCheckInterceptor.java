package playbook.encore.back.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import playbook.encore.back.data.entity.Admin;
import playbook.encore.back.data.entity.BookUser;
import playbook.encore.back.data.repository.AdminRepository;
import playbook.encore.back.data.repository.BookUserRepository;
import playbook.encore.back.jwt.jwtUtil;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final jwtUtil jwtUtil;
    private final BookUserRepository bookUserRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public LoginCheckInterceptor(jwtUtil jwtUtil, BookUserRepository bookUserRepository, AdminRepository adminRepository) {
        this.jwtUtil = jwtUtil;
        this.bookUserRepository = bookUserRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (
                ((uri.equals("/subtitles") && method.equals("POST")) || (uri.startsWith("/subtitles/") && (method.equals("PUT") || method.equals("DELETE"))))
                || ((uri.equals("/subjects") && method.equals("POST")) || (uri.startsWith("/subjects/") && (method.equals("PUT") || method.equals("DELETE"))))
                || ((uri.equals("/courses") && method.equals("POST")) || (uri.startsWith("/courses/") && (method.equals("PUT") || method.equals("DELETE"))))
                || ((uri.equals("/users") && method.equals("POST")) || (uri.startsWith("/users/") && (method.equals("PUT") || method.equals("DELETE"))))
                || ((uri.equals("/books") && method.equals("POST")) || (uri.startsWith("/books/") && (method.equals("PUT") || method.equals("DELETE"))))
                || ((uri.equals("/users/me") && method.equals("GET")))
        ) {
            // 로그인 검증 로직
            if (!isLoggedIn(request, response, handler)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        return true;
    }

    private boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("로그인이 필요합니다.");
            return false;
        }

        String token = authHeader.substring(7);
        String reason = jwtUtil.validateAndGetReason(token);

        if (!reason.equals("VALID")) {
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("토큰이 유효하지 않습니다: " + reason);
            return false;
        }

        String userId = jwtUtil.getIdUserFromToken(token);
        Optional<BookUser> userOpt = bookUserRepository.findByIdUser(userId);
        Optional<Admin> adminOpt = adminRepository.findByIdAdmin(userId);
        if (userOpt.isEmpty() && adminOpt.isEmpty() ) {
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("해당 사용자가 존재하지 않습니다.");
            return false;
        }

        if (userOpt.isPresent()) {
            request.setAttribute("user", userOpt.get());
            request.setAttribute("ROLE", RoleType.USER);
        } else {
            request.setAttribute("admin", adminOpt.get());
            request.setAttribute("ROLE", RoleType.ADMIN);
        }

        return true;
    }

    private void setUtf8Response(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");
    }

    public enum RoleType {
        USER, ADMIN
    }

}



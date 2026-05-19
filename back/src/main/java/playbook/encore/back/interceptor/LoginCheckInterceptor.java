package playbook.encore.back.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
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
        return isLoggedIn(request, response, handler);
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
        String role = jwtUtil.getRoleFromToken(token);

        if ("admin".equalsIgnoreCase(role)) {
            Optional<Admin> adminOpt = adminRepository.findByIdAdminWithCampus(userId);
            if (adminOpt.isEmpty()) {
                setUtf8Response(response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("해당 관리자가 존재하지 않습니다.");
                return false;
            }
            Admin admin = adminOpt.get();
            request.setAttribute("admin", admin);
            request.setAttribute("ROLE", RoleType.ADMIN);
            request.setAttribute("campusId", admin.getSeqCampus() != null ? admin.getSeqCampus().getSeqCampus() : null);
        } else if ("user".equalsIgnoreCase(role)) {
            Optional<BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(userId);
            if (userOpt.isEmpty()) {
                setUtf8Response(response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("해당 사용자가 존재하지 않습니다.");
                return false;
            }
            BookUser user = userOpt.get();
            request.setAttribute("user", user);
            request.setAttribute("ROLE", RoleType.USER);
            Integer campusId = (user.getSeqCourse() != null && user.getSeqCourse().getSeqCampus() != null)
                    ? user.getSeqCourse().getSeqCampus().getSeqCampus()
                    : null;
            request.setAttribute("campusId", campusId);
        } else {
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("올바르지 않은 사용자 입니다.");
            return false;
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



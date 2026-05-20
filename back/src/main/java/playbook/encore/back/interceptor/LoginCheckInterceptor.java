package playbook.encore.back.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.admin.dao.AdminRepository;
import playbook.encore.back.bookUser.dao.BookUserRepository;
import playbook.encore.back.common.audit.AuditContext;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final BookUserRepository bookUserRepository;
    private final AdminRepository adminRepository;
    private final AuditContext auditContext;

    @Autowired
    public LoginCheckInterceptor(BookUserRepository bookUserRepository, AdminRepository adminRepository, AuditContext auditContext) {
        this.bookUserRepository = bookUserRepository;
        this.adminRepository = adminRepository;
        this.auditContext = auditContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return isLoggedIn(request, response);
    }

    private boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("로그인이 필요합니다.");
            return false;
        }

        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

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
            auditContext.setActorId(admin.getSeqAdmin().longValue());
            auditContext.setActorType("ADMIN");
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
            auditContext.setActorId(user.getSeqUser().longValue());
            auditContext.setActorType("USER");
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

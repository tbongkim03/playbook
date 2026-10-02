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
import java.util.Set;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final BookUserRepository bookUserRepository;
    private final AdminRepository adminRepository;
    private final AuditContext auditContext;

    // /books/{공개조회} 단일 세그먼트 경로 (관리자 전용 /books/all, /books/count 등과 구분)
    private static final Set<String> PUBLIC_BOOK_SUBPATHS = Set.of("search", "related", "sortFirst", "sortSecond");

    @Autowired
    public LoginCheckInterceptor(BookUserRepository bookUserRepository, AdminRepository adminRepository, AuditContext auditContext) {
        this.bookUserRepository = bookUserRepository;
        this.adminRepository = adminRepository;
        this.auditContext = auditContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 세션이 있으면 인증 컨텍스트(ROLE, campusId, user/admin, audit)를 채운다.
        AuthState state = populateAuthContext(request, response);
        if (state == AuthState.INVALID) {
            return false; // 응답은 populateAuthContext에서 이미 작성됨
        }

        // 공개 조회(GET) 엔드포인트는 비로그인도 허용. 관리자 전용 작업은 컨트롤러의 AuthUtil로 검증한다.
        if (isPublicReadEndpoint(request)) {
            return true;
        }

        // 그 외 보호 대상 경로는 로그인 필수
        if (state == AuthState.ANONYMOUS) {
            writeUnauthorized(response, "로그인이 필요합니다.");
            return false;
        }
        return true;
    }

    private AuthState populateAuthContext(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return AuthState.ANONYMOUS;
        }

        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if ("admin".equalsIgnoreCase(role)) {
            Optional<Admin> adminOpt = adminRepository.findByIdAdminWithCampus(userId);
            if (adminOpt.isEmpty()) {
                writeUnauthorized(response, "해당 관리자가 존재하지 않습니다.");
                return AuthState.INVALID;
            }
            Admin admin = adminOpt.get();
            request.setAttribute("admin", admin);
            request.setAttribute("ROLE", RoleType.ADMIN);
            request.setAttribute("campusId", admin.getSeqCampus() != null ? admin.getSeqCampus().getSeqCampus() : null);
            auditContext.setActorId(admin.getSeqAdmin().longValue());
            auditContext.setActorName(admin.getNameAdmin());
            auditContext.setActorType("ADMIN");
            return AuthState.AUTHENTICATED;
        } else if ("user".equalsIgnoreCase(role)) {
            Optional<BookUser> userOpt = bookUserRepository.findByIdUserWithCourseAndCampus(userId);
            if (userOpt.isEmpty()) {
                writeUnauthorized(response, "해당 사용자가 존재하지 않습니다.");
                return AuthState.INVALID;
            }
            BookUser user = userOpt.get();
            request.setAttribute("user", user);
            request.setAttribute("ROLE", RoleType.USER);
            Integer campusId = (user.getSeqCourse() != null && user.getSeqCourse().getSeqCampus() != null)
                    ? user.getSeqCourse().getSeqCampus().getSeqCampus()
                    : null;
            request.setAttribute("campusId", campusId);
            auditContext.setActorId(user.getSeqUser().longValue());
            auditContext.setActorName(user.getNameUser());
            auditContext.setActorType("USER");
            return AuthState.AUTHENTICATED;
        } else {
            writeUnauthorized(response, "올바르지 않은 사용자 입니다.");
            return AuthState.INVALID;
        }
    }

    // 비로그인 공개 조회 허용 경로: GET /books, /books/{search|related|sortFirst|sortSecond|{id}}, /campus
    // 관리자 전용 조회(/books/all, /books/admin/list, /campus/{id} 등)는 여기서 제외되어 로그인/권한 검증을 거친다.
    private boolean isPublicReadEndpoint(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        // context-path(/api) 제거한 애플리케이션 상대 경로로 비교
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        if ("/campus".equals(uri) || "/books".equals(uri)) {
            return true;
        }
        if (uri.startsWith("/books/")) {
            String sub = uri.substring("/books/".length());
            if (sub.isEmpty() || sub.indexOf('/') >= 0) {
                return false; // /books/admin/list 같은 다단계 경로는 관리자 전용
            }
            if (PUBLIC_BOOK_SUBPATHS.contains(sub)) {
                return true;
            }
            return sub.chars().allMatch(Character::isDigit); // /books/{id} 단건 조회
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        setUtf8Response(response);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(message);
    }

    private void setUtf8Response(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");
    }

    private enum AuthState {
        AUTHENTICATED, ANONYMOUS, INVALID
    }

    public enum RoleType {
        USER, ADMIN
    }
}

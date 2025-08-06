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
                || ((uri.equals("/books/check/barcode") && method.equals("POST")))
                || ((uri.equals("/books/unprinted") && method.equals("GET")))
                || ((uri.equals("/users/me") && method.equals("GET")))
                || ((uri.equals("/admin/me") && method.equals("GET")))
                || ((uri.equals("/api/borrow") && method.equals("POST")))
                || ((uri.equals("/api/return") && method.equals("PUT")))
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

//        System.out.println("=== 토큰 처리 디버깅 ===");
//        System.out.println("Authorization 헤더: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            System.out.println("Authorization 헤더가 없거나 Bearer 형식이 아님");
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("로그인이 필요합니다.");
            return false;
        }

        String token = authHeader.substring(7);
//        System.out.println("추출된 토큰: " + token);

        String reason = jwtUtil.validateAndGetReason(token);
//        System.out.println("토큰 검증 결과: " + reason);

        if (!reason.equals("VALID")) {
//            System.out.println("토큰이 유효하지 않음: " + reason);
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("토큰이 유효하지 않습니다: " + reason);
            return false;
        }

        String userId = jwtUtil.getIdUserFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        System.out.println("사용자 ID: " + userId);
        System.out.println("역할: " + role);

        if ("admin".equalsIgnoreCase(role)) {
            System.out.println("관리자 역할 확인됨");
            Optional<Admin> adminOpt = adminRepository.findByIdAdmin(userId);
            if (adminOpt.isEmpty()) {
//                System.out.println("관리자가 DB에서 찾아지지 않음");
                setUtf8Response(response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("해당 관리자가 존재하지 않습니다.");
                return false;
            }
//            System.out.println("관리자 정보 확인됨, ROLE 속성 설정");
            request.setAttribute("admin", adminOpt.get());
            request.setAttribute("ROLE", RoleType.ADMIN);
//            System.out.println("설정된 ROLE: " + request.getAttribute("ROLE"));
        } else if ("user".equalsIgnoreCase(role)) {
            Optional<BookUser> userOpt = bookUserRepository.findByIdUser(userId);
            if (userOpt.isEmpty()) {
                setUtf8Response(response);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("해당 사용자가 존재하지 않습니다.");
                return false;
            }
            request.setAttribute("user", userOpt.get());
            request.setAttribute("ROLE", RoleType.USER);
        } else {
//            System.out.println("올바르지 않은 역할: " + role);
            setUtf8Response(response);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("올바르지 않은 사용자 입니다.");
            return false;
        }

//        System.out.println("인증 성공");
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



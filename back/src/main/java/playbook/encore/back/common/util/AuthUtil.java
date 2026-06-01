package playbook.encore.back.common.util;

import jakarta.servlet.http.HttpServletRequest;
import playbook.encore.back.admin.entity.Admin;
import playbook.encore.back.bookUser.entity.BookUser;
import playbook.encore.back.common.exception.NotAuthorizedException;
import playbook.encore.back.interceptor.LoginCheckInterceptor;

public class AuthUtil {

    public static void requireAdmin(HttpServletRequest request) {
        if (!LoginCheckInterceptor.RoleType.ADMIN.equals(request.getAttribute("ROLE"))) {
            throw new NotAuthorizedException();
        }
    }

    public static Admin getAdmin(HttpServletRequest request) {
        requireAdmin(request);
        return (Admin) request.getAttribute("admin");
    }

    public static void requireUser(HttpServletRequest request) {
        if (!LoginCheckInterceptor.RoleType.USER.equals(request.getAttribute("ROLE"))) {
            throw new NotAuthorizedException();
        }
    }

    public static BookUser getUser(HttpServletRequest request) {
        requireUser(request);
        return (BookUser) request.getAttribute("user");
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return LoginCheckInterceptor.RoleType.ADMIN.equals(request.getAttribute("ROLE"));
    }

    public static boolean isUser(HttpServletRequest request) {
        return LoginCheckInterceptor.RoleType.USER.equals(request.getAttribute("ROLE"));
    }

    public static Integer getCampusId(HttpServletRequest request, Integer requestParam) {
        return requestParam != null ? requestParam : (Integer) request.getAttribute("campusId");
    }
}

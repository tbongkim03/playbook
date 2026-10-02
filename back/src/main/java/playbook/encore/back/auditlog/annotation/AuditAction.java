package playbook.encore.back.auditlog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 관리자 작업 감사 로그를 자동 기록하는 어노테이션.
 * 적용된 메서드 실행 전후로 tb_audit_log 에 기록된다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {

    /** 작업 유형 (예: BOOK_CREATE, USER_DELETE) */
    String action();

    /** 작업 대상 유형 (예: BOOK, USER, ADMIN, COURSE) */
    String targetType();

    /** 추가 설명 (선택) */
    String detail() default "";
}

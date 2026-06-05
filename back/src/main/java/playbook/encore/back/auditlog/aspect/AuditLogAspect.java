package playbook.encore.back.auditlog.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import playbook.encore.back.auditlog.annotation.AuditAction;
import playbook.encore.back.auditlog.service.AuditLogService;
import playbook.encore.back.common.audit.AuditContext;

import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final AuditContext auditContext;

    @Around("@annotation(auditAction)")
    public Object around(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        String targetId = extractTargetId(joinPoint);
        try {
            Object result = joinPoint.proceed();
            record(auditAction, targetId, "SUCCESS", null);
            return result;
        } catch (Exception e) {
            record(auditAction, targetId, "FAIL", truncate(e.getMessage(), 100));
            throw e;
        }
    }

    private void record(AuditAction auditAction, String targetId, String result, String failReason) {
        try {
            Long actorId = auditContext.getActorId();
            if (actorId == null) {
                log.warn("[AuditLog] actorId 없음 — 감사 로그 생략");
                return;
            }
            String actorName = auditContext.getActorName();
            String detail = auditAction.detail().isEmpty() ? null : auditAction.detail();
            auditLogService.save(actorId, actorName != null ? actorName : "unknown",
                    auditAction.action(), auditAction.targetType(), targetId, detail, result, failReason);
        } catch (Exception e) {
            log.error("[AuditLog] 감사 로그 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * seq* 또는 *Id 패턴 파라미터에서 대상 ID를 추출.
     * 없으면 첫 번째 숫자형 인자를 사용.
     */
    private String extractTargetId(ProceedingJoinPoint joinPoint) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Parameter[] params = sig.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < params.length; i++) {
            String name = params[i].getName();
            if ((name.startsWith("seq") || name.toLowerCase().endsWith("id")) && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }
        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof Integer) {
                return String.valueOf(arg);
            }
        }
        return null;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

package com.wesjou.keymanager.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLogAspect {
    private final AuditLogRepository auditLogRepository;

    public AuditLogAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(auditable)")
    public Object auditLog(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        var audit = new AuditLog();

        audit.setAction(auditable.action());
        audit.setResourceType(auditable.resourceType());

        var index = auditable.resourceIdArgIndex();
        var args = joinPoint.getArgs();
        if (index >= 0 && index < args.length) {
            var resourceIdArg = args[index];
            if (resourceIdArg != null) {
                audit.setResourceId(String.valueOf(resourceIdArg));
            }
        } else {
            audit.setResourceId(null);
        }

        audit.setActorType(AuditActorType.USER);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            audit.setActorId(authentication.getName());
        } else {
            audit.setActorId("Unknown");
        }

        var request = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (request != null) {
            audit.setIpAddress(request.getRequest().getRemoteAddr());
        } else {
            audit.setIpAddress(null);
        }

        try {
            var returnedByMethod = joinPoint.proceed();
            audit.setSuccess(true);
            auditLogRepository.save(audit);
            return returnedByMethod;
        } catch (Throwable e) {
            audit.setSuccess(false);
            auditLogRepository.save(audit);
            throw e;
        }
    }
}

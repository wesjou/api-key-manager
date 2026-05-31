package com.wesjou.keymanager.audit;

import java.time.LocalDateTime;

record AuditLogResponse(AuditActorType actor, AuditAction action, AuditResourceType resourceType,
                        boolean success, String ipAddress, LocalDateTime createdAt) {
}

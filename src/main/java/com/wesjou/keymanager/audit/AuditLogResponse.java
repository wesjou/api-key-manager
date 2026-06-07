package com.wesjou.keymanager.audit;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AuditLogResponse(
        @Schema(description = "The type of actor who performed the action", example = "USER")
        AuditActorType actor,

        @Schema(description = "The action that was performed", example = "API_KEY_CREATED")
        AuditAction action,

        @Schema(description = "The type of resource affected", example = "API_KEY")
        AuditResourceType resourceType,

        @Schema(description = "Whether the action was successful", example = "true")
        boolean success,

        @Schema(description = "The IP address from which the request originated", example = "192.168.1.1")
        String ipAddress,

        @Schema(description = "When the audit log entry was created", example = "2026-06-06T10:00:00")
        LocalDateTime createdAt
) {
}

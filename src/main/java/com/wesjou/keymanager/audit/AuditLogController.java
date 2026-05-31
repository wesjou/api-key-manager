package com.wesjou.keymanager.audit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/audit-logs")
@RestController
class AuditLogController {
    private final AuditLogService auditLogService;

    AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    List<AuditLogResponse> getLatestAuditLogs() {
        return auditLogService.getLatestAuditLogs();
    }
}

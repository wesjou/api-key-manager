package com.wesjou.keymanager.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Audit Logs", description = "Endpoint for retrieving system audit logs.")
@SecurityRequirement(name = "jwtAuth")
@RequestMapping("/api/v1/audit-logs")
@RestController
class AuditLogController {
    private final AuditLogService auditLogService;

    AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "Get latest audit logs", description = "Retrieves the 50 most recent audit logs from the system. Admin access only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved audit logs"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Admin role required")
    })
    @GetMapping
    List<AuditLogResponse> getLatestAuditLogs() {
        return auditLogService.getLatestAuditLogs();
    }
}

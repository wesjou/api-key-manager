package com.wesjou.keymanager.audit;

import java.util.List;

interface AuditLogService {
    List<AuditLogResponse> getLatestAuditLogs();
}

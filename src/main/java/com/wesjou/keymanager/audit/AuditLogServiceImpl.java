package com.wesjou.keymanager.audit;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }


    @Override
    public List<AuditLogResponse> getLatestAuditLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(auditLog -> new AuditLogResponse(
                        auditLog.getActorType(),
                        auditLog.getAction(),
                        auditLog.getResourceType(),
                        auditLog.isSuccess(),
                        auditLog.getIpAddress(),
                        auditLog.getCreatedAt()
                )).toList();
    }
}

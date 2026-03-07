package com.example.EyeCareHubDB.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.EyeCareHubDB.entity.Account;
import com.example.EyeCareHubDB.entity.AuditLog;
import com.example.EyeCareHubDB.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long actorAccountId, String action, String entityType, Long entityId,
                    String oldData, String newData) {
        AuditLog log = AuditLog.builder()
            .actorAccount(actorAccountId != null ? Account.builder().id(actorAccountId).build() : null)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .oldDataJson(oldData)
            .newDataJson(newData)
            .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getLogsByActor(Long accountId) {
        return auditLogRepository.findByActorAccountIdOrderByPerformedAtDesc(accountId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}

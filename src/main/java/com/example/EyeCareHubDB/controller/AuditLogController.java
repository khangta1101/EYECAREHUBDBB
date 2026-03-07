package com.example.EyeCareHubDB.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.AuditLog;
import com.example.EyeCareHubDB.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/entity")
    public ResponseEntity<List<AuditLog>> getByEntity(@RequestParam String entityType,
                                                       @RequestParam Long entityId) {
        return ResponseEntity.ok(auditLogService.getLogsByEntity(entityType, entityId));
    }

    @GetMapping("/actor/{accountId}")
    public ResponseEntity<List<AuditLog>> getByActor(@PathVariable Long accountId) {
        return ResponseEntity.ok(auditLogService.getLogsByActor(accountId));
    }
}

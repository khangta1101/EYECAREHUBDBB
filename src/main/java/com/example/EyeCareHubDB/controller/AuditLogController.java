package com.example.EyeCareHubDB.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EyeCareHubDB.entity.AuditLog;
import com.example.EyeCareHubDB.service.AuditLogService;

import lombok.RequiredArgsConstructor;

@Tag(name = "Audit Log")
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/entity")
    public ResponseEntity<List<AuditLog>> getByEntity(@RequestParam("entityType") String entityType,
                                                       @RequestParam("entityId") Long entityId) {
        return ResponseEntity.ok(auditLogService.getLogsByEntity(entityType, entityId));
    }

    @GetMapping("/actor/{accountId}")
    public ResponseEntity<List<AuditLog>> getByActor(@PathVariable("accountId") Long accountId) {
        return ResponseEntity.ok(auditLogService.getLogsByActor(accountId));
    }
}

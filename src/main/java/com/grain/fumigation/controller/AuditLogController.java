package com.grain.fumigation.controller;

import com.grain.fumigation.dto.ApiResponse;
import com.grain.fumigation.entity.AuditLog;
import com.grain.fumigation.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/logs/operation/{operationId}")
    public ApiResponse<List<AuditLog>> getAuditLogsByOperationId(
            @PathVariable Long operationId) {
        List<AuditLog> logs = auditLogService.getAuditLogsByOperationId(operationId);
        return ApiResponse.success(logs);
    }

    @GetMapping("/logs/ticket/{ticketNo}")
    public ApiResponse<List<AuditLog>> getAuditLogsByTicketNo(
            @PathVariable String ticketNo) {
        List<AuditLog> logs = auditLogService.getAuditLogsByTicketNo(ticketNo);
        return ApiResponse.success(logs);
    }

    @GetMapping("/logs/operator/{operatorId}")
    public ApiResponse<List<AuditLog>> getAuditLogsByOperatorId(
            @PathVariable String operatorId) {
        List<AuditLog> logs = auditLogService.getAuditLogsByOperatorId(operatorId);
        return ApiResponse.success(logs);
    }

    @GetMapping("/logs")
    public ApiResponse<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogService.getAllAuditLogs();
        return ApiResponse.success(logs);
    }
}

package com.grain.fumigation.service;

import com.grain.fumigation.entity.AuditLog;
import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.enums.AuditOperationType;
import com.grain.fumigation.enums.OperationRole;
import com.grain.fumigation.enums.OperationStatus;
import com.grain.fumigation.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public AuditLog createAuditLog(FumigationOperation operation,
                                   AuditOperationType operationType,
                                   OperationRole operatorRole,
                                   String operatorId,
                                   String operatorName,
                                   String operationDetail,
                                   OperationStatus beforeStatus,
                                   OperationStatus afterStatus,
                                   boolean success,
                                   String failureReason,
                                   String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationId(operation.getId());
        auditLog.setTicketNo(operation.getTicketNo());
        auditLog.setOperationType(operationType);
        auditLog.setOperatorRole(operatorRole);
        auditLog.setOperatorId(operatorId);
        auditLog.setOperatorName(operatorName);
        auditLog.setOperationDetail(operationDetail);
        auditLog.setBeforeStatus(beforeStatus != null ? beforeStatus.name() : null);
        auditLog.setAfterStatus(afterStatus != null ? afterStatus.name() : null);
        auditLog.setSuccess(success);
        auditLog.setFailureReason(failureReason);
        auditLog.setIpAddress(ipAddress);

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsByOperationId(Long operationId) {
        return auditLogRepository.findByOperationIdOrderByOperationTimeDesc(operationId);
    }

    public List<AuditLog> getAuditLogsByTicketNo(String ticketNo) {
        return auditLogRepository.findByTicketNoOrderByOperationTimeDesc(ticketNo);
    }

    public List<AuditLog> getAuditLogsByOperatorId(String operatorId) {
        return auditLogRepository.findByOperatorIdOrderByOperationTimeDesc(operatorId);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}

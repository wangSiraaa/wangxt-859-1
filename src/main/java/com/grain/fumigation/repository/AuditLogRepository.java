package com.grain.fumigation.repository;

import com.grain.fumigation.entity.AuditLog;
import com.grain.fumigation.enums.AuditOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByOperationIdOrderByOperationTimeDesc(Long operationId);

    List<AuditLog> findByTicketNoOrderByOperationTimeDesc(String ticketNo);

    List<AuditLog> findByOperatorIdOrderByOperationTimeDesc(String operatorId);

    List<AuditLog> findByOperationType(AuditOperationType operationType);

    List<AuditLog> findBySuccess(Boolean success);
}

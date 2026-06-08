package com.grain.fumigation.event;

import com.grain.fumigation.entity.FumigationOperation;
import com.grain.fumigation.enums.AuditOperationType;
import com.grain.fumigation.enums.OperationRole;
import com.grain.fumigation.enums.OperationStatus;
import org.springframework.context.ApplicationEvent;

public class StatusChangedEvent extends ApplicationEvent {

    private final FumigationOperation operation;
    private final OperationStatus beforeStatus;
    private final OperationStatus afterStatus;
    private final AuditOperationType operationType;
    private final OperationRole operatorRole;
    private final String operatorId;
    private final String operatorName;
    private final boolean success;
    private final String failureReason;

    public StatusChangedEvent(FumigationOperation operation,
                              OperationStatus beforeStatus,
                              OperationStatus afterStatus,
                              AuditOperationType operationType,
                              OperationRole operatorRole,
                              String operatorId,
                              String operatorName,
                              boolean success,
                              String failureReason) {
        super(operation);
        this.operation = operation;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.operationType = operationType;
        this.operatorRole = operatorRole;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.success = success;
        this.failureReason = failureReason;
    }

    public FumigationOperation getOperation() {
        return operation;
    }

    public OperationStatus getBeforeStatus() {
        return beforeStatus;
    }

    public OperationStatus getAfterStatus() {
        return afterStatus;
    }

    public AuditOperationType getOperationType() {
        return operationType;
    }

    public OperationRole getOperatorRole() {
        return operatorRole;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureReason() {
        return failureReason;
    }
}

package com.grain.fumigation.exception;

import com.grain.fumigation.enums.AuditOperationType;
import com.grain.fumigation.enums.OperationRole;
import com.grain.fumigation.enums.OperationStatus;

public class BusinessException extends RuntimeException {

    private final int code;
    private Long operationId;
    private String ticketNo;
    private AuditOperationType operationType;
    private OperationRole operatorRole;
    private String operatorId;
    private String operatorName;
    private OperationStatus beforeStatus;
    private OperationStatus afterStatus;
    private String ipAddress;
    private boolean auditLogged = false;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }

    public int getCode() {
        return code;
    }

    public Long getOperationId() {
        return operationId;
    }

    public BusinessException setOperationId(Long operationId) {
        this.operationId = operationId;
        return this;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public BusinessException setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
        return this;
    }

    public AuditOperationType getOperationType() {
        return operationType;
    }

    public BusinessException setOperationType(AuditOperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public OperationRole getOperatorRole() {
        return operatorRole;
    }

    public BusinessException setOperatorRole(OperationRole operatorRole) {
        this.operatorRole = operatorRole;
        return this;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public BusinessException setOperatorId(String operatorId) {
        this.operatorId = operatorId;
        return this;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public BusinessException setOperatorName(String operatorName) {
        this.operatorName = operatorName;
        return this;
    }

    public OperationStatus getBeforeStatus() {
        return beforeStatus;
    }

    public BusinessException setBeforeStatus(OperationStatus beforeStatus) {
        this.beforeStatus = beforeStatus;
        return this;
    }

    public OperationStatus getAfterStatus() {
        return afterStatus;
    }

    public BusinessException setAfterStatus(OperationStatus afterStatus) {
        this.afterStatus = afterStatus;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public BusinessException setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public boolean isAuditLogged() {
        return auditLogged;
    }

    public BusinessException setAuditLogged(boolean auditLogged) {
        this.auditLogged = auditLogged;
        return this;
    }
}

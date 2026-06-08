package com.grain.fumigation.dto;

import com.grain.fumigation.enums.OperationRole;
import com.grain.fumigation.enums.OperationStatus;

import java.time.LocalDateTime;

public class TodoItemVO {

    private Long operationId;
    private String ticketNo;
    private String warehouseCode;
    private String warehouseName;
    private String grainType;
    private OperationStatus status;
    private String statusDescription;
    private OperationRole requiredRole;
    private String requiredRoleDescription;
    private String actionName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getGrainType() {
        return grainType;
    }

    public void setGrainType(String grainType) {
        this.grainType = grainType;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public OperationRole getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(OperationRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    public String getRequiredRoleDescription() {
        return requiredRoleDescription;
    }

    public void setRequiredRoleDescription(String requiredRoleDescription) {
        this.requiredRoleDescription = requiredRoleDescription;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

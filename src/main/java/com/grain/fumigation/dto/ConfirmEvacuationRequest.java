package com.grain.fumigation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ConfirmEvacuationRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    @NotNull(message = "人员是否撤离不能为空")
    private Boolean evacuationConfirmed;

    @NotBlank(message = "安全员ID不能为空")
    private String operatorId;

    private String operatorName;

    private String evacuationRemark;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Boolean getEvacuationConfirmed() {
        return evacuationConfirmed;
    }

    public void setEvacuationConfirmed(Boolean evacuationConfirmed) {
        this.evacuationConfirmed = evacuationConfirmed;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getEvacuationRemark() {
        return evacuationRemark;
    }

    public void setEvacuationRemark(String evacuationRemark) {
        this.evacuationRemark = evacuationRemark;
    }
}

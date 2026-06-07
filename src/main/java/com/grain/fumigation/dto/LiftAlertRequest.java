package com.grain.fumigation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class LiftAlertRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    @NotBlank(message = "作业负责人ID不能为空")
    private String operatorId;

    private String operatorName;

    private String alertLiftRemark;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
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

    public String getAlertLiftRemark() {
        return alertLiftRemark;
    }

    public void setAlertLiftRemark(String alertLiftRemark) {
        this.alertLiftRemark = alertLiftRemark;
    }
}

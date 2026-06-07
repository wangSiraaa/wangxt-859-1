package com.grain.fumigation.dto;

import javax.validation.constraints.NotNull;

public class SubmitTicketRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    private String operatorId;

    private String operatorName;

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
}

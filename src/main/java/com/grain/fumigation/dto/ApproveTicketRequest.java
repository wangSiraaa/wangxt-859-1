package com.grain.fumigation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ApproveTicketRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    @NotBlank(message = "审批人ID不能为空")
    private String operatorId;

    private String operatorName;

    private String approveRemark;

    private String rejectReason;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
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

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}

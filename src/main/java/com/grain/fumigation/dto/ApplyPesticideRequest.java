package com.grain.fumigation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ApplyPesticideRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    @NotBlank(message = "药剂类型不能为空")
    private String pesticideType;

    @NotNull(message = "药剂用量不能为空")
    private BigDecimal pesticideDosage;

    @NotBlank(message = "作业负责人ID不能为空")
    private String operatorId;

    private String operatorName;

    private String pesticideRemark;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getPesticideType() {
        return pesticideType;
    }

    public void setPesticideType(String pesticideType) {
        this.pesticideType = pesticideType;
    }

    public BigDecimal getPesticideDosage() {
        return pesticideDosage;
    }

    public void setPesticideDosage(BigDecimal pesticideDosage) {
        this.pesticideDosage = pesticideDosage;
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

    public String getPesticideRemark() {
        return pesticideRemark;
    }

    public void setPesticideRemark(String pesticideRemark) {
        this.pesticideRemark = pesticideRemark;
    }
}

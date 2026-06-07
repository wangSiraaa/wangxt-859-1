package com.grain.fumigation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VentilationRequest {

    @NotNull(message = "作业票ID不能为空")
    private Long operationId;

    private LocalDateTime ventilationStartTime;

    private LocalDateTime ventilationEndTime;

    private BigDecimal ventilationDurationHours;

    @NotNull(message = "毒气浓度不能为空")
    private BigDecimal gasConcentration;

    @NotNull(message = "通风检测是否合格不能为空")
    private Boolean ventilationPassed;

    @NotBlank(message = "作业负责人ID不能为空")
    private String operatorId;

    private String operatorName;

    private String ventilationRemark;

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public LocalDateTime getVentilationStartTime() {
        return ventilationStartTime;
    }

    public void setVentilationStartTime(LocalDateTime ventilationStartTime) {
        this.ventilationStartTime = ventilationStartTime;
    }

    public LocalDateTime getVentilationEndTime() {
        return ventilationEndTime;
    }

    public void setVentilationEndTime(LocalDateTime ventilationEndTime) {
        this.ventilationEndTime = ventilationEndTime;
    }

    public BigDecimal getVentilationDurationHours() {
        return ventilationDurationHours;
    }

    public void setVentilationDurationHours(BigDecimal ventilationDurationHours) {
        this.ventilationDurationHours = ventilationDurationHours;
    }

    public BigDecimal getGasConcentration() {
        return gasConcentration;
    }

    public void setGasConcentration(BigDecimal gasConcentration) {
        this.gasConcentration = gasConcentration;
    }

    public Boolean getVentilationPassed() {
        return ventilationPassed;
    }

    public void setVentilationPassed(Boolean ventilationPassed) {
        this.ventilationPassed = ventilationPassed;
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

    public String getVentilationRemark() {
        return ventilationRemark;
    }

    public void setVentilationRemark(String ventilationRemark) {
        this.ventilationRemark = ventilationRemark;
    }
}

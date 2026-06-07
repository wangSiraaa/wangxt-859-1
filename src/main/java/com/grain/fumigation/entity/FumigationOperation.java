package com.grain.fumigation.entity;

import com.grain.fumigation.enums.OperationStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fumigation_operation")
public class FumigationOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", unique = true, nullable = false, length = 50)
    private String ticketNo;

    @Column(name = "warehouse_code", nullable = false, length = 50)
    private String warehouseCode;

    @Column(name = "warehouse_name", length = 100)
    private String warehouseName;

    @Column(name = "grain_type", nullable = false, length = 50)
    private String grainType;

    @Column(name = "grain_quantity", precision = 10, scale = 2)
    private BigDecimal grainQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OperationStatus status;

    @Column(name = "keeper_id", nullable = false, length = 50)
    private String keeperId;

    @Column(name = "keeper_name", length = 50)
    private String keeperName;

    @Column(name = "safety_officer_id", length = 50)
    private String safetyOfficerId;

    @Column(name = "safety_officer_name", length = 50)
    private String safetyOfficerName;

    @Column(name = "operation_manager_id", length = 50)
    private String operationManagerId;

    @Column(name = "operation_manager_name", length = 50)
    private String operationManagerName;

    @Column(name = "evacuation_confirmed")
    private Boolean evacuationConfirmed;

    @Column(name = "evacuation_confirm_time")
    private LocalDateTime evacuationConfirmTime;

    @Column(name = "evacuation_remark", length = 500)
    private String evacuationRemark;

    @Column(name = "pesticide_type", length = 50)
    private String pesticideType;

    @Column(name = "pesticide_dosage", precision = 10, scale = 2)
    private BigDecimal pesticideDosage;

    @Column(name = "pesticide_apply_time")
    private LocalDateTime pesticideApplyTime;

    @Column(name = "pesticide_remark", length = 500)
    private String pesticideRemark;

    @Column(name = "ventilation_start_time")
    private LocalDateTime ventilationStartTime;

    @Column(name = "ventilation_end_time")
    private LocalDateTime ventilationEndTime;

    @Column(name = "ventilation_duration_hours", precision = 5, scale = 1)
    private BigDecimal ventilationDurationHours;

    @Column(name = "gas_concentration", precision = 5, scale = 2)
    private BigDecimal gasConcentration;

    @Column(name = "ventilation_passed")
    private Boolean ventilationPassed;

    @Column(name = "ventilation_remark", length = 500)
    private String ventilationRemark;

    @Column(name = "alert_lifted")
    private Boolean alertLifted;

    @Column(name = "alert_lift_time")
    private LocalDateTime alertLiftTime;

    @Column(name = "alert_lift_remark", length = 500)
    private String alertLiftRemark;

    @Column(name = "approve_remark", length = 500)
    private String approveRemark;

    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getGrainQuantity() {
        return grainQuantity;
    }

    public void setGrainQuantity(BigDecimal grainQuantity) {
        this.grainQuantity = grainQuantity;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getKeeperId() {
        return keeperId;
    }

    public void setKeeperId(String keeperId) {
        this.keeperId = keeperId;
    }

    public String getKeeperName() {
        return keeperName;
    }

    public void setKeeperName(String keeperName) {
        this.keeperName = keeperName;
    }

    public String getSafetyOfficerId() {
        return safetyOfficerId;
    }

    public void setSafetyOfficerId(String safetyOfficerId) {
        this.safetyOfficerId = safetyOfficerId;
    }

    public String getSafetyOfficerName() {
        return safetyOfficerName;
    }

    public void setSafetyOfficerName(String safetyOfficerName) {
        this.safetyOfficerName = safetyOfficerName;
    }

    public String getOperationManagerId() {
        return operationManagerId;
    }

    public void setOperationManagerId(String operationManagerId) {
        this.operationManagerId = operationManagerId;
    }

    public String getOperationManagerName() {
        return operationManagerName;
    }

    public void setOperationManagerName(String operationManagerName) {
        this.operationManagerName = operationManagerName;
    }

    public Boolean getEvacuationConfirmed() {
        return evacuationConfirmed;
    }

    public void setEvacuationConfirmed(Boolean evacuationConfirmed) {
        this.evacuationConfirmed = evacuationConfirmed;
    }

    public LocalDateTime getEvacuationConfirmTime() {
        return evacuationConfirmTime;
    }

    public void setEvacuationConfirmTime(LocalDateTime evacuationConfirmTime) {
        this.evacuationConfirmTime = evacuationConfirmTime;
    }

    public String getEvacuationRemark() {
        return evacuationRemark;
    }

    public void setEvacuationRemark(String evacuationRemark) {
        this.evacuationRemark = evacuationRemark;
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

    public LocalDateTime getPesticideApplyTime() {
        return pesticideApplyTime;
    }

    public void setPesticideApplyTime(LocalDateTime pesticideApplyTime) {
        this.pesticideApplyTime = pesticideApplyTime;
    }

    public String getPesticideRemark() {
        return pesticideRemark;
    }

    public void setPesticideRemark(String pesticideRemark) {
        this.pesticideRemark = pesticideRemark;
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

    public String getVentilationRemark() {
        return ventilationRemark;
    }

    public void setVentilationRemark(String ventilationRemark) {
        this.ventilationRemark = ventilationRemark;
    }

    public Boolean getAlertLifted() {
        return alertLifted;
    }

    public void setAlertLifted(Boolean alertLifted) {
        this.alertLifted = alertLifted;
    }

    public LocalDateTime getAlertLiftTime() {
        return alertLiftTime;
    }

    public void setAlertLiftTime(LocalDateTime alertLiftTime) {
        this.alertLiftTime = alertLiftTime;
    }

    public String getAlertLiftRemark() {
        return alertLiftRemark;
    }

    public void setAlertLiftRemark(String alertLiftRemark) {
        this.alertLiftRemark = alertLiftRemark;
    }

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}

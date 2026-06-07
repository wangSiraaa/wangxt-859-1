package com.grain.fumigation.dto;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class CreateTicketRequest {

    @NotBlank(message = "仓房编码不能为空")
    private String warehouseCode;

    private String warehouseName;

    @NotBlank(message = "粮食品种不能为空")
    private String grainType;

    private BigDecimal grainQuantity;

    @NotBlank(message = "保管员ID不能为空")
    private String keeperId;

    private String keeperName;

    private String safetyOfficerId;

    private String safetyOfficerName;

    private String operationManagerId;

    private String operationManagerName;

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
}

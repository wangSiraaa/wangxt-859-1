package com.grain.fumigation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "fumigation")
public class FumigationConfig {

    private BigDecimal maxPesticideDosage = new BigDecimal("50");

    private BigDecimal requiredVentilationHours = new BigDecimal("4");

    private BigDecimal allowableGasConcentration = new BigDecimal("0.5");

    public BigDecimal getMaxPesticideDosage() {
        return maxPesticideDosage;
    }

    public void setMaxPesticideDosage(BigDecimal maxPesticideDosage) {
        this.maxPesticideDosage = maxPesticideDosage;
    }

    public BigDecimal getRequiredVentilationHours() {
        return requiredVentilationHours;
    }

    public void setRequiredVentilationHours(BigDecimal requiredVentilationHours) {
        this.requiredVentilationHours = requiredVentilationHours;
    }

    public BigDecimal getAllowableGasConcentration() {
        return allowableGasConcentration;
    }

    public void setAllowableGasConcentration(BigDecimal allowableGasConcentration) {
        this.allowableGasConcentration = allowableGasConcentration;
    }
}

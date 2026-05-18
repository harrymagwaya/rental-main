package com.xpro.rentalmain.rentalmain.model;

import java.math.BigDecimal;

public class RiskScore {

    private final BigDecimal successRate; // Your original "score" (Performance)
    private final BigDecimal probabilityOfDefault; // The inverse (Risk)
    private final RiskCategory category;

    public RiskScore(BigDecimal successRate, BigDecimal probabilityOfDefault, RiskCategory category) {
        this.successRate = successRate;
        this.probabilityOfDefault = probabilityOfDefault;
        this.category = category;
    }

    public BigDecimal getSuccessRate() {
        return successRate;
    }

    public BigDecimal getProbabilityOfDefault() {
        return probabilityOfDefault;
    }

    public RiskCategory getCategory() {
        return category;
    }
}
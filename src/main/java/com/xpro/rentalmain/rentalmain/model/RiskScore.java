package com.xpro.rentalmain.rentalmain.model;

import java.math.BigDecimal;

public class RiskScore {

    private BigDecimal score;
    private RiskCategory category;

    public RiskScore(BigDecimal score, RiskCategory category) {
        this.score = score;
        this.category = category;
    }

    public BigDecimal getScore() {
        return score;
    }

    public RiskCategory getCategory() {
        return category;
    }
}
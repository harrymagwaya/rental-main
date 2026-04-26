package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.util.Map;

// The request payload
public class WeightUpdateRequest {
    // Allows sending {"weights": {"RENT": 0.40, "PAYMENT": 0.30}}
    private Map<String, BigDecimal> weights;

    public Map<String, BigDecimal> getWeights() { return weights; }
    public void setWeights(Map<String, BigDecimal> weights) { this.weights = weights; }
}
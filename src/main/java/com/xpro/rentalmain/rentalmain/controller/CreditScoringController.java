package com.xpro.rentalmain.rentalmain.controller;

import com.xpro.rentalmain.rentalmain.entity.BehavioralFeatures;
import com.xpro.rentalmain.rentalmain.model.RiskScore;
import com.xpro.rentalmain.rentalmain.service.model.CreditRiskModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credit-scoring")
public class CreditScoringController {

    private final CreditRiskModel riskModel;

    public CreditScoringController(CreditRiskModel riskModel) {
        this.riskModel = riskModel;
    }

    // Expects JSON with user's behavioral features
    @PostMapping("/evaluate")
    public RiskScore evaluateRisk(@RequestBody BehavioralFeatures features) {

        // Delegates to the service layer. Returns the RiskScore object directly.
        // Spring will serialize this into JSON automatically.
        return riskModel.predict(features);
    }
}
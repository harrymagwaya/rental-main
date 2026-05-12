package com.xpro.rentalmain.rentalmain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xpro.rentalmain.rentalmain.model.FinancialCategory;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// What the user sends to "Attach" a record
public record FinancialRecordRequest(
        UUID tenantId,
        String txnId,
        FinancialCategory category,
        BigDecimal amount,

        @JsonProperty("transactionDate")
        LocalDateTime transactionDate,
        String referenceNote
) {}
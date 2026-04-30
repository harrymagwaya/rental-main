package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.FinancialCategory;
import com.xpro.rentalmain.rentalmain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialRecordResponse(
        UUID recordId,
        UUID tenantId,
        String txnId,
        FinancialCategory category,
        BigDecimal amount,
        LocalDateTime transactionDate,
        PaymentStatus status,
        String referenceNote
) {}
package com.xpro.rentalmain.rentalmain.dto;

import com.xpro.rentalmain.rentalmain.model.PaymentStatus;
import com.xpro.rentalmain.rentalmain.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalPaymentEventResponse(
        UUID id,
        UUID profileId,
        String unitNumber, // Flattened from PropertyUnit
        LocalDateTime dueDate,
        LocalDateTime paymentDate,
        BigDecimal expectedAmount,
        BigDecimal amountPaid,
        Integer daysLate,
        PaymentStatus status,
        PaymentMethod method,
        String transactionReference
) {}
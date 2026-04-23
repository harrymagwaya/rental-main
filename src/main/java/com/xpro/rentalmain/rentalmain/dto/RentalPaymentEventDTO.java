package com.xpro.rentalmain.rentalmain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RentalPaymentEventDTO {

    private UUID tenantId;
    private LocalDateTime paymentDate;
    private LocalDateTime dueDate;
    private BigDecimal amountPaid;
    private BigDecimal expectedAmount;
    private String paymentStatus;

}
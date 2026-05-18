package com.xpro.rentalmain.rentalmain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// For creating a new lease
@Data
public class RentalProfileCreateDTO {
    private UUID tenantId;
    private UUID unitId;
    private BigDecimal agreedRentAmount;
    private Integer rentDueDay;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
}
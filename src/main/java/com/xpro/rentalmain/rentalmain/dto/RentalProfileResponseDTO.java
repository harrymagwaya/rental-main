package com.xpro.rentalmain.rentalmain.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RentalProfileResponseDTO {
    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID unitId;
    private String unitNumber;
    private BigDecimal agreedRentAmount;
    private Integer rentDueDay;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private String status;
    private Integer totalLatePayments;
    private Integer totalPayments;
}
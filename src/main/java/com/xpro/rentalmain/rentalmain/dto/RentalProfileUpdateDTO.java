package com.xpro.rentalmain.rentalmain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// For updating an existing lease
@Data
public class RentalProfileUpdateDTO {
    private BigDecimal agreedRentAmount;
    private Integer rentDueDay;
    private LocalDate leaseEndDate;
    private String status;
}
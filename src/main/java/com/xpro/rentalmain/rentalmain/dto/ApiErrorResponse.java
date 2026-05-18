package com.xpro.rentalmain.rentalmain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiErrorResponse {
    private String message;     // High-level human-readable message
    private String details;     // Specific details with populated placeholders
    private int status;
    private String path;
    private LocalDateTime timestamp;
}
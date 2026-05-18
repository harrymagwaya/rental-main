package com.xpro.rentalmain.rentalmain.model;

public enum UserStatus {
    REGISTERED,   // Initial state (Account created, but maybe email not verified)
    PENDING,      // In progress (Email verified, but profile/KYC incomplete)
    ACTIVE,       // Fully operational
    SUSPENDED,    // Temporary block
    DELETED       // Logically removed
}

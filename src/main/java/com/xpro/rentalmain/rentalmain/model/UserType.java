package com.xpro.rentalmain.rentalmain.model;

public enum UserType {
    SYSTEM_ADMIN,   // Full access to the entire platform
    LOAN_ADMIN,     // Can approve/deny loans and view scoring
    LANDLORD,       // Can manage properties and units
    TENANT          // Can view their unit and apply for loans
}

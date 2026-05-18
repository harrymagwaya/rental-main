package com.xpro.rentalmain.rentalmain.model;

public enum RentStatus {
    PAID_CONFIRMED, // Landlord clicked "Confirm"
    PENDING,        // Tenant filled form, Landlord hasn't acted
    DEFAULTED,      // Tenant didn't pay/fill form by deadline
    REJECTED        // Landlord says the tenant is lying
}
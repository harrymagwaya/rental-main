package com.xpro.rentalmain.rentalmain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum RentalApp {
    XPRO_STORAGE_SERVICE("xpro-storage-service"),
    XPRO_WEBB_APP("xpro-webb-app"),
    XPRO_ADMIN_PORTAL("xpro-admin-portal"),
    XPRO_RENTAL_MOBILE_APP("xpro-user-mobile-app"),
    XPRO_LANDLORD_WEB_APP("xpro-landlord-web-app"),
    XPRO_LOAN_WEB_APP("xpro-loan-web-app");

    private final String value;

    RentalApp(String value) {
        this.value = value;
    }

    /**
     * @JsonValue ensures that when this enum is sent over an API (JSON),
     * it uses the kebab-case string instead of the uppercase name.
     */
    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Helper method to find an enum by its hyphenated string
     */
    public static RentalApp fromString(String text) {
        for (RentalApp app : RentalApp.values()) {
            if (app.value.equalsIgnoreCase(text)) {
                return app;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
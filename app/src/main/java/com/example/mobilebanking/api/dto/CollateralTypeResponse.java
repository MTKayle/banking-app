package com.example.mobilebanking.api.dto;

/**
 * Collateral Type Response DTO
 * Maps với response từ GET /api/mortgage/collateral-types
 */
public class CollateralTypeResponse {
    private String value;
    private String displayName;

    public CollateralTypeResponse() {
    }

    public CollateralTypeResponse(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    // Getters and Setters
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

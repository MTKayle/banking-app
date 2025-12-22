package com.example.mobilebanking.api.dto;

/**
 * Saving Rate Update Request DTO
 * Maps với UpdateSavingTermRateRequest từ backend
 * Dùng cho PUT /api/saving/terms/update-rate
 */
public class SavingRateUpdateRequest {
    private String termType; // ONE_MONTH, THREE_MONTHS, SIX_MONTHS, TWELVE_MONTHS, etc.
    private Double interestRate;

    public SavingRateUpdateRequest() {
    }

    public SavingRateUpdateRequest(String termType, Double interestRate) {
        this.termType = termType;
        this.interestRate = interestRate;
    }

    // Getters and Setters
    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }
}

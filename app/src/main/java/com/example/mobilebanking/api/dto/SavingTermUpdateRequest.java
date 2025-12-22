package com.example.mobilebanking.api.dto;

/**
 * DTO for Saving Term Update Rate Request
 */
public class SavingTermUpdateRequest {
    private String termType;
    private Double interestRate;

    public SavingTermUpdateRequest() {}

    public SavingTermUpdateRequest(String termType, Double interestRate) {
        this.termType = termType;
        this.interestRate = interestRate;
    }

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

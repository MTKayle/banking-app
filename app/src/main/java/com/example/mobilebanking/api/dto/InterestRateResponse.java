package com.example.mobilebanking.api.dto;

/**
 * Interest Rate Response DTO
 * Maps với MortgageInterestRate từ GET /api/mortgage/interest-rates
 */
public class InterestRateResponse {
    private Long rateId;
    private Integer minMonths;
    private Integer maxMonths;
    private Double interestRate;
    private String description;

    public InterestRateResponse() {
    }

    // Getters and Setters
    public Long getRateId() {
        return rateId;
    }

    public void setRateId(Long rateId) {
        this.rateId = rateId;
    }

    public Integer getMinMonths() {
        return minMonths;
    }

    public void setMinMonths(Integer minMonths) {
        this.minMonths = minMonths;
    }

    public Integer getMaxMonths() {
        return maxMonths;
    }

    public void setMaxMonths(Integer maxMonths) {
        this.maxMonths = maxMonths;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get display text for term range
     */
    public String getTermDisplay() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        if (maxMonths == null) {
            return "> " + minMonths + " tháng";
        }
        return minMonths + " - " + maxMonths + " tháng";
    }
}

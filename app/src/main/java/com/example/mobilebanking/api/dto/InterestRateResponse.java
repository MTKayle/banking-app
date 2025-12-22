package com.example.mobilebanking.api.dto;

/**
 * Interest Rate Response DTO
 * Maps với MortgageInterestRate từ GET /api/mortgage/interest-rates
 */
public class InterestRateResponse {
    private Long id;
    private Integer termMonths;
    private Double interestRate;
    private String description;

    public InterestRateResponse() {
    }

    public InterestRateResponse(Long id, Integer termMonths, Double interestRate, String description) {
        this.id = id;
        this.termMonths = termMonths;
        this.interestRate = interestRate;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
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
}

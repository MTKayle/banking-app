package com.example.mobilebanking.api.dto;

/**
 * Mortgage Approve Request DTO
 * Maps với ApproveMortgageRequest từ backend
 * Dùng cho POST /api/mortgage/approve
 * Input: { mortgageId, principalAmount, termMonths }
 */
public class MortgageApproveRequest {
    private Long mortgageId;
    private Long principalAmount;
    private Integer termMonths;

    public MortgageApproveRequest() {
    }

    public MortgageApproveRequest(Long mortgageId, Long principalAmount, Integer termMonths) {
        this.mortgageId = mortgageId;
        this.principalAmount = principalAmount;
        this.termMonths = termMonths;
    }

    // Getters and Setters
    public Long getMortgageId() {
        return mortgageId;
    }

    public void setMortgageId(Long mortgageId) {
        this.mortgageId = mortgageId;
    }

    public Long getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Long principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }
}

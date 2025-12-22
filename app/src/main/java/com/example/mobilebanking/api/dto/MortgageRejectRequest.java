package com.example.mobilebanking.api.dto;

/**
 * Mortgage Reject Request DTO
 * Maps với RejectMortgageRequest từ backend
 * Dùng cho POST /api/mortgage/reject
 */
public class MortgageRejectRequest {
    private Long mortgageId;
    private String rejectionReason;

    public MortgageRejectRequest() {
    }

    public MortgageRejectRequest(Long mortgageId, String rejectionReason) {
        this.mortgageId = mortgageId;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public Long getMortgageId() {
        return mortgageId;
    }

    public void setMortgageId(Long mortgageId) {
        this.mortgageId = mortgageId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}

package com.example.mobilebanking.api.dto;

/**
 * Withdraw Request DTO
 * Maps với WithdrawRequest từ backend
 * Dùng cho POST /api/payment/checking/withdraw
 */
public class WithdrawRequest {
    private Long userId;
    private Double amount;
    private String description;

    public WithdrawRequest() {
    }

    public WithdrawRequest(Long userId, Double amount, String description) {
        this.userId = userId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

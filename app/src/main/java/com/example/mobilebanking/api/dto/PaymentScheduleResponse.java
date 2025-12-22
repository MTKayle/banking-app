package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Payment Schedule Response DTO
 * Maps với PaymentScheduleResponse từ backend
 */
public class PaymentScheduleResponse {
    private Integer periodNumber;
    private String dueDate;
    
    // Backend trả về principalAmount, interestAmount
    @SerializedName("principalAmount")
    private Double principalAmount;
    
    @SerializedName("interestAmount")
    private Double interestAmount;
    
    private Double penaltyAmount;
    private Double remainingBalance;
    
    @SerializedName("paid")
    private Boolean isPaid;
    
    private String paidDate;
    private Boolean isOverdue;
    private Integer daysOverdue;
    private Boolean isCurrentPeriod;

    public PaymentScheduleResponse() {
    }

    // Getters and Setters
    public Integer getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(Integer periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Double getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(Double interestAmount) {
        this.interestAmount = interestAmount;
    }

    /**
     * Tính tổng thanh toán = gốc + lãi + phạt (nếu có)
     */
    public Double getTotalPayment() {
        double principal = principalAmount != null ? principalAmount : 0;
        double interest = interestAmount != null ? interestAmount : 0;
        double penalty = penaltyAmount != null ? penaltyAmount : 0;
        return principal + interest + penalty;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

    public Double getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(Double penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public Boolean getIsCurrentPeriod() {
        return isCurrentPeriod;
    }

    public void setIsCurrentPeriod(Boolean isCurrentPeriod) {
        this.isCurrentPeriod = isCurrentPeriod;
    }
}

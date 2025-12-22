package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Payment Schedule Response DTO
 * Maps với PaymentScheduleResponse từ backend
 */
public class PaymentScheduleResponse {
    private Long scheduleId;
    private Integer periodNumber;
    private String dueDate;
    private Double principalAmount;
    private Double interestAmount;
    private Double totalAmount;
    private Double penaltyAmount;
    private Double remainingBalance;
    private String status;
    private String paidDate;
    private Double paidAmount;
    private Integer overdueDays;
    
    // Backend trả về currentPeriod và overdue (không có prefix "is")
    private Boolean currentPeriod;
    private Boolean overdue;

    public PaymentScheduleResponse() {
    }

    // Getters and Setters
    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(Double penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public Boolean getCurrentPeriod() {
        return currentPeriod;
    }

    public void setCurrentPeriod(Boolean currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    // Helper methods
    public Boolean getIsPaid() {
        return "PAID".equals(status);
    }

    public Boolean getIsCurrentPeriod() {
        return Boolean.TRUE.equals(currentPeriod);
    }

    public Boolean getIsOverdue() {
        return Boolean.TRUE.equals(overdue);
    }
}

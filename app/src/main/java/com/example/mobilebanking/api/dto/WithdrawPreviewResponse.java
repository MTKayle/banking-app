package com.example.mobilebanking.api.dto;

/**
 * Withdraw Preview Response DTO
 * Response từ GET /api/saving/{savingBookNumber}/withdraw-preview
 */
public class WithdrawPreviewResponse {
    private String savingBookNumber;
    private Double principalAmount;
    private Double appliedInterestRate;
    private Double interestEarned;
    private Double totalAmount;
    private String openedDate;
    private String withdrawDate;
    private Integer daysHeld;
    private String message;
    private Boolean earlyWithdrawal;

    public WithdrawPreviewResponse() {
    }

    public String getSavingBookNumber() {
        return savingBookNumber;
    }

    public void setSavingBookNumber(String savingBookNumber) {
        this.savingBookNumber = savingBookNumber;
    }

    public Double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Double getAppliedInterestRate() {
        return appliedInterestRate;
    }

    public void setAppliedInterestRate(Double appliedInterestRate) {
        this.appliedInterestRate = appliedInterestRate;
    }

    public Double getInterestEarned() {
        return interestEarned;
    }

    public void setInterestEarned(Double interestEarned) {
        this.interestEarned = interestEarned;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(String openedDate) {
        this.openedDate = openedDate;
    }

    public String getWithdrawDate() {
        return withdrawDate;
    }

    public void setWithdrawDate(String withdrawDate) {
        this.withdrawDate = withdrawDate;
    }

    public Integer getDaysHeld() {
        return daysHeld;
    }

    public void setDaysHeld(Integer daysHeld) {
        this.daysHeld = daysHeld;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getEarlyWithdrawal() {
        return earlyWithdrawal;
    }

    public void setEarlyWithdrawal(Boolean earlyWithdrawal) {
        this.earlyWithdrawal = earlyWithdrawal;
    }
}

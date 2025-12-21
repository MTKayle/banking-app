package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for withdraw-confirm API
 */
public class WithdrawConfirmResponse {
    
    @SerializedName("savingBookNumber")
    private String savingBookNumber;
    
    @SerializedName("principalAmount")
    private Double principalAmount;
    
    @SerializedName("appliedInterestRate")
    private Double appliedInterestRate;
    
    @SerializedName("interestEarned")
    private Double interestEarned;
    
    @SerializedName("totalAmount")
    private Double totalAmount;
    
    @SerializedName("checkingAccountNumber")
    private String checkingAccountNumber;
    
    @SerializedName("newCheckingBalance")
    private Double newCheckingBalance;
    
    @SerializedName("openedDate")
    private String openedDate;
    
    @SerializedName("closedDate")
    private String closedDate;
    
    @SerializedName("daysHeld")
    private Integer daysHeld;
    
    @SerializedName("transactionCode")
    private String transactionCode;
    
    @SerializedName("message")
    private String message;

    // Getters and Setters
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

    public String getCheckingAccountNumber() {
        return checkingAccountNumber;
    }

    public void setCheckingAccountNumber(String checkingAccountNumber) {
        this.checkingAccountNumber = checkingAccountNumber;
    }

    public Double getNewCheckingBalance() {
        return newCheckingBalance;
    }

    public void setNewCheckingBalance(Double newCheckingBalance) {
        this.newCheckingBalance = newCheckingBalance;
    }

    public String getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(String openedDate) {
        this.openedDate = openedDate;
    }

    public String getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(String closedDate) {
        this.closedDate = closedDate;
    }

    public Integer getDaysHeld() {
        return daysHeld;
    }

    public void setDaysHeld(Integer daysHeld) {
        this.daysHeld = daysHeld;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

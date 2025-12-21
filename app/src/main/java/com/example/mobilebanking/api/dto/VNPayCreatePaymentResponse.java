package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

public class VNPayCreatePaymentResponse {
    
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("paymentUrl")
    private String paymentUrl;
    
    @SerializedName("txnRef")
    private String txnRef;
    
    @SerializedName("amount")
    private Long amount;
    
    @SerializedName("accountNumber")
    private String accountNumber;
    
    @SerializedName("createdAt")
    private String createdAt;

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getTxnRef() {
        return txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

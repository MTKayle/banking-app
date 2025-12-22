package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

public class QrScanResponse {
    @SerializedName("accountNumber")
    private String accountNumber;
    
    @SerializedName("accountHolderName")
    private String accountHolderName;
    
    @SerializedName("bankBin")
    private String bankBin;
    
    @SerializedName("bankCode")
    private String bankCode;
    
    @SerializedName("bankName")
    private String bankName;
    
    @SerializedName("amount")
    private Long amount;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("accountType")
    private String accountType;
    
    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getBankBin() {
        return bankBin;
    }
    
    public void setBankBin(String bankBin) {
        this.bankBin = bankBin;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}

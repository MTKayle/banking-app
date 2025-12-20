package com.example.mobilebanking.api.dto;

/**
 * Account Info Response
 * Maps với response từ /api/accounts/info/{accountNumber}
 */
public class AccountInfoResponse {
    private String accountNumber;
    private String accountHolderName;
    private String bankBin;
    private String bankCode;
    private String bankName;
    private Long userId;
    private String accountType; // checking, saving, mortgage

    public AccountInfoResponse() {
    }

    public AccountInfoResponse(String accountNumber, String accountHolderName, 
                              String bankBin, String bankCode, String bankName, 
                              Long userId, String accountType) {
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.bankBin = bankBin;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.userId = userId;
        this.accountType = accountType;
    }

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


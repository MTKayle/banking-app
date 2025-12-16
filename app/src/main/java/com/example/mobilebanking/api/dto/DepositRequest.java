package com.example.mobilebanking.api.dto;

import java.math.BigDecimal;

/**
 * Deposit Request DTO
 */
public class DepositRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String description;

    public DepositRequest() {
    }

    public DepositRequest(String accountNumber, BigDecimal amount, String description) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.description = description;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}



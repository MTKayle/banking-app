package com.example.mobilebanking.api.dto;

/**
 * Create Saving Request DTO
 * Request body cho POST /api/saving/create
 */
public class CreateSavingRequest {
    private String senderAccountNumber;
    private Double amount;
    private String term;

    public CreateSavingRequest() {
    }

    public CreateSavingRequest(String senderAccountNumber, Double amount, String term) {
        this.senderAccountNumber = senderAccountNumber;
        this.amount = amount;
        this.term = term;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}


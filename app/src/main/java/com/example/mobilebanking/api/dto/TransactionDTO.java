package com.example.mobilebanking.api.dto;

import java.io.Serializable;

/**
 * Transaction Data Transfer Object
 * Maps với response từ /api/transactions/my-transactions
 */
public class TransactionDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long transactionId;
    private String code;
    private String senderAccountNumber;
    private String senderAccountName;
    private String receiverAccountNumber;
    private String receiverAccountName;
    private Double amount;
    private String transactionType; // TRANSFER, DEPOSIT, WITHDRAW
    private String description;
    private String status; // SUCCESS
    private String createdAt; // ISO8601 format

    public TransactionDTO() {
    }

    public TransactionDTO(Long transactionId, String code, String senderAccountNumber, 
                         String senderAccountName, String receiverAccountNumber, 
                         String receiverAccountName, Double amount, String transactionType, 
                         String description, String status, String createdAt) {
        this.transactionId = transactionId;
        this.code = code;
        this.senderAccountNumber = senderAccountNumber;
        this.senderAccountName = senderAccountName;
        this.receiverAccountNumber = receiverAccountNumber;
        this.receiverAccountName = receiverAccountName;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public String getSenderAccountName() {
        return senderAccountName;
    }

    public void setSenderAccountName(String senderAccountName) {
        this.senderAccountName = senderAccountName;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public String getReceiverAccountName() {
        return receiverAccountName;
    }

    public void setReceiverAccountName(String receiverAccountName) {
        this.receiverAccountName = receiverAccountName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}


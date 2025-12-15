package com.example.mobilebanking.models;

import java.util.Date;

/**
 * Transaction model class representing a financial transaction
 */
public class Transaction {
    private String transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private double amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private Date transactionDate;
    private String description;
    private String referenceNumber;

    public enum TransactionType {
        TRANSFER,
        DEPOSIT,
        WITHDRAWAL,
        BILL_PAYMENT,
        MOBILE_TOPUP,
        TICKET_BOOKING,
        HOTEL_BOOKING,
        ECOMMERCE_PAYMENT
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public Transaction() {
        this.currency = "VND";
        this.status = TransactionStatus.PENDING;
        this.transactionDate = new Date();
    }

    public Transaction(String transactionId, String fromAccountNumber, String toAccountNumber,
                       double amount, TransactionType type, String description) {
        this.transactionId = transactionId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.currency = "VND";
        this.status = TransactionStatus.PENDING;
        this.transactionDate = new Date();
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}


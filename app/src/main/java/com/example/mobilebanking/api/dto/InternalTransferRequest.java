package com.example.mobilebanking.api.dto;

/**
 * Internal Transfer Request (HAT Bank to HAT Bank)
 * Request for /api/payment/transfer/initiate
 */
public class InternalTransferRequest {
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private double amount;
    private String description;

    public InternalTransferRequest() {}

    public InternalTransferRequest(String senderAccountNumber, String receiverAccountNumber, 
                                   double amount, String description) {
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.description = description;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

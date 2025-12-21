package com.example.mobilebanking.api.dto;

/**
 * External Transfer Request (HAT Bank to Other Bank)
 * Request for /api/external-transfer/initiate
 */
public class ExternalTransferRequest {
    private String senderAccountNumber;
    private String receiverBankBin;
    private String receiverAccountNumber;
    private String receiverName;
    private double amount;
    private String description;

    public ExternalTransferRequest() {}

    public ExternalTransferRequest(String senderAccountNumber, String receiverBankBin,
                                   String receiverAccountNumber, String receiverName,
                                   double amount, String description) {
        this.senderAccountNumber = senderAccountNumber;
        this.receiverBankBin = receiverBankBin;
        this.receiverAccountNumber = receiverAccountNumber;
        this.receiverName = receiverName;
        this.amount = amount;
        this.description = description;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public String getReceiverBankBin() {
        return receiverBankBin;
    }

    public void setReceiverBankBin(String receiverBankBin) {
        this.receiverBankBin = receiverBankBin;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
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

package com.example.mobilebanking.api.dto;

/**
 * Transfer Confirm Response
 * Response for both internal and external transfer confirm
 */
public class TransferConfirmResponse {
    private Long transactionId;
    private String transactionCode;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private String receiverUserFullName;
    private Double amount;
    private String description;
    private Double senderNewBalance;
    private Double receiverNewBalance;
    private String transactionTime;
    private String status; // "SUCCESS", "FAILED", etc.
    
    // For external transfers (wrapped in data object)
    private boolean success;
    private String message;
    private Object data;

    public TransferConfirmResponse() {}

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
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

    public String getReceiverUserFullName() {
        return receiverUserFullName;
    }

    public void setReceiverUserFullName(String receiverUserFullName) {
        this.receiverUserFullName = receiverUserFullName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getSenderNewBalance() {
        return senderNewBalance;
    }

    public void setSenderNewBalance(Double senderNewBalance) {
        this.senderNewBalance = senderNewBalance;
    }

    public Double getReceiverNewBalance() {
        return receiverNewBalance;
    }

    public void setReceiverNewBalance(Double receiverNewBalance) {
        this.receiverNewBalance = receiverNewBalance;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccess() {
        // For internal transfers: check status field
        if (status != null) {
            return "SUCCESS".equalsIgnoreCase(status);
        }
        // For external transfers: check success field
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

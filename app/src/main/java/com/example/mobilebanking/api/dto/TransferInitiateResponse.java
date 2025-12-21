package com.example.mobilebanking.api.dto;

/**
 * Transfer Initiate Response
 * Response for internal transfer /api/payment/transfer/initiate
 */
public class TransferInitiateResponse {
    private String transactionCode;
    private String message;

    public TransferInitiateResponse() {}

    public TransferInitiateResponse(String transactionCode, String message) {
        this.transactionCode = transactionCode;
        this.message = message;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

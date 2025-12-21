package com.example.mobilebanking.api.dto;

/**
 * Transfer Confirm Request (Internal HAT to HAT)
 * Request for /api/payment/transfer/confirm
 */
public class TransferConfirmRequest {
    private String transactionCode;

    public TransferConfirmRequest() {}

    public TransferConfirmRequest(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }
}

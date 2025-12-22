package com.example.mobilebanking.api.dto;

/**
 * Request DTO for bill payment
 */
public class BillPaymentRequest {
    private String billCode;

    public BillPaymentRequest() {
    }

    public BillPaymentRequest(String billCode) {
        this.billCode = billCode;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }
}

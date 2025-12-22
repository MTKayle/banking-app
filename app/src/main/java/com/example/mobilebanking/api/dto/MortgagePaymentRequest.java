package com.example.mobilebanking.api.dto;

public class MortgagePaymentRequest {
    private Long mortgageId;
    private Double paymentAmount;
    private String paymentAccountNumber;

    public MortgagePaymentRequest() {
    }

    public MortgagePaymentRequest(Long mortgageId, Double paymentAmount, String paymentAccountNumber) {
        this.mortgageId = mortgageId;
        this.paymentAmount = paymentAmount;
        this.paymentAccountNumber = paymentAccountNumber;
    }

    public Long getMortgageId() {
        return mortgageId;
    }

    public void setMortgageId(Long mortgageId) {
        this.mortgageId = mortgageId;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentAccountNumber() {
        return paymentAccountNumber;
    }

    public void setPaymentAccountNumber(String paymentAccountNumber) {
        this.paymentAccountNumber = paymentAccountNumber;
    }
}

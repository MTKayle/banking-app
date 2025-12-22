package com.example.mobilebanking.api.dto;

/**
 * Mortgage Create Request DTO
 * Maps với CreateMortgageAccountRequest từ backend
 * Dùng cho POST /api/mortgage/create
 */
public class MortgageCreateRequest {
    private String phoneNumber;
    private String collateralType; // NHA, DAT, XE, etc.
    private String collateralDescription;
    private String paymentFrequency; // MONTHLY, QUARTERLY

    public MortgageCreateRequest() {
    }

    public MortgageCreateRequest(String phoneNumber, String collateralType, 
                                  String collateralDescription, String paymentFrequency) {
        this.phoneNumber = phoneNumber;
        this.collateralType = collateralType;
        this.collateralDescription = collateralDescription;
        this.paymentFrequency = paymentFrequency;
    }

    // Getters and Setters
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCollateralType() {
        return collateralType;
    }

    public void setCollateralType(String collateralType) {
        this.collateralType = collateralType;
    }

    public String getCollateralDescription() {
        return collateralDescription;
    }

    public void setCollateralDescription(String collateralDescription) {
        this.collateralDescription = collateralDescription;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }
}

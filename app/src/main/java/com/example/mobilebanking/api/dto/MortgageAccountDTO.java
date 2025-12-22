package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Mortgage Account Data Transfer Object
 * Maps với response từ /api/mortgage/user/{userId}
 */
public class MortgageAccountDTO {
    private Long mortgageId;
    private String accountNumber;
    private String customerName;
    private String customerPhone;
    private Double principalAmount;
    private Double interestRate;
    private Integer termMonths;
    private String startDate;
    private String status; // PENDING_APPRAISAL, ACTIVE, REJECTED, COMPLETED
    private String collateralType; // HOUSE, CAR, LAND
    private String collateralDescription;
    private String cccdFrontUrl;
    private String cccdBackUrl;
    private String collateralDocumentUrls;
    private String paymentFrequency;
    private String rejectionReason;
    private String createdDate;
    private String approvalDate;
    private Double remainingBalance;
    private Double earlySettlementAmount;
    private List<PaymentScheduleDTO> paymentSchedules;

    public MortgageAccountDTO() {
    }

    // Getters and Setters
    public Long getMortgageId() {
        return mortgageId;
    }

    public void setMortgageId(Long mortgageId) {
        this.mortgageId = mortgageId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCccdFrontUrl() {
        return cccdFrontUrl;
    }

    public void setCccdFrontUrl(String cccdFrontUrl) {
        this.cccdFrontUrl = cccdFrontUrl;
    }

    public String getCccdBackUrl() {
        return cccdBackUrl;
    }

    public void setCccdBackUrl(String cccdBackUrl) {
        this.cccdBackUrl = cccdBackUrl;
    }

    public String getCollateralDocumentUrls() {
        return collateralDocumentUrls;
    }

    public void setCollateralDocumentUrls(String collateralDocumentUrls) {
        this.collateralDocumentUrls = collateralDocumentUrls;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Double getEarlySettlementAmount() {
        return earlySettlementAmount;
    }

    public void setEarlySettlementAmount(Double earlySettlementAmount) {
        this.earlySettlementAmount = earlySettlementAmount;
    }

    public List<PaymentScheduleDTO> getPaymentSchedules() {
        return paymentSchedules;
    }

    public void setPaymentSchedules(List<PaymentScheduleDTO> paymentSchedules) {
        this.paymentSchedules = paymentSchedules;
    }
}


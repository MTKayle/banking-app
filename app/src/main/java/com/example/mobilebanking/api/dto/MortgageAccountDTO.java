package com.example.mobilebanking.api.dto;

/**
 * Mortgage Account Data Transfer Object
 * Maps với response từ /api/mortgage/user/{userId}
 */
public class MortgageAccountDTO {
    private Long mortgageAccountId;
    private String mortgageAccountNumber;
    private Double loanAmount;
    private Double remainingBalance;
    private Integer termMonths;
    private Double interestRate;
    private String status; // ACTIVE, PENDING_APPRAISAL, COMPLETED
    private String collateralType; // NHA, DAT, XE
    private String startDate;

    public MortgageAccountDTO() {
    }

    public MortgageAccountDTO(Long mortgageAccountId, String mortgageAccountNumber, 
                             Double loanAmount, Double remainingBalance, 
                             Integer termMonths, Double interestRate, String status, 
                             String collateralType, String startDate) {
        this.mortgageAccountId = mortgageAccountId;
        this.mortgageAccountNumber = mortgageAccountNumber;
        this.loanAmount = loanAmount;
        this.remainingBalance = remainingBalance;
        this.termMonths = termMonths;
        this.interestRate = interestRate;
        this.status = status;
        this.collateralType = collateralType;
        this.startDate = startDate;
    }

    // Getters and Setters
    public Long getMortgageAccountId() {
        return mortgageAccountId;
    }

    public void setMortgageAccountId(Long mortgageAccountId) {
        this.mortgageAccountId = mortgageAccountId;
    }

    public String getMortgageAccountNumber() {
        return mortgageAccountNumber;
    }

    public void setMortgageAccountNumber(String mortgageAccountNumber) {
        this.mortgageAccountNumber = mortgageAccountNumber;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}


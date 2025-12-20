package com.example.mobilebanking.api.dto;

/**
 * Saving Account Data Transfer Object
 * Maps với response từ /api/saving/accounts/user/{userId}
 */
public class SavingAccountDTO {
    private Long savingAccountId;
    private String savingAccountNumber;
    private Double principalAmount;
    private String term; // TWELVE_MONTHS, SIX_MONTHS, etc
    private Double interestRate;
    private String status; // ACTIVE, CLOSED
    private String startDate;
    private String maturityDate;
    private Double estimatedInterest;

    public SavingAccountDTO() {
    }

    public SavingAccountDTO(Long savingAccountId, String savingAccountNumber, 
                           Double principalAmount, String term, Double interestRate, 
                           String status, String startDate, String maturityDate, 
                           Double estimatedInterest) {
        this.savingAccountId = savingAccountId;
        this.savingAccountNumber = savingAccountNumber;
        this.principalAmount = principalAmount;
        this.term = term;
        this.interestRate = interestRate;
        this.status = status;
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.estimatedInterest = estimatedInterest;
    }

    // Getters and Setters
    public Long getSavingAccountId() {
        return savingAccountId;
    }

    public void setSavingAccountId(Long savingAccountId) {
        this.savingAccountId = savingAccountId;
    }

    public String getSavingAccountNumber() {
        return savingAccountNumber;
    }

    public void setSavingAccountNumber(String savingAccountNumber) {
        this.savingAccountNumber = savingAccountNumber;
    }

    public Double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(String maturityDate) {
        this.maturityDate = maturityDate;
    }

    public Double getEstimatedInterest() {
        return estimatedInterest;
    }

    public void setEstimatedInterest(Double estimatedInterest) {
        this.estimatedInterest = estimatedInterest;
    }
}


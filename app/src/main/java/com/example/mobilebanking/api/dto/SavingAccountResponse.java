package com.example.mobilebanking.api.dto;

/**
 * DTO for Saving Account API Response
 * Endpoint: GET /api/saving/all
 */
public class SavingAccountResponse {
    private Long savingId;
    private String savingBookNumber;
    private String accountNumber;
    private Double balance;
    private String term;
    private Integer termMonths;
    private Double interestRate;
    private String openedDate;
    private String maturityDate;
    private String status;
    private Long userId;
    private String userFullName;

    // Default constructor
    public SavingAccountResponse() {}

    // Getters and Setters
    public Long getSavingId() {
        return savingId;
    }

    public void setSavingId(Long savingId) {
        this.savingId = savingId;
    }

    public String getSavingBookNumber() {
        return savingBookNumber;
    }

    public void setSavingBookNumber(String savingBookNumber) {
        this.savingBookNumber = savingBookNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
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

    public String getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(String openedDate) {
        this.openedDate = openedDate;
    }

    public String getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(String maturityDate) {
        this.maturityDate = maturityDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
}

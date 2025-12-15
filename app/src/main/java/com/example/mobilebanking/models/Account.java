package com.example.mobilebanking.models;

import java.util.Date;

/**
 * Account model class representing a bank account
 */
public class Account {
    private String accountId;
    private String accountNumber;
    private String userId;
    private AccountType type;
    private double balance;
    private String currency;
    private Date createdDate;
    private boolean isActive;
    
    // For Savings Account
    private double interestRate;
    private double monthlyProfit;
    
    // For Mortgage Account
    private double loanAmount;
    private double monthlyPayment;
    private int remainingMonths;

    public enum AccountType {
        CHECKING,
        SAVINGS,
        MORTGAGE
    }

    public Account() {
        this.currency = "VND";
        this.isActive = true;
    }

    public Account(String accountId, String accountNumber, String userId, 
                   AccountType type, double balance) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.type = type;
        this.balance = balance;
        this.currency = "VND";
        this.isActive = true;
        this.createdDate = new Date();
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getMonthlyProfit() {
        return monthlyProfit;
    }

    public void setMonthlyProfit(double monthlyProfit) {
        this.monthlyProfit = monthlyProfit;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public int getRemainingMonths() {
        return remainingMonths;
    }

    public void setRemainingMonths(int remainingMonths) {
        this.remainingMonths = remainingMonths;
    }
}


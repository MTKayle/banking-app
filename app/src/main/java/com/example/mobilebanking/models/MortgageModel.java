package com.example.mobilebanking.models;

/**
 * MortgageModel - Model cho mock data quản lý khoản vay (Officer)
 * Đơn giản hóa từ MortgageAccount entity trong backend
 */
public class MortgageModel {
    private Long mortgageId;
    private String accountNumber;
    private String customerName;
    private String customerPhone;
    private double principalAmount;
    private double interestRate;
    private int termMonths;
    private String status; // "PENDING_APPRAISAL", "ACTIVE", "COMPLETED", "REJECTED"
    private String createdDate;
    private String collateralType;
    private String collateralDescription;
    private double monthlyPayment;
    private String rejectionReason;

    // Constructor đầy đủ
    public MortgageModel(Long mortgageId, String accountNumber, String customerName, 
                        String customerPhone, double principalAmount, double interestRate,
                        int termMonths, String status, String createdDate, 
                        String collateralType, String collateralDescription, double monthlyPayment) {
        this.mortgageId = mortgageId;
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.status = status;
        this.createdDate = createdDate;
        this.collateralType = collateralType;
        this.collateralDescription = collateralDescription;
        this.monthlyPayment = monthlyPayment;
    }
    
    // Constructor đầy đủ với rejectionReason
    public MortgageModel(Long mortgageId, String accountNumber, String customerName, 
                        String customerPhone, double principalAmount, double interestRate,
                        int termMonths, String status, String createdDate, 
                        String collateralType, String collateralDescription, double monthlyPayment,
                        String rejectionReason) {
        this(mortgageId, accountNumber, customerName, customerPhone, principalAmount, interestRate,
             termMonths, status, createdDate, collateralType, collateralDescription, monthlyPayment);
        this.rejectionReason = rejectionReason;
    }

    // Constructor đơn giản
    public MortgageModel(Long mortgageId, String accountNumber, String customerName,
                        double principalAmount, String status, String createdDate) {
        this.mortgageId = mortgageId;
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.principalAmount = principalAmount;
        this.status = status;
        this.createdDate = createdDate;
        this.interestRate = 8.5;
        this.termMonths = 120;
        this.collateralType = "Nhà đất";
        this.monthlyPayment = 0;
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

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
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

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    /**
     * Lấy màu status badge
     */
    public int getStatusColor() {
        switch (status) {
            case "PENDING_APPRAISAL":
                return android.graphics.Color.parseColor("#FF9800"); // Orange
            case "ACTIVE":
                return android.graphics.Color.parseColor("#4CAF50"); // Green
            case "COMPLETED":
                return android.graphics.Color.parseColor("#2196F3"); // Blue
            case "REJECTED":
                return android.graphics.Color.parseColor("#F44336"); // Red
            default:
                return android.graphics.Color.GRAY;
        }
    }

    /**
     * Lấy text hiển thị status
     */
    public String getStatusText() {
        switch (status) {
            case "PENDING_APPRAISAL":
                return "Chờ duyệt";
            case "ACTIVE":
                return "Đang vay";
            case "COMPLETED":
                return "Hoàn thành";
            case "REJECTED":
                return "Từ chối";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "MortgageModel{" +
                "mortgageId=" + mortgageId +
                ", accountNumber='" + accountNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", principalAmount=" + principalAmount +
                ", status='" + status + '\'' +
                '}';
    }
}


package com.example.mobilebanking.api.dto;

import java.math.BigDecimal;

/**
 * Response DTO for bill search API
 */
public class BillSearchResponse {
    private Boolean success;
    private BillData data;
    private String message;

    public BillSearchResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public BillData getData() {
        return data;
    }

    public void setData(BillData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Bill data
     */
    public static class BillData {
        private Long billId;
        private String billCode;
        private String billType;
        private String billTypeDisplay;
        private String customerName;
        private String customerAddress;
        private String customerPhone;
        private String period;
        private Integer usageAmount;
        private Integer oldIndex;
        private Integer newIndex;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private BigDecimal vat;
        private BigDecimal totalAmount;
        private String issueDate;
        private String dueDate;
        private String status;
        private String statusDisplay;
        private String providerName;
        private String providerCode;
        private String notes;
        private Boolean overdue;

        public BillData() {
        }

        public Long getBillId() {
            return billId;
        }

        public void setBillId(Long billId) {
            this.billId = billId;
        }

        public String getBillCode() {
            return billCode;
        }

        public void setBillCode(String billCode) {
            this.billCode = billCode;
        }

        public String getBillType() {
            return billType;
        }

        public void setBillType(String billType) {
            this.billType = billType;
        }

        public String getBillTypeDisplay() {
            return billTypeDisplay;
        }

        public void setBillTypeDisplay(String billTypeDisplay) {
            this.billTypeDisplay = billTypeDisplay;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerAddress() {
            return customerAddress;
        }

        public void setCustomerAddress(String customerAddress) {
            this.customerAddress = customerAddress;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public Integer getUsageAmount() {
            return usageAmount;
        }

        public void setUsageAmount(Integer usageAmount) {
            this.usageAmount = usageAmount;
        }

        public Integer getOldIndex() {
            return oldIndex;
        }

        public void setOldIndex(Integer oldIndex) {
            this.oldIndex = oldIndex;
        }

        public Integer getNewIndex() {
            return newIndex;
        }

        public void setNewIndex(Integer newIndex) {
            this.newIndex = newIndex;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public BigDecimal getVat() {
            return vat;
        }

        public void setVat(BigDecimal vat) {
            this.vat = vat;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getIssueDate() {
            return issueDate;
        }

        public void setIssueDate(String issueDate) {
            this.issueDate = issueDate;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatusDisplay() {
            return statusDisplay;
        }

        public void setStatusDisplay(String statusDisplay) {
            this.statusDisplay = statusDisplay;
        }

        public String getProviderName() {
            return providerName;
        }

        public void setProviderName(String providerName) {
            this.providerName = providerName;
        }

        public String getProviderCode() {
            return providerCode;
        }

        public void setProviderCode(String providerCode) {
            this.providerCode = providerCode;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public Boolean getOverdue() {
            return overdue;
        }

        public void setOverdue(Boolean overdue) {
            this.overdue = overdue;
        }
    }
}

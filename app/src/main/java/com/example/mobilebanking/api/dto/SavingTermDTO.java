package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Saving Term DTO
 * Maps với response từ GET /api/saving/terms
 */
public class SavingTermDTO {
    @SerializedName("termId")
    private Long termId;
    
    @SerializedName("termType")
    private String termType;
    
    @SerializedName("months")
    private Integer months;
    
    @SerializedName("displayName")
    private String displayName;
    
    @SerializedName("interestRate")
    private Double interestRate;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("updatedBy")
    private String updatedBy;

    public SavingTermDTO() {
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    public Integer getMonths() {
        return months;
    }

    public void setMonths(Integer months) {
        this.months = months;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Lấy số tháng (sử dụng field months từ API)
     */
    public int getTermMonths() {
        return months != null ? months : 0;
    }
}


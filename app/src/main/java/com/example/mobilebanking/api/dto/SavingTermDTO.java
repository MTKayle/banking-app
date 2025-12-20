package com.example.mobilebanking.api.dto;

/**
 * Saving Term DTO
 * Maps với response từ GET /api/saving/terms
 */
public class SavingTermDTO {
    private Long termId;
    private String termType;
    private Double interestRate;
    private String updatedAt;
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
     * Lấy số tháng từ termType
     */
    public int getTermMonths() {
        switch (termType) {
            case "NON_TERM": return 0;
            case "ONE_MONTH": return 1;
            case "TWO_MONTHS": return 2;
            case "THREE_MONTHS": return 3;
            case "FOUR_MONTHS": return 4;
            case "FIVE_MONTHS": return 5;
            case "SIX_MONTHS": return 6;
            case "SEVEN_MONTHS": return 7;
            case "EIGHT_MONTHS": return 8;
            case "NINE_MONTHS": return 9;
            case "TWELVE_MONTHS": return 12;
            case "FIFTEEN_MONTHS": return 15;
            case "EIGHTEEN_MONTHS": return 18;
            case "TWENTY_FOUR_MONTHS": return 24;
            case "THIRTY_SIX_MONTHS": return 36;
            default: return 0;
        }
    }

    /**
     * Lấy tên hiển thị
     */
    public String getDisplayName() {
        int months = getTermMonths();
        if (months == 0) return "Không kỳ hạn";
        return String.format("%02d tháng", months);
    }
}


package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for QR code generation
 */
public class QRCodeRequest {
    @SerializedName("amount")
    private Long amount;
    
    @SerializedName("description")
    private String description;
    
    public QRCodeRequest() {
    }
    
    public QRCodeRequest(Long amount, String description) {
        this.amount = amount;
        this.description = description;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}

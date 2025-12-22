package com.example.mobilebanking.api.dto;

/**
 * DTO for updating user CCCD number
 * Used with PATCH /api/users/{userId}/cccd endpoint
 */
public class UpdateCccdRequest {
    
    private String cccdNumber;
    
    public UpdateCccdRequest() {
    }
    
    public UpdateCccdRequest(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }
    
    public String getCccdNumber() {
        return cccdNumber;
    }
    
    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }
}

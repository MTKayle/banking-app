package com.example.mobilebanking.api.dto;

/**
 * DTO for updating user phone number
 * Used with PATCH /api/users/{userId}/phone endpoint
 */
public class UpdatePhoneRequest {
    
    private String phone;
    
    public UpdatePhoneRequest() {
    }
    
    public UpdatePhoneRequest(String phone) {
        this.phone = phone;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}

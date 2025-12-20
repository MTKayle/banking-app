package com.example.mobilebanking.api.dto;

/**
 * Request DTO for Change Password API
 * POST /api/password/change
 */
public class ChangePasswordRequest {
    private String phone;
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String phone, String newPassword) {
        this.phone = phone;
        this.newPassword = newPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}


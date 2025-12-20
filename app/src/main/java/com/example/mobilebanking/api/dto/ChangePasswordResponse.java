package com.example.mobilebanking.api.dto;

/**
 * Response DTO for Change Password API
 */
public class ChangePasswordResponse {
    private boolean success;
    private String message;

    public ChangePasswordResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


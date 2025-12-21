package com.example.mobilebanking.api.dto;

/**
 * Response DTO for check-phone-exists API
 */
public class PhoneExistsResponse {
    private boolean exists;
    private String message;

    public PhoneExistsResponse() {
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

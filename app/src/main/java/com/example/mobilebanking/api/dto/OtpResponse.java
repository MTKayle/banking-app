package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Goixe247 OTP API
 * 
 * Example success response:
 * {
 *   "success": true,
 *   "message": "OTP sent successfully"
 * }
 * 
 * Example error response:
 * {
 *   "success": false,
 *   "message": "Invalid phone number"
 * }
 */
public class OtpResponse {
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    // Alternative field names (for compatibility)
    @SerializedName("status")
    private String status;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Check if the response indicates success
     * Supports both "success" boolean field and "status" string field
     */
    public boolean isSuccess() {
        if (success != null) {
            return success;
        }
        if (status != null) {
            return "success".equalsIgnoreCase(status);
        }
        return false;
    }
}

package com.example.mobilebanking.api;

/**
 * Response model for biometric registration
 */
public class BiometricResponse {
    private boolean success;
    private String message;
    private String biometricId;
    private String errorCode;
    
    public BiometricResponse() {
    }
    
    // Getters and Setters
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
    
    public String getBiometricId() {
        return biometricId;
    }
    
    public void setBiometricId(String biometricId) {
        this.biometricId = biometricId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}


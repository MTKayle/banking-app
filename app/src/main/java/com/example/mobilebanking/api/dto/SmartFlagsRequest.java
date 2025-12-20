package com.example.mobilebanking.api.dto;

/**
 * Request DTO for updating user smart flags (fingerprint, face recognition, etc.)
 */
public class SmartFlagsRequest {
    private Boolean smartEkycEnabled;
    private Boolean faceRecognitionEnabled;
    private Boolean fingerprintLoginEnabled;

    public SmartFlagsRequest() {
    }

    public SmartFlagsRequest(Boolean fingerprintLoginEnabled) {
        this.fingerprintLoginEnabled = fingerprintLoginEnabled;
    }

    public Boolean getSmartEkycEnabled() {
        return smartEkycEnabled;
    }

    public void setSmartEkycEnabled(Boolean smartEkycEnabled) {
        this.smartEkycEnabled = smartEkycEnabled;
    }

    public Boolean getFaceRecognitionEnabled() {
        return faceRecognitionEnabled;
    }

    public void setFaceRecognitionEnabled(Boolean faceRecognitionEnabled) {
        this.faceRecognitionEnabled = faceRecognitionEnabled;
    }

    public Boolean getFingerprintLoginEnabled() {
        return fingerprintLoginEnabled;
    }

    public void setFingerprintLoginEnabled(Boolean fingerprintLoginEnabled) {
        this.fingerprintLoginEnabled = fingerprintLoginEnabled;
    }
}

package com.example.mobilebanking.api.dto;

/**
 * Face Compare Response DTO
 * Response from /api/face/compare
 */
public class FaceCompareResponse {
    private boolean success;
    private String message;
    private boolean matched;  // Changed from 'match' to 'matched'
    private Double confidence;

    public FaceCompareResponse() {}

    public FaceCompareResponse(boolean success, String message, boolean matched, Double confidence) {
        this.success = success;
        this.message = message;
        this.matched = matched;
        this.confidence = confidence;
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

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}

package com.example.mobilebanking.api.dto;

/**
 * API Error Response DTO
 */
public class ApiErrorResponse {
    private String message;
    private String error;
    private int status;

    public ApiErrorResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}



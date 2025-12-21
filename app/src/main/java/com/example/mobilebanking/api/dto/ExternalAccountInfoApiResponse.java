package com.example.mobilebanking.api.dto;

public class ExternalAccountInfoApiResponse {
    private boolean success;
    private String message;
    private ExternalAccountInfoResponse data;

    public ExternalAccountInfoApiResponse() {
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

    public ExternalAccountInfoResponse getData() {
        return data;
    }

    public void setData(ExternalAccountInfoResponse data) {
        this.data = data;
    }
}

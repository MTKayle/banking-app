package com.example.mobilebanking.utils;

/**
 * Basic response model for Goixe247 OTP API.
 * Example: {"status":"success","message":"Yêu cầu đã được ghi nhận."}
 */
public class OtpResponse {
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}



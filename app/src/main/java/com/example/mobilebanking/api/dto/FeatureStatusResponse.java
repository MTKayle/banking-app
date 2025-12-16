package com.example.mobilebanking.api.dto;

/**
 * DTO dùng chung cho các API kiểm tra trạng thái bật/tắt tính năng
 * Ví dụ: fingerprint login enabled, smart eKYC enabled, ...
 */
public class FeatureStatusResponse {
    private boolean enabled;

    public FeatureStatusResponse() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}




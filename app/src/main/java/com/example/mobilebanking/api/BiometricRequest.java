package com.example.mobilebanking.api;

/**
 * Request model for biometric registration
 */
public class BiometricRequest {
    private String imageBase64;
    private String timestamp;
    private String deviceInfo;
    private int imageWidth;
    private int imageHeight;
    private String userId; // Optional: if registering for existing user
    
    public BiometricRequest() {
    }
    
    public BiometricRequest(String imageBase64, String timestamp, String deviceInfo, 
                           int imageWidth, int imageHeight) {
        this.imageBase64 = imageBase64;
        this.timestamp = timestamp;
        this.deviceInfo = deviceInfo;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
    
    // Getters and Setters
    public String getImageBase64() {
        return imageBase64;
    }
    
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public int getImageWidth() {
        return imageWidth;
    }
    
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }
    
    public int getImageHeight() {
        return imageHeight;
    }
    
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}


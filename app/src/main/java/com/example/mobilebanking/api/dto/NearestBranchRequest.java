package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request cho API tìm chi nhánh gần nhất
 */
public class NearestBranchRequest {
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;
    
    // Constructors
    public NearestBranchRequest() {}
    
    public NearestBranchRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

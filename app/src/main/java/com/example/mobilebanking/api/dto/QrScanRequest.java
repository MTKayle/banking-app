package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

public class QrScanRequest {
    @SerializedName("qrContent")
    private String qrContent;
    
    public QrScanRequest(String qrContent) {
        this.qrContent = qrContent;
    }
    
    public String getQrContent() {
        return qrContent;
    }
    
    public void setQrContent(String qrContent) {
        this.qrContent = qrContent;
    }
}

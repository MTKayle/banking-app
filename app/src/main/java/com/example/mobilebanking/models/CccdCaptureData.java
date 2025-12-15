package com.example.mobilebanking.models;

import android.graphics.Bitmap;

/**
 * Data model for CCCD capture flow
 */
public class CccdCaptureData {
    private Bitmap frontImage;
    private Bitmap backImage;
    private Bitmap faceImage;
    private Bitmap portraitImage; // Extracted portrait from front CCCD
    private String frontImagePath;
    private String backImagePath;
    private String faceImagePath;
    private String portraitPath;
    
    public CccdCaptureData() {
    }
    
    // Front image
    public Bitmap getFrontImage() {
        return frontImage;
    }
    
    public void setFrontImage(Bitmap frontImage) {
        this.frontImage = frontImage;
    }
    
    public String getFrontImagePath() {
        return frontImagePath;
    }
    
    public void setFrontImagePath(String frontImagePath) {
        this.frontImagePath = frontImagePath;
    }
    
    // Back image
    public Bitmap getBackImage() {
        return backImage;
    }
    
    public void setBackImage(Bitmap backImage) {
        this.backImage = backImage;
    }
    
    public String getBackImagePath() {
        return backImagePath;
    }
    
    public void setBackImagePath(String backImagePath) {
        this.backImagePath = backImagePath;
    }
    
    // Face image
    public Bitmap getFaceImage() {
        return faceImage;
    }
    
    public void setFaceImage(Bitmap faceImage) {
        this.faceImage = faceImage;
    }
    
    public String getFaceImagePath() {
        return faceImagePath;
    }
    
    public void setFaceImagePath(String faceImagePath) {
        this.faceImagePath = faceImagePath;
    }
    
    // Portrait image (extracted from front CCCD)
    public Bitmap getPortraitImage() {
        return portraitImage;
    }
    
    public void setPortraitImage(Bitmap portraitImage) {
        this.portraitImage = portraitImage;
    }
    
    public String getPortraitPath() {
        return portraitPath;
    }
    
    public void setPortraitPath(String portraitPath) {
        this.portraitPath = portraitPath;
    }
    
    /**
     * Check if all images are captured
     */
    public boolean isComplete() {
        return frontImage != null && backImage != null && faceImage != null;
    }
}


package com.example.mobilebanking.models;

import android.graphics.Bitmap;

/**
 * Model to hold registration data across all steps
 */
public class RegistrationData {
    // Step 1: Basic Info
    private String phoneNumber;
    private String email;
    private String password;
    private String confirmPassword;
    
    // Step 2: CCCD QR Data
    private String fullName;
    private String idNumber;
    private String dateOfBirth;
    private String gender;
    private String permanentAddress;
    private String issueDate;
    
    // Step 3 & 4: CCCD Images
    private Bitmap frontCardImage;
    private Bitmap backCardImage;
    private Bitmap portraitImage; // Extracted from front card (saved silently)
    
    // Step 5: Face Verification
    private Bitmap selfieImage; // Selfie for face verification
    
    // Getters and Setters
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }
    
    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    
    public Bitmap getFrontCardImage() { return frontCardImage; }
    public void setFrontCardImage(Bitmap frontCardImage) { this.frontCardImage = frontCardImage; }
    
    public Bitmap getBackCardImage() { return backCardImage; }
    public void setBackCardImage(Bitmap backCardImage) { this.backCardImage = backCardImage; }
    
    public Bitmap getPortraitImage() { return portraitImage; }
    public void setPortraitImage(Bitmap portraitImage) { this.portraitImage = portraitImage; }
    
    public Bitmap getSelfieImage() { return selfieImage; }
    public void setSelfieImage(Bitmap selfieImage) { this.selfieImage = selfieImage; }
    
    /**
     * Check if step 1 data is complete
     */
    public boolean isStep1Complete() {
        return phoneNumber != null && !phoneNumber.isEmpty() &&
               email != null && !email.isEmpty() &&
               password != null && !password.isEmpty() &&
               confirmPassword != null && !confirmPassword.isEmpty() &&
               password.equals(confirmPassword);
    }
    
    /**
     * Check if step 2 data is complete
     */
    public boolean isStep2Complete() {
        return fullName != null && !fullName.isEmpty() &&
               idNumber != null && !idNumber.isEmpty();
    }
    
    /**
     * Check if step 3 data is complete
     */
    public boolean isStep3Complete() {
        return frontCardImage != null;
    }
    
    /**
     * Check if step 4 data is complete
     */
    public boolean isStep4Complete() {
        return backCardImage != null;
    }
    
    /**
     * Check if step 5 data is complete
     */
    public boolean isStep5Complete() {
        return selfieImage != null;
    }
}


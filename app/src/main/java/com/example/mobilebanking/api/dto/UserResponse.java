package com.example.mobilebanking.api.dto;

/**
 * Response DTO for user information
 * Matches backend UserResponse DTO
 */
public class UserResponse {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String cccdNumber;
    private String permanentAddress;
    private String temporaryAddress;
    private String photoUrl;
    private String role;
    private Boolean isLocked;
    private String createdAt;
    private String updatedAt;
    private Boolean smartEkycEnabled;
    private Boolean faceRecognitionEnabled;
    private Boolean fingerprintLoginEnabled;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getTemporaryAddress() {
        return temporaryAddress;
    }

    public void setTemporaryAddress(String temporaryAddress) {
        this.temporaryAddress = temporaryAddress;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getSmartEkycEnabled() {
        return smartEkycEnabled;
    }

    public void setSmartEkycEnabled(Boolean smartEkycEnabled) {
        this.smartEkycEnabled = smartEkycEnabled;
    }

    public Boolean getFaceRecognitionEnabled() {
        return faceRecognitionEnabled;
    }

    public void setFaceRecognitionEnabled(Boolean faceRecognitionEnabled) {
        this.faceRecognitionEnabled = faceRecognitionEnabled;
    }

    public Boolean getFingerprintLoginEnabled() {
        return fingerprintLoginEnabled;
    }

    public void setFingerprintLoginEnabled(Boolean fingerprintLoginEnabled) {
        this.fingerprintLoginEnabled = fingerprintLoginEnabled;
    }
}

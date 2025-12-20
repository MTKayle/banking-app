package com.example.mobilebanking.models;

/**
 * UserModel - Model cho mock data quản lý người dùng (Officer)
 * Đơn giản hóa từ User entity trong backend
 */
public class UserModel {
    private Long userId;
    private String phone;
    private String fullName;
    private String email;
    private String role; // "customer" hoặc "officer"
    private boolean isLocked;
    private String createdAt;
    private String cccdNumber;
    private String dateOfBirth;

    // Constructor đầy đủ
    public UserModel(Long userId, String phone, String fullName, String email, 
                     String role, boolean isLocked, String createdAt, 
                     String cccdNumber, String dateOfBirth) {
        this.userId = userId;
        this.phone = phone;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isLocked = isLocked;
        this.createdAt = createdAt;
        this.cccdNumber = cccdNumber;
        this.dateOfBirth = dateOfBirth;
    }

    // Constructor đơn giản
    public UserModel(Long userId, String phone, String fullName, String email, String role) {
        this.userId = userId;
        this.phone = phone;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isLocked = false;
        this.createdAt = "2024-01-15";
        this.cccdNumber = "";
        this.dateOfBirth = "";
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCccdNumber() {
        return cccdNumber;
    }

    public void setCccdNumber(String cccdNumber) {
        this.cccdNumber = cccdNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "userId=" + userId +
                ", phone='" + phone + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isLocked=" + isLocked +
                '}';
    }
}


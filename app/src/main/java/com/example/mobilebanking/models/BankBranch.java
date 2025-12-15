package com.example.mobilebanking.models;

/**
 * BankBranch model class representing a bank branch location
 */
public class BankBranch {
    private String branchId;
    private String branchName;
    private String address;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String workingHours;
    private boolean hasATM;

    public BankBranch() {
    }

    public BankBranch(String branchId, String branchName, String address, 
                      double latitude, double longitude, String phoneNumber) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.workingHours = "8:00 AM - 5:00 PM";
        this.hasATM = true;
    }

    // Getters and Setters
    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public boolean isHasATM() {
        return hasATM;
    }

    public void setHasATM(boolean hasATM) {
        this.hasATM = hasATM;
    }
}


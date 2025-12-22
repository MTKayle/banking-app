package com.example.mobilebanking.api.dto;

/**
 * Request DTO for updating user information
 * Used with PUT /api/users/{userId} endpoint
 * All fields are optional - only send fields that need to be updated
 */
public class UpdateUserRequest {
    private String fullName;
    private String email;
    private String dateOfBirth;
    private String permanentAddress;
    private String temporaryAddress;

    public UpdateUserRequest() {
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

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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
}

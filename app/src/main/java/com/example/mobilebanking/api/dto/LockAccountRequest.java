package com.example.mobilebanking.api.dto;

/**
 * Request DTO for locking/unlocking user account
 * Used with PATCH /api/users/{userId}/lock endpoint
 */
public class LockAccountRequest {
    private Boolean locked;

    public LockAccountRequest() {
    }

    public LockAccountRequest(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
}

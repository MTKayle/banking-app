package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.SmartFlagsRequest;
import com.example.mobilebanking.api.dto.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

/**
 * User Management API Service
 */
public interface UserApiService {
    
    /**
     * Update user smart flags (fingerprint, face recognition, etc.)
     * Endpoint: PATCH /users/{userId}/settings
     */
    @PATCH("users/{userId}/settings")
    Call<UserResponse> updateSmartFlags(
            @Path("userId") Long userId,
            @Body SmartFlagsRequest request
    );
}

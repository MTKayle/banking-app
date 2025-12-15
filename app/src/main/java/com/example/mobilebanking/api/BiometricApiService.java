package com.example.mobilebanking.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * API Service for Biometric Registration
 */
public interface BiometricApiService {
    
    @POST("/api/biometric/register")
    Call<BiometricResponse> registerBiometric(@Body BiometricRequest request);
}


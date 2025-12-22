package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.SavingAccountResponse;
import com.example.mobilebanking.api.dto.SavingTermListResponse;
import com.example.mobilebanking.api.dto.SavingTermResponse;
import com.example.mobilebanking.api.dto.SavingTermUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

/**
 * Saving API Service for Officer
 * Provides endpoints for saving account management operations
 */
public interface SavingApiService {

    /**
     * Get all saving accounts - Officer only
     * Endpoint: GET /saving/all
     */
    @GET("saving/all")
    Call<List<SavingAccountResponse>> getAllSavingAccounts();

    /**
     * Get all saving terms with interest rates
     * Endpoint: GET /saving/terms
     */
    @GET("saving/terms")
    Call<SavingTermListResponse> getSavingTerms();

    /**
     * Update saving term interest rate
     * Endpoint: PUT /saving/terms/update-rate
     */
    @PUT("saving/terms/update-rate")
    Call<SavingTermResponse> updateSavingTermRate(@Body SavingTermUpdateRequest request);
}

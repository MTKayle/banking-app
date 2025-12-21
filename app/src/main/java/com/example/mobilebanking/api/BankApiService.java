package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.BankListResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Bank API Service
 */
public interface BankApiService {
    
    /**
     * Lấy danh sách tất cả ngân hàng
     * GET /api/banks
     */
    @GET("banks")
    Call<BankListResponse> getAllBanks();
}

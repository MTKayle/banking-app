package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.ExternalAccountInfoApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * External Account API Service
 */
public interface ExternalAccountApiService {
    
    /**
     * Lấy thông tin tài khoản ngân hàng ngoài
     * GET /api/external-accounts/info?bankBin=xxx&accountNumber=xxx
     */
    @GET("external-accounts/info")
    Call<ExternalAccountInfoApiResponse> getAccountInfo(
        @Query("bankBin") String bankBin,
        @Query("accountNumber") String accountNumber
    );
}

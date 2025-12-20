package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.TransactionResponse;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Transaction API Service
 * Provides endpoints for transaction operations
 */
public interface TransactionApiService {
    
    /**
     * Lấy lịch sử giao dịch thành công của user hiện tại
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("transactions/my-transactions")
    Call<TransactionResponse> getMyTransactions();
}


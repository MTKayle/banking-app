package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.TransactionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
    
    /**
     * Lấy lịch sử giao dịch của một user cụ thể - Officer only
     * Header cần có: Authorization: Bearer {token}
     * Endpoint: GET /transactions/user/{userId}
     */
    @GET("transactions/user/{userId}")
    Call<TransactionResponse> getTransactionsByUser(@Path("userId") Long userId);
}


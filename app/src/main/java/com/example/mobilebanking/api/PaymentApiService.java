package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.DepositRequest;
import com.example.mobilebanking.api.dto.DepositResponse;
import com.example.mobilebanking.api.dto.WithdrawRequest;
import com.example.mobilebanking.api.dto.WithdrawResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Payment API Service
 */
public interface PaymentApiService {
    
    /**
     * Nạp tiền vào tài khoản checking
     * Header cần có: Authorization: Bearer {token}
     * Chỉ Officer/Admin mới có quyền
     */
    @POST("payment/checking/deposit")
    Call<DepositResponse> depositToChecking(@Body DepositRequest request);
    
    /**
     * Rút tiền từ tài khoản checking
     * Header cần có: Authorization: Bearer {token}
     * Chỉ Officer/Admin mới có quyền
     * Endpoint: POST /payment/checking/withdraw
     */
    @POST("payment/checking/withdraw")
    Call<WithdrawResponse> withdrawFromChecking(@Body WithdrawRequest request);
}



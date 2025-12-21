package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.VNPayCreatePaymentResponse;
import com.example.mobilebanking.api.dto.VNPayDepositRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * VNPay API Service for payment integration
 */
public interface VNPayApiService {
    
    /**
     * Create VNPay payment link
     * @param request Request body with amount
     * @return Payment response with payment URL
     */
    @POST("vnpay/create-payment")
    Call<VNPayCreatePaymentResponse> createPayment(@Body VNPayDepositRequest request);
}

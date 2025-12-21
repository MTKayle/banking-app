package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.ExternalTransferInitiateResponse;
import com.example.mobilebanking.api.dto.ExternalTransferRequest;
import com.example.mobilebanking.api.dto.InternalTransferRequest;
import com.example.mobilebanking.api.dto.TransferConfirmRequest;
import com.example.mobilebanking.api.dto.TransferConfirmResponse;
import com.example.mobilebanking.api.dto.TransferInitiateResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Transfer API Service
 * Handles internal and external transfers
 */
public interface TransferApiService {
    
    /**
     * Initiate internal transfer (HAT Bank to HAT Bank)
     * POST /api/payment/transfer/initiate
     */
    @POST("payment/transfer/initiate")
    Call<TransferInitiateResponse> initiateInternalTransfer(@Body InternalTransferRequest request);
    
    /**
     * Initiate external transfer (HAT Bank to Other Bank)
     * POST /api/external-transfer/initiate
     */
    @POST("external-transfer/initiate")
    Call<ExternalTransferInitiateResponse> initiateExternalTransfer(@Body ExternalTransferRequest request);
    
    /**
     * Confirm internal transfer (HAT Bank to HAT Bank)
     * POST /api/payment/transfer/confirm
     */
    @POST("payment/transfer/confirm")
    Call<TransferConfirmResponse> confirmInternalTransfer(@Body TransferConfirmRequest request);
    
    /**
     * Confirm external transfer (HAT Bank to Other Bank)
     * POST /api/external-transfer/confirm
     */
    @FormUrlEncoded
    @POST("external-transfer/confirm")
    Call<TransferConfirmResponse> confirmExternalTransfer(@Field("transactionCode") String transactionCode);
}

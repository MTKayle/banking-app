package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.QrScanRequest;
import com.example.mobilebanking.api.dto.QrScanResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * QR API Service
 */
public interface QrApiService {
    
    /**
     * Scan QR code and get payment information
     * POST /api/qr/scan
     */
    @POST("qr/scan")
    Call<QrScanResponse> scanQr(@Body QrScanRequest request);
}

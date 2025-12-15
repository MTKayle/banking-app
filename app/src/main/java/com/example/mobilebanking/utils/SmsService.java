package com.example.mobilebanking.utils;

import android.content.Context;
import android.util.Log;

import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.ESmsApiService;
import com.example.mobilebanking.api.ESmsRequest;
import com.example.mobilebanking.api.ESmsResponse;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service for sending OTP via eSMS API
 */
public class SmsService {
    private static final String TAG = "SmsService";
    
    private Context context;
    private ESmsConfig esmsConfig;
    private OtpManager otpManager;
    
    public interface SmsCallback {
        void onSuccess(String otpCode, String smsId);
        void onError(String errorMessage);
    }
    
    public SmsService(Context context) {
        this.context = context;
        this.esmsConfig = new ESmsConfig(context);
        this.otpManager = new OtpManager(context);
    }
    
    /**
     * Send OTP SMS to phone number
     * @param phoneNumber Phone number to send OTP (format: 0901234567)
     * @param callback Callback for result
     */
    public void sendOtp(String phoneNumber, SmsCallback callback) {
        // Generate OTP code
        String otpCode = otpManager.generateOtp();
        
        // Save OTP for verification
        otpManager.saveOtp(phoneNumber, otpCode);
        
        // Create request
        ESmsRequest request = new ESmsRequest();
        request.setApiKey(esmsConfig.getApiKey());
        request.setSecretKey(esmsConfig.getSecretKey());
        request.setPhone(phoneNumber);
        
        // Content must match the test template exactly (as per eSMS documentation)
        // Only CODE can be changed
        request.setContent(otpCode + " la ma xac minh dang ky Baotrixemay cua ban");
        
        request.setBrandname(esmsConfig.getBrandname());
        request.setSmsType("2"); // CSKH
        request.setIsUnicode("0"); // No unicode (no Vietnamese accents in test content)
        request.setSandbox(esmsConfig.isUseSandbox() ? "1" : "0"); // Use sandbox for testing
        request.setRequestId(UUID.randomUUID().toString());
        request.setCampaignid("OTP Verification");
        
        Log.d(TAG, "Sending OTP SMS to: " + phoneNumber);
        Log.d(TAG, "OTP Code: " + otpCode);
        Log.d(TAG, "Using Sandbox: " + esmsConfig.isUseSandbox());
        
        // Send request
        ESmsApiService apiService = ApiClient.getESmsApiService();
        Call<ESmsResponse> call = apiService.sendOtpSms(request);
        
        call.enqueue(new Callback<ESmsResponse>() {
            @Override
            public void onResponse(Call<ESmsResponse> call, Response<ESmsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ESmsResponse esmsResponse = response.body();
                    
                    if (esmsResponse.isSuccess()) {
                        Log.d(TAG, "SMS sent successfully. SMSID: " + esmsResponse.getSMSID());
                        if (callback != null) {
                            callback.onSuccess(otpCode, esmsResponse.getSMSID());
                        }
                    } else {
                        String errorMsg = "Lỗi gửi SMS: " + esmsResponse.getErrorMessage() + 
                                        " (Code: " + esmsResponse.getCodeResult() + ")";
                        Log.e(TAG, errorMsg);
                        if (callback != null) {
                            callback.onError(errorMsg);
                        }
                    }
                } else {
                    String errorMsg = "Lỗi kết nối API: " + response.message();
                    Log.e(TAG, errorMsg);
                    if (callback != null) {
                        callback.onError(errorMsg);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ESmsResponse> call, Throwable t) {
                String errorMsg = "Lỗi gửi SMS: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (callback != null) {
                    callback.onError(errorMsg);
                }
            }
        });
    }
    
    /**
     * Verify OTP code
     */
    public boolean verifyOtp(String phoneNumber, String otpCode) {
        return otpManager.verifyOtp(phoneNumber, otpCode);
    }
}


package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.FcmTokenRequest;
import com.example.mobilebanking.api.dto.FcmTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Notification API Service
 */
public interface NotificationApiService {
    
    /**
     * Đăng ký FCM token để nhận thông báo
     */
    @POST("notifications/register-token")
    Call<FcmTokenResponse> registerFcmToken(@Body FcmTokenRequest request);
}

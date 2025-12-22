package com.example.mobilebanking.utils;

import android.content.Context;
import android.util.Log;

import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.NotificationApiService;
import com.example.mobilebanking.api.dto.FcmTokenRequest;
import com.example.mobilebanking.api.dto.FcmTokenResponse;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager để xử lý FCM token
 */
public class FcmTokenManager {
    private static final String TAG = "FcmTokenManager";
    
    /**
     * Lấy FCM token và đăng ký với server
     */
    public static void registerFcmToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Lấy FCM token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Gửi token lên server
                    sendTokenToServer(context, token);
                });
    }

    /**
     * Gửi FCM token lên server
     */
    private static void sendTokenToServer(Context context, String token) {
        ApiClient.init(context);
        NotificationApiService apiService = ApiClient.getNotificationApiService();
        FcmTokenRequest request = new FcmTokenRequest(token);
        
        Call<FcmTokenResponse> call = apiService.registerFcmToken(request);
        call.enqueue(new Callback<FcmTokenResponse>() {
            @Override
            public void onResponse(Call<FcmTokenResponse> call, Response<FcmTokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Token registered successfully: " + response.body().getMessage());
                } else {
                    Log.e(TAG, "Failed to register token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<FcmTokenResponse> call, Throwable t) {
                Log.e(TAG, "Error registering token: " + t.getMessage());
            }
        });
    }
}

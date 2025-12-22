package com.example.mobilebanking.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.NotificationApiService;
import com.example.mobilebanking.api.dto.FcmTokenRequest;
import com.example.mobilebanking.api.dto.FcmTokenResponse;
import com.example.mobilebanking.ui_home.UiHomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Firebase Messaging Service để nhận và xử lý thông báo
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "banking_notifications";
    private static final String CHANNEL_NAME = "Banking Notifications";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        
        // Gửi token mới lên server
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Kiểm tra xem message có data payload không
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Kiểm tra xem message có notification payload không
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);
            
            // Hiển thị notification
            showNotification(title, body);
        }
    }

    /**
     * Gửi FCM token lên server
     */
    private void sendTokenToServer(String token) {
        ApiClient.init(this);
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

    /**
     * Hiển thị notification
     */
    private void showNotification(String title, String body) {
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo notification channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo từ ứng dụng ngân hàng");
            notificationManager.createNotificationChannel(channel);
        }

        // Tạo intent để mở app khi click vào notification
        Intent intent = new Intent(this, UiHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Dùng icon mặc định của Android
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify(0, notificationBuilder.build());
    }
}

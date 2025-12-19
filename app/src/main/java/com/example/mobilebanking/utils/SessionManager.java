package com.example.mobilebanking.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.example.mobilebanking.activities.LoginActivity;

/**
 * SessionManager - Quản lý session timeout và auto logout
 * 
 * Tính năng:
 * 1. Khi tắt app và mở lại → Phải đăng nhập lại
 * 2. Không thao tác quá 5 phút → Hiển thị popup yêu cầu đăng nhập lại
 */
public class SessionManager {
    private static SessionManager instance;
    private static final String PREF_NAME = "SessionPrefs";
    private static final String KEY_LAST_ACTIVITY_TIME = "last_activity_time";
    private static final String KEY_APP_IN_BACKGROUND = "app_in_background";
    private static final String KEY_SESSION_EXPIRED = "session_expired";
    
    // Timeout: 5 phút = 300,000 milliseconds
    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;
    
    private SharedPreferences sharedPreferences;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Context appContext;
    private boolean isSessionExpiredDialogShowing = false;
    
    private SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.timeoutHandler = new Handler(Looper.getMainLooper());
    }
    
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }
    
    /**
     * Đánh dấu app đang chạy foreground
     */
    public void onAppForeground() {
        sharedPreferences.edit()
            .putBoolean(KEY_APP_IN_BACKGROUND, false)
            .apply();
    }
    
    /**
     * Đánh dấu app đang chạy background
     */
    public void onAppBackground() {
        sharedPreferences.edit()
            .putBoolean(KEY_APP_IN_BACKGROUND, true)
            .putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis())
            .apply();
    }
    
    /**
     * Kiểm tra xem session có hết hạn không
     * @return true nếu session hết hạn (cần logout)
     */
    public boolean isSessionExpired() {
        // Kiểm tra flag session expired (đã được đánh dấu bởi timeout timer)
        boolean markedExpired = sharedPreferences.getBoolean(KEY_SESSION_EXPIRED, false);
        if (markedExpired) {
            return true;
        }
        
        boolean wasInBackground = sharedPreferences.getBoolean(KEY_APP_IN_BACKGROUND, false);
        
        // Nếu app vừa mở lại từ background → Phải đăng nhập lại
        if (wasInBackground) {
            markSessionExpired();
            return true;
        }
        
        // Kiểm tra timeout 5 phút
        long lastActivityTime = sharedPreferences.getLong(KEY_LAST_ACTIVITY_TIME, 0);
        if (lastActivityTime == 0) {
            return false; // Chưa có activity nào
        }
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastActivity = currentTime - lastActivityTime;
        
        if (timeSinceLastActivity > SESSION_TIMEOUT_MS) {
            markSessionExpired();
            return true;
        }
        
        return false;
    }
    
    /**
     * Đánh dấu session đã hết hạn
     */
    private void markSessionExpired() {
        sharedPreferences.edit()
            .putBoolean(KEY_SESSION_EXPIRED, true)
            .apply();
    }
    
    /**
     * Xóa flag session expired
     */
    private void clearSessionExpired() {
        sharedPreferences.edit()
            .putBoolean(KEY_SESSION_EXPIRED, false)
            .apply();
    }
    
    /**
     * Kiểm tra xem dialog session expired có đang hiển thị không
     */
    public boolean isSessionExpiredDialogShowing() {
        return isSessionExpiredDialogShowing;
    }
    
    /**
     * Set trạng thái dialog session expired
     */
    public void setSessionExpiredDialogShowing(boolean showing) {
        this.isSessionExpiredDialogShowing = showing;
    }
    
    /**
     * Cập nhật thời gian activity cuối cùng
     * Gọi mỗi khi user có tương tác với app
     */
    public void updateLastActivityTime() {
        sharedPreferences.edit()
            .putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis())
            .apply();
        
        // Reset timeout timer
        resetTimeoutTimer();
    }
    
    /**
     * Bắt đầu theo dõi timeout (5 phút không thao tác)
     */
    public void startTimeoutTimer(Activity activity) {
        // Hủy timer cũ nếu có
        stopTimeoutTimer();
        
        // Tạo timer mới
        timeoutRunnable = () -> {
            // Timeout → Đánh dấu session expired (popup sẽ hiện trong BaseActivity)
            markSessionExpired();
        };
        
        // Đặt timer 5 phút
        timeoutHandler.postDelayed(timeoutRunnable, SESSION_TIMEOUT_MS);
    }
    
    /**
     * Reset timeout timer (khi user có tương tác)
     */
    private void resetTimeoutTimer() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            // Không start lại timer ở đây, sẽ start trong onResume của Activity
        }
    }
    
    /**
     * Dừng timeout timer
     */
    public void stopTimeoutTimer() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }
    
    /**
     * Logout và quay về LoginActivity (activity_login_quick)
     */
    public void logout(Activity currentActivity) {
        // Clear session
        DataManager dataManager = DataManager.getInstance(appContext);
        dataManager.logout();
        
        // Clear session prefs
        sharedPreferences.edit().clear().apply();
        
        // Quay về LoginActivity
        Intent intent = new Intent(currentActivity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        currentActivity.startActivity(intent);
        currentActivity.finish();
    }
    
    /**
     * Reset session khi đăng nhập thành công
     */
    public void onLoginSuccess() {
        sharedPreferences.edit()
            .putBoolean(KEY_APP_IN_BACKGROUND, false)
            .putBoolean(KEY_SESSION_EXPIRED, false)
            .putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis())
            .apply();
        isSessionExpiredDialogShowing = false;
    }
}

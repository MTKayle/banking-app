package com.example.mobilebanking.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.mobilebanking.activities.LoginActivity;

/**
 * SessionManager - Quản lý session và auto logout
 * 
 * Tính năng:
 * 1. Khi tắt app và mở lại → Phải đăng nhập lại
 */
public class SessionManager {
    private static SessionManager instance;
    private static final String PREF_NAME = "SessionPrefs";
    private static final String KEY_APP_IN_BACKGROUND = "app_in_background";
    
    private SharedPreferences sharedPreferences;
    private Context appContext;
    
    private SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
            .apply();
    }
    
    /**
     * Kiểm tra xem session có hết hạn không
     * @return true nếu session hết hạn (cần logout)
     */
    public boolean isSessionExpired() {
        boolean wasInBackground = sharedPreferences.getBoolean(KEY_APP_IN_BACKGROUND, false);
        
        // Nếu app vừa mở lại từ background → Phải đăng nhập lại
        if (wasInBackground) {
            return true;
        }
        
        return false;
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
            .apply();
    }
}

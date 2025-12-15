package com.example.mobilebanking.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Configuration manager for eSMS API credentials
 * Store ApiKey and SecretKey securely
 */
public class ESmsConfig {
    private static final String PREF_NAME = "ESmsConfig";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_SECRET_KEY = "secret_key";
    private static final String KEY_BRANDNAME = "brandname";
    private static final String KEY_USE_SANDBOX = "use_sandbox";
    
    // Default values for testing (replace with your actual credentials)
    private static final String DEFAULT_API_KEY = "YOUR_API_KEY_HERE";
    private static final String DEFAULT_SECRET_KEY = "YOUR_SECRET_KEY_HERE";
    private static final String DEFAULT_BRANDNAME = "Baotrixemay"; // Test brandname from eSMS
    private static final boolean DEFAULT_USE_SANDBOX = true; // Use sandbox for testing
    
    private SharedPreferences sharedPreferences;
    
    public ESmsConfig(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public String getApiKey() {
        return sharedPreferences.getString(KEY_API_KEY, DEFAULT_API_KEY);
    }
    
    public void setApiKey(String apiKey) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply();
    }
    
    public String getSecretKey() {
        return sharedPreferences.getString(KEY_SECRET_KEY, DEFAULT_SECRET_KEY);
    }
    
    public void setSecretKey(String secretKey) {
        sharedPreferences.edit().putString(KEY_SECRET_KEY, secretKey).apply();
    }
    
    public String getBrandname() {
        return sharedPreferences.getString(KEY_BRANDNAME, DEFAULT_BRANDNAME);
    }
    
    public void setBrandname(String brandname) {
        sharedPreferences.edit().putString(KEY_BRANDNAME, brandname).apply();
    }
    
    public boolean isUseSandbox() {
        return sharedPreferences.getBoolean(KEY_USE_SANDBOX, DEFAULT_USE_SANDBOX);
    }
    
    public void setUseSandbox(boolean useSandbox) {
        sharedPreferences.edit().putBoolean(KEY_USE_SANDBOX, useSandbox).apply();
    }
    
    /**
     * Check if credentials are configured
     */
    public boolean isConfigured() {
        String apiKey = getApiKey();
        String secretKey = getSecretKey();
        return apiKey != null && !apiKey.equals(DEFAULT_API_KEY) && 
               secretKey != null && !secretKey.equals(DEFAULT_SECRET_KEY);
    }
}


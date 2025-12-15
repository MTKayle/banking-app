package com.example.mobilebanking.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Random;
import java.util.UUID;

/**
 * OTP Manager for generating, storing and verifying OTP codes
 */
public class OtpManager {
    private static final String TAG = "OtpManager";
    private static final String PREF_NAME = "OtpPrefs";
    private static final String KEY_OTP_CODE = "otp_code_";
    private static final String KEY_OTP_PHONE = "otp_phone_";
    private static final String KEY_OTP_EXPIRY = "otp_expiry_";
    private static final long OTP_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes
    
    private SharedPreferences sharedPreferences;
    
    public OtpManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Generate a 6-digit OTP code
     */
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(otp);
    }
    
    /**
     * Save OTP code with phone number and expiry time
     */
    public void saveOtp(String phoneNumber, String otpCode) {
        String requestId = UUID.randomUUID().toString();
        long expiryTime = System.currentTimeMillis() + OTP_EXPIRY_TIME;
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_OTP_CODE + requestId, otpCode);
        editor.putString(KEY_OTP_PHONE + requestId, phoneNumber);
        editor.putLong(KEY_OTP_EXPIRY + requestId, expiryTime);
        editor.apply();
        
        Log.d(TAG, "OTP saved for phone: " + phoneNumber + ", code: " + otpCode + ", expires at: " + expiryTime);
    }
    
    /**
     * Verify OTP code for a phone number
     */
    public boolean verifyOtp(String phoneNumber, String otpCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean verified = false;
        
        // Search through all saved OTPs
        for (String key : sharedPreferences.getAll().keySet()) {
            if (key.startsWith(KEY_OTP_CODE)) {
                String requestId = key.substring(KEY_OTP_CODE.length());
                String savedOtp = sharedPreferences.getString(key, null);
                String savedPhone = sharedPreferences.getString(KEY_OTP_PHONE + requestId, null);
                long expiryTime = sharedPreferences.getLong(KEY_OTP_EXPIRY + requestId, 0);
                
                // Check if expired
                if (System.currentTimeMillis() > expiryTime) {
                    // Remove expired OTP
                    editor.remove(KEY_OTP_CODE + requestId);
                    editor.remove(KEY_OTP_PHONE + requestId);
                    editor.remove(KEY_OTP_EXPIRY + requestId);
                    continue;
                }
                
                // Verify OTP
                if (phoneNumber.equals(savedPhone) && otpCode.equals(savedOtp)) {
                    verified = true;
                    // Remove used OTP
                    editor.remove(KEY_OTP_CODE + requestId);
                    editor.remove(KEY_OTP_PHONE + requestId);
                    editor.remove(KEY_OTP_EXPIRY + requestId);
                    Log.d(TAG, "OTP verified successfully for phone: " + phoneNumber);
                    break;
                }
            }
        }
        
        editor.apply();
        return verified;
    }
    
    /**
     * Check if OTP exists and is valid for a phone number
     */
    public boolean hasValidOtp(String phoneNumber) {
        for (String key : sharedPreferences.getAll().keySet()) {
            if (key.startsWith(KEY_OTP_PHONE)) {
                String requestId = key.substring(KEY_OTP_PHONE.length());
                String savedPhone = sharedPreferences.getString(key, null);
                long expiryTime = sharedPreferences.getLong(KEY_OTP_EXPIRY + requestId, 0);
                
                if (phoneNumber.equals(savedPhone) && System.currentTimeMillis() < expiryTime) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Clear all OTPs (for testing or cleanup)
     */
    public void clearAllOtps() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "All OTPs cleared");
    }
}


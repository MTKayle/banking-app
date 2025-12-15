package com.example.mobilebanking.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * BiometricAuthManager - Quản lý xác thực vân tay và lưu trữ token an toàn
 * 
 * Chức năng:
 * - Xác thực vân tay bằng BiometricPrompt
 * - Lưu refresh token vào Android Keystore (mã hóa)
 * - Chỉ giải mã được token khi người dùng quét vân tay
 * - App không bao giờ biết dữ liệu vân tay, chỉ biết "xác thực thành công"
 */
public class BiometricAuthManager {
    private static final String TAG = "BiometricAuthManager";
    private static final String KEYSTORE_ALIAS = "MobileBankingBiometricKey";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PREF_NAME = "BiometricPrefs";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_REFRESH_TOKEN_EXPIRY = "refresh_token_expiry";
    
    // Refresh token hết hạn sau 7 ngày
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;
    private static final long REFRESH_TOKEN_VALIDITY_MS = REFRESH_TOKEN_VALIDITY_DAYS * 24 * 60 * 60 * 1000L;
    
    private Context context;
    private SharedPreferences sharedPreferences;
    private Executor executor;
    
    public BiometricAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.executor = ContextCompat.getMainExecutor(context);
    }
    
    /**
     * Kiểm tra thiết bị có hỗ trợ vân tay không
     */
    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = androidx.biometric.BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
        );
        return canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
    }
    
    /**
     * Kiểm tra đã bật chức năng đăng nhập bằng vân tay chưa
     */
    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }
    
    /**
     * Bật chức năng đăng nhập bằng vân tay
     * Yêu cầu người dùng quét vân tay ngay lập tức
     */
    public void enableBiometric(FragmentActivity activity, BiometricAuthCallback callback) {
        if (!isBiometricAvailable()) {
            callback.onError("Thiết bị không hỗ trợ vân tay");
            return;
        }
        
        // Tạo key trong Keystore nếu chưa có
        if (!createKeyIfNeeded()) {
            callback.onError("Không thể khởi tạo bảo mật vân tay");
            return;
        }
        
        // Hiển thị BiometricPrompt để xác thực
        showBiometricPrompt(activity, "Xác thực dấu vân tay để bật tính năng đăng nhập sinh trắc học", 
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    // Lưu trạng thái đã bật
                    sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, true).apply();
                    callback.onSuccess();
                }
                
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    callback.onError("Xác thực thất bại: " + errString);
                }
                
                @Override
                public void onAuthenticationFailed() {
                    callback.onError("Vân tay không khớp");
                }
            });
    }
    
    /**
     * Tắt chức năng đăng nhập bằng vân tay
     * Xóa refresh token và trạng thái
     */
    public void disableBiometric() {
        sharedPreferences.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .remove(KEY_REFRESH_TOKEN_EXPIRY)
            .apply();
        
        // Xóa refresh token khỏi Keystore
        deleteRefreshToken();
    }
    
    /**
     * Lưu refresh token vào Keystore (mã hóa)
     * Chỉ có thể giải mã khi người dùng quét vân tay
     * @param username Username để lưu cùng với token (dùng khi đăng nhập bằng vân tay)
     */
    public void saveRefreshToken(FragmentActivity activity, String refreshToken, String username, BiometricAuthCallback callback) {
        if (!isBiometricEnabled()) {
            callback.onError("Chưa bật chức năng đăng nhập bằng vân tay");
            return;
        }
        
        // Tạo key nếu chưa có
        if (!createKeyIfNeeded()) {
            callback.onError("Không thể khởi tạo bảo mật vân tay");
            return;
        }
        
        // Yêu cầu xác thực vân tay để mã hóa và lưu token
        showBiometricPrompt(activity, "Xác thực dấu vân tay để lưu thông tin đăng nhập", 
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    try {
                        // Mã hóa và lưu token
                        Cipher cipher = getCipher();
                        SecretKey secretKey = getSecretKey();
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                        
                        byte[] encrypted = cipher.doFinal(refreshToken.getBytes("UTF-8"));
                        byte[] iv = cipher.getIV();
                        
                        // Lưu encrypted token và IV vào SharedPreferences
                        // (Trong production, nên lưu vào EncryptedSharedPreferences)
                        String encryptedToken = Base64.encodeToString(encrypted, Base64.DEFAULT);
                        String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
                        
                        long expiryTime = System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("encrypted_refresh_token", encryptedToken);
                        editor.putString("encrypted_refresh_token_iv", ivString);
                        editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, expiryTime);
                        if (username != null) {
                            editor.putString("biometric_username", username);
                        }
                        editor.apply();
                        
                        Log.d(TAG, "Refresh token saved successfully");
                        callback.onSuccess();
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving refresh token", e);
                        callback.onError("Không thể lưu thông tin đăng nhập: " + e.getMessage());
                    }
                }
                
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    callback.onError("Xác thực thất bại: " + errString);
                }
                
                @Override
                public void onAuthenticationFailed() {
                    callback.onError("Vân tay không khớp");
                }
            });
    }
    
    /**
     * Lấy refresh token từ Keystore hoặc temp storage (yêu cầu xác thực vân tay)
     */
    public void getRefreshToken(FragmentActivity activity, BiometricAuthCallback callback) {
        if (!isBiometricEnabled()) {
            callback.onError("Chưa bật chức năng đăng nhập bằng vân tay");
            return;
        }
        
        // Kiểm tra refresh token có hết hạn không
        long expiryTime = sharedPreferences.getLong(KEY_REFRESH_TOKEN_EXPIRY, 0);
        if (expiryTime == 0 || System.currentTimeMillis() > expiryTime) {
            callback.onError("Token đã hết hạn. Vui lòng đăng nhập bằng mật khẩu.");
            return;
        }
        
        // Kiểm tra có refresh token trong Keystore không
        String encryptedToken = sharedPreferences.getString("encrypted_refresh_token", null);
        String ivString = sharedPreferences.getString("encrypted_refresh_token_iv", null);
        
        // Nếu chưa có trong Keystore, kiểm tra temp token
        String tempToken = sharedPreferences.getString("temp_refresh_token", null);
        
        if (encryptedToken == null && tempToken == null) {
            callback.onError("Chưa có thông tin đăng nhập. Vui lòng đăng nhập bằng mật khẩu.");
            return;
        }
        
        // Yêu cầu xác thực vân tay
        showBiometricPrompt(activity, "Xác thực dấu vân tay để đăng nhập", 
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    try {
                        String refreshToken;
                        
                        // Nếu có token trong Keystore, giải mã
                        if (encryptedToken != null && ivString != null) {
                            byte[] encrypted = Base64.decode(encryptedToken, Base64.DEFAULT);
                            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
                            
                            Cipher cipher = getCipher();
                            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                            SecretKey secretKey = getSecretKey();
                            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
                            
                            byte[] decrypted = cipher.doFinal(encrypted);
                            refreshToken = new String(decrypted, "UTF-8");
                        } else if (tempToken != null) {
                            // Nếu chỉ có temp token, dùng trực tiếp và lưu vào Keystore
                            refreshToken = tempToken;
                            String username = sharedPreferences.getString("biometric_username", null);
                            
                            // Lưu vào Keystore với mã hóa (sử dụng session vân tay hiện tại)
                            if (username != null) {
                                try {
                                    // Tạo key nếu chưa có
                                    if (!createKeyIfNeeded()) {
                                        callback.onError("Không thể khởi tạo bảo mật vân tay");
                                        return;
                                    }
                                    
                                    // Mã hóa và lưu token (sử dụng session vân tay hiện tại)
                                    Cipher cipher = getCipher();
                                    SecretKey secretKey = getSecretKey();
                                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                                    
                                    byte[] encrypted = cipher.doFinal(refreshToken.getBytes("UTF-8"));
                                    byte[] iv = cipher.getIV();
                                    
                                    String encryptedTokenStr = Base64.encodeToString(encrypted, Base64.DEFAULT);
                                    String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
                                    
                                    long expiryTime = System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY_MS;
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("encrypted_refresh_token", encryptedTokenStr);
                                    editor.putString("encrypted_refresh_token_iv", ivString);
                                    editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, expiryTime);
                                    editor.putString("biometric_username", username);
                                    editor.remove("temp_refresh_token"); // Xóa temp token
                                    editor.apply();
                                    
                                    Log.d(TAG, "Temp refresh token saved to Keystore successfully");
                                } catch (Exception e) {
                                    Log.e(TAG, "Error saving temp token to Keystore", e);
                                    // Nếu không lưu được vào Keystore, vẫn dùng temp token
                                }
                            }
                        } else {
                            callback.onError("Không tìm thấy refresh token");
                            return;
                        }
                        
                        Log.d(TAG, "Refresh token retrieved successfully");
                        callback.onTokenRetrieved(refreshToken);
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving refresh token", e);
                        callback.onError("Không thể lấy thông tin đăng nhập: " + e.getMessage());
                    }
                }
                
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    callback.onError("Xác thực thất bại: " + errString);
                }
                
                @Override
                public void onAuthenticationFailed() {
                    callback.onError("Vân tay không khớp");
                }
            });
    }
    
    /**
     * Lấy username đã lưu cùng với refresh token
     */
    public String getBiometricUsername() {
        return sharedPreferences.getString("biometric_username", null);
    }
    
    /**
     * Kiểm tra có refresh token không (không cần xác thực)
     * Kiểm tra cả token trong Keystore và temp token
     */
    public boolean hasRefreshToken() {
        if (!isBiometricEnabled()) {
            return false;
        }
        
        long expiryTime = sharedPreferences.getLong(KEY_REFRESH_TOKEN_EXPIRY, 0);
        if (expiryTime == 0 || System.currentTimeMillis() > expiryTime) {
            return false;
        }
        
        // Kiểm tra token trong Keystore hoặc temp token
        String encryptedToken = sharedPreferences.getString("encrypted_refresh_token", null);
        String tempToken = sharedPreferences.getString("temp_refresh_token", null);
        return encryptedToken != null || tempToken != null;
    }
    
    /**
     * Hiển thị BiometricPrompt
     */
    private void showBiometricPrompt(FragmentActivity activity, String title, 
                                     BiometricPrompt.AuthenticationCallback callback) {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Sử dụng vân tay của bạn để xác thực")
            .setNegativeButtonText("Hủy")
            .build();
        
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, callback);
        biometricPrompt.authenticate(promptInfo);
    }
    
    /**
     * Tạo key trong Android Keystore nếu chưa có
     */
    private boolean createKeyIfNeeded() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true) // QUAN TRỌNG: Yêu cầu xác thực để sử dụng key
                    .setInvalidatedByBiometricEnrollment(true) // Vô hiệu hóa key nếu thêm/xóa vân tay mới
                    .build();
                
                keyGenerator.init(keyGenParameterSpec);
                keyGenerator.generateKey();
                Log.d(TAG, "Key created successfully");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating key", e);
            return false;
        }
    }
    
    /**
     * Lấy SecretKey từ Keystore
     */
    private SecretKey getSecretKey() throws KeyStoreException, CertificateException,
            NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
    }
    
    /**
     * Lấy Cipher để mã hóa/giải mã
     */
    private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance(TRANSFORMATION);
    }
    
    /**
     * Xóa refresh token khỏi Keystore
     */
    private void deleteRefreshToken() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                keyStore.deleteEntry(KEYSTORE_ALIAS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting key", e);
        }
        
        sharedPreferences.edit()
            .remove("encrypted_refresh_token")
            .remove("encrypted_refresh_token_iv")
            .remove("biometric_username")
            .apply();
    }
    
    /**
     * Callback interface cho các thao tác biometric
     */
    public interface BiometricAuthCallback {
        void onSuccess();
        void onError(String error);
        default void onTokenRetrieved(String refreshToken) {
            // Default implementation - override nếu cần
        }
    }
}

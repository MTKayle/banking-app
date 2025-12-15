package com.example.mobilebanking.utils;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Utility class for AES-256-GCM encryption/decryption of images
 */
public class ImageEncryptionUtil {
    private static final String TAG = "ImageEncryption";
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 12 bytes for GCM
    private static final int GCM_TAG_LENGTH = 16; // 16 bytes for GCM tag
    private static final int KEY_SIZE = 256; // AES-256
    
    // Default key (in production, this should be stored securely, e.g., in Android Keystore)
    // For demo purposes, using a fixed key. In production, generate and store securely.
    private static final String DEFAULT_KEY_STRING = "MobileBankingFaceImageEncryptionKey2024Secure!";
    
    /**
     * Generate a new AES-256 key
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            Log.e(TAG, "Error generating key", e);
            return null;
        }
    }
    
    /**
     * Get default key (for demo purposes)
     * In production, use Android Keystore to store keys securely
     */
    private static SecretKey getDefaultKey() {
        try {
            // Use first 32 bytes of the key string (AES-256 requires 32 bytes)
            byte[] keyBytes = DEFAULT_KEY_STRING.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[32];
            System.arraycopy(keyBytes, 0, key, 0, Math.min(keyBytes.length, 32));
            // Pad if necessary
            for (int i = keyBytes.length; i < 32; i++) {
                key[i] = 0;
            }
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            Log.e(TAG, "Error getting default key", e);
            return null;
        }
    }
    
    /**
     * Encrypt image file and save to encrypted file
     * @param inputFile Original image file
     * @param outputFile Encrypted output file
     * @return true if encryption successful
     */
    public static boolean encryptImageFile(File inputFile, File outputFile) {
        if (inputFile == null || !inputFile.exists() || outputFile == null) {
            Log.e(TAG, "Invalid input or output file");
            return false;
        }
        
        try {
            SecretKey key = getDefaultKey();
            if (key == null) {
                return false;
            }
            
            // Read image bytes
            FileInputStream fis = new FileInputStream(inputFile);
            byte[] imageBytes = new byte[(int) inputFile.length()];
            fis.read(imageBytes);
            fis.close();
            
            // Encrypt
            byte[] encrypted = encrypt(imageBytes, key);
            if (encrypted == null) {
                return false;
            }
            
            // Write encrypted data to file
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(encrypted);
            fos.flush();
            fos.close();
            
            Log.d(TAG, "Image encrypted successfully. Original: " + inputFile.length() + 
                  " bytes, Encrypted: " + outputFile.length() + " bytes");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting image file", e);
            return false;
        }
    }
    
    /**
     * Encrypt byte array
     * Format: [IV (12 bytes)][Encrypted Data][Tag (16 bytes)]
     */
    public static byte[] encrypt(byte[] plaintext, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Initialize cipher
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            
            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext);
            
            // Combine IV + ciphertext (which includes tag)
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            return byteBuffer.array();
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
            return null;
        }
    }
    
    /**
     * Decrypt byte array
     */
    public static byte[] decrypt(byte[] ciphertext, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
            // Extract IV
            ByteBuffer byteBuffer = ByteBuffer.wrap(ciphertext);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            
            // Extract encrypted data (includes tag)
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);
            
            // Initialize cipher
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            
            // Decrypt
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return null;
        }
    }
    
    /**
     * Decrypt image file
     */
    public static boolean decryptImageFile(File encryptedFile, File outputFile) {
        if (encryptedFile == null || !encryptedFile.exists() || outputFile == null) {
            Log.e(TAG, "Invalid input or output file");
            return false;
        }
        
        try {
            SecretKey key = getDefaultKey();
            if (key == null) {
                return false;
            }
            
            // Read encrypted bytes
            FileInputStream fis = new FileInputStream(encryptedFile);
            byte[] encryptedBytes = new byte[(int) encryptedFile.length()];
            fis.read(encryptedBytes);
            fis.close();
            
            // Decrypt
            byte[] decrypted = decrypt(encryptedBytes, key);
            if (decrypted == null) {
                return false;
            }
            
            // Write decrypted data to file
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(decrypted);
            fos.flush();
            fos.close();
            
            Log.d(TAG, "Image decrypted successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting image file", e);
            return false;
        }
    }
    
    /**
     * Encrypt and encode to Base64
     */
    public static String encryptAndEncodeBase64(byte[] imageBytes) {
        try {
            SecretKey key = getDefaultKey();
            if (key == null) {
                return null;
            }
            
            byte[] encrypted = encrypt(imageBytes, key);
            if (encrypted == null) {
                return null;
            }
            
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting and encoding to base64", e);
            return null;
        }
    }
}


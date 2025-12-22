package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.FeatureStatusResponse;
import com.example.mobilebanking.api.dto.LockAccountRequest;
import com.example.mobilebanking.api.dto.SmartFlagsRequest;
import com.example.mobilebanking.api.dto.UpdateCccdRequest;
import com.example.mobilebanking.api.dto.UpdatePhoneRequest;
import com.example.mobilebanking.api.dto.UpdateUserRequest;
import com.example.mobilebanking.api.dto.UserResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * User Management API Service
 */
public interface UserApiService {
    
    /**
     * Get all users - Only OFFICER can access
     * Endpoint: GET /users
     */
    @GET("users")
    Call<List<UserResponse>> getAllUsers();
    
    /**
     * Get user by ID - Only OFFICER can access
     * Endpoint: GET /users/{userId}
     */
    @GET("users/{userId}")
    Call<UserResponse> getUserById(@Path("userId") Long userId);
    
    /**
     * Update user smart flags (fingerprint, face recognition, etc.)
     * Endpoint: PATCH /users/{userId}/settings
     */
    @PATCH("users/{userId}/settings")
    Call<UserResponse> updateSmartFlags(
            @Path("userId") Long userId,
            @Body SmartFlagsRequest request
    );
    
    /**
     * Lock or unlock user account - Only OFFICER can access
     * Endpoint: PATCH /users/{userId}/lock
     * @param userId User ID to lock/unlock
     * @param request LockAccountRequest with locked status (true = lock, false = unlock)
     * @return Updated UserResponse with new isLocked status
     */
    @PATCH("users/{userId}/lock")
    Call<UserResponse> lockAccount(
            @Path("userId") Long userId,
            @Body LockAccountRequest request
    );
    
    /**
     * Update user information - Only OFFICER can access
     * Endpoint: PUT /users/{userId}
     * @param userId User ID to update
     * @param request UpdateUserRequest with fields to update (all fields optional)
     * @return Updated UserResponse with all current user information
     */
    @PUT("users/{userId}")
    Call<UserResponse> updateUser(
            @Path("userId") Long userId,
            @Body UpdateUserRequest request
    );
    
    /**
     * Update user phone number - Only OFFICER can access
     * Endpoint: PATCH /users/{userId}/phone
     * @param userId User ID to update
     * @param request UpdatePhoneRequest with new phone number
     * @return Response with success status and updated UserResponse
     */
    @PATCH("users/{userId}/phone")
    Call<Map<String, Object>> updatePhoneNumber(
            @Path("userId") Long userId,
            @Body UpdatePhoneRequest request
    );
    
    /**
     * Update user CCCD number - Only OFFICER can access
     * Endpoint: PATCH /users/{userId}/cccd
     * @param userId User ID to update
     * @param request UpdateCccdRequest with new CCCD number
     * @return Response with success status and updated UserResponse
     */
    @PATCH("users/{userId}/cccd")
    Call<Map<String, Object>> updateCccdNumber(
            @Path("userId") Long userId,
            @Body UpdateCccdRequest request
    );
    
    /**
     * Search user by phone number - Only OFFICER can access
     * Endpoint: GET /users/by-phone/{phone}
     * @param phone Phone number to search
     * @return UserResponse with user information
     */
    @GET("users/by-phone/{phone}")
    Call<UserResponse> getUserByPhone(@Path("phone") String phone);
    
    /**
     * Search user by CCCD number - Only OFFICER can access
     * Endpoint: GET /users/by-cccd/{cccdNumber}
     * @param cccdNumber CCCD number to search
     * @return UserResponse with user information
     */
    @GET("users/by-cccd/{cccdNumber}")
    Call<UserResponse> getUserByCccd(@Path("cccdNumber") String cccdNumber);
    
    /**
     * Update user profile photo - Only OFFICER can access
     * Endpoint: POST /users/{userId}/update-photo
     * Content-Type: multipart/form-data
     * @param userId User ID to update
     * @param photo Photo file
     * @return Response with success status
     */
    @Multipart
    @POST("users/{userId}/update-photo")
    Call<Map<String, Object>> updateUserPhoto(
            @Path("userId") Long userId,
            @Part MultipartBody.Part photo
    );
    
    /**
     * Check face recognition feature status
     * Endpoint: GET /users/{userId}/features/face-recognition
     * @param userId User ID to check
     * @return FeatureStatusResponse with enabled status
     */
    @GET("users/{userId}/features/face-recognition")
    Call<FeatureStatusResponse> checkFaceRecognition(@Path("userId") Long userId);
    
    /**
     * Check smart eKYC feature status
     * Endpoint: GET /users/{userId}/features/smart-ekyc
     * @param userId User ID to check
     * @return FeatureStatusResponse with enabled status
     */
    @GET("users/{userId}/features/smart-ekyc")
    Call<FeatureStatusResponse> checkSmartEkyc(@Path("userId") Long userId);
    
    /**
     * Check fingerprint login feature status
     * Endpoint: GET /users/{userId}/features/fingerprint-login
     * @param userId User ID to check
     * @return FeatureStatusResponse with enabled status
     */
    @GET("users/{userId}/features/fingerprint-login")
    Call<FeatureStatusResponse> checkFingerprintLogin(@Path("userId") Long userId);
}

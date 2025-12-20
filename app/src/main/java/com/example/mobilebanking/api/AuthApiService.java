package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.api.dto.ChangePasswordRequest;
import com.example.mobilebanking.api.dto.ChangePasswordResponse;
import com.example.mobilebanking.api.dto.LoginRequest;
import com.example.mobilebanking.api.dto.RegisterRequest;
import com.example.mobilebanking.api.dto.RefreshTokenRequest;
import com.example.mobilebanking.api.dto.FeatureStatusResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Authentication API Service
 */


public interface AuthApiService {
    
    /**
     * Đăng ký tài khoản (không xác thực khuôn mặt)
     */
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    /**
     * Đăng nhập bằng số điện thoại và mật khẩu
     */
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
    
    /**
     * Đăng ký với xác thực khuôn mặt
     * Gửi multipart/form-data với các field:
     * - phone, email, password, fullName, cccdNumber, dateOfBirth (optional), 
     *   permanentAddress (optional), temporaryAddress (optional)
     * - cccdPhoto (file)
     * - selfiePhoto (file)
     */
    @Multipart
    @POST("auth/register-with-face")
    Call<AuthResponse> registerWithFace(
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("fullName") RequestBody fullName,
            @Part("cccdNumber") RequestBody cccdNumber,
            @Part("dateOfBirth") RequestBody dateOfBirth,
            @Part("permanentAddress") RequestBody permanentAddress,
            @Part("temporaryAddress") RequestBody temporaryAddress,
            @Part MultipartBody.Part cccdPhoto,
            @Part MultipartBody.Part selfiePhoto
    );
    
    /**
     * Đăng nhập bằng khuôn mặt
     * Gửi multipart/form-data với:
     * - phone
     * - facePhoto (file)
     */
    @Multipart
    @POST("auth/login-with-face")
    Call<AuthResponse> loginWithFace(
            @Part("phone") RequestBody phone,
            @Part MultipartBody.Part facePhoto
    );

    /**
     * Refresh access token bằng refresh token
     */
    @POST("auth/refresh-token")
    Call<AuthResponse> refreshToken(@Body RefreshTokenRequest request);

    /**
     * Kiểm tra xem một số điện thoại có bật fingerprint login trên backend không
     * Dùng ở màn hình đăng nhập trước khi cho phép đăng nhập bằng vân tay
     */
    @GET("auth/check-fingerprint-enabled")
    Call<FeatureStatusResponse> checkFingerprintEnabled(@Query("phone") String phone);

    /**
     * Đổi mật khẩu (dùng cho Forgot Password flow)
     * Không cần authentication
     */
    @POST("password/change")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);
}



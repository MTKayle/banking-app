package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.OtpResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Service interface for Goixe247 OTP API
 * Base URL: https://otp.goixe247.com/
 */
public interface OtpApiService {

    /**
     * Gửi OTP đến số điện thoại
     * Endpoint: POST /api/otp/request
     */
    @FormUrlEncoded
    @POST("api/otp/request")
    Call<OtpResponse> requestOtp(
            @Field("user_id") String userId,
            @Field("api_key") String apiKey,
            @Field("phone") String phone
    );

    /**
     * Xác thực OTP
     * Endpoint: POST /api/otp/verify
     */
    @FormUrlEncoded
    @POST("api/otp/verify")
    Call<OtpResponse> verifyOtp(
            @Field("user_id") String userId,
            @Field("api_key") String apiKey,
            @Field("phone") String phone,
            @Field("otp") String otp
    );
}

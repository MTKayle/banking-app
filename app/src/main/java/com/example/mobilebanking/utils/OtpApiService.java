package com.example.mobilebanking.utils;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Service interface for Goixe247 OTP API.
 * Currently only supports requesting OTP via SMS.
 */
public interface OtpApiService {

    @FormUrlEncoded
    @POST("request_otp.php")
    Call<OtpResponse> requestOtp(
            @Field("user_id") String userId,
            @Field("api_key") String apiKey,
            @Field("recipient_phone") String phone
    );
}



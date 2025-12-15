package com.example.mobilebanking.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * eSMS API Service for sending OTP SMS
 */
public interface ESmsApiService {
    
    @POST("MainService.svc/json/SendMultipleMessage_V4_post_json/")
    Call<ESmsResponse> sendOtpSms(@Body ESmsRequest request);
}


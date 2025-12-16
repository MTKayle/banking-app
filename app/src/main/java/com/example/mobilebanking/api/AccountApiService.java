package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Account API Service
 */
public interface AccountApiService {
    
    /**
     * Lấy thông tin tài khoản checking theo userId
     * Header cần có: Authorization: Bearer {token}
     */
    @GET("accounts/{userId}/checking")
    Call<CheckingAccountInfoResponse> getCheckingAccountInfo(@Path("userId") Long userId);
}



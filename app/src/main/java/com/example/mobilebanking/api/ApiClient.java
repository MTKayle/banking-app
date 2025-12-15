package com.example.mobilebanking.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * API Client for Retrofit
 */
public class ApiClient {
    private static final String BASE_URL = "https://your-api-domain.com"; // TODO: Update with actual API URL
    private static final String ESMS_BASE_URL = "https://rest.esms.vn/"; // eSMS API base URL
    private static Retrofit retrofit;
    private static Retrofit esmsRetrofit;
    private static BiometricApiService biometricApiService;
    private static ESmsApiService esmsApiService;
    
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Create OkHttp client with timeout
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    public static BiometricApiService getBiometricApiService() {
        if (biometricApiService == null) {
            biometricApiService = getRetrofitInstance().create(BiometricApiService.class);
        }
        return biometricApiService;
    }
    
    public static Retrofit getESmsRetrofitInstance() {
        if (esmsRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            esmsRetrofit = new Retrofit.Builder()
                    .baseUrl(ESMS_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return esmsRetrofit;
    }
    
    public static ESmsApiService getESmsApiService() {
        if (esmsApiService == null) {
            esmsApiService = getESmsRetrofitInstance().create(ESmsApiService.class);
        }
        return esmsApiService;
    }
}


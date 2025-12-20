package com.example.mobilebanking.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobilebanking.utils.DataManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * API Client for Retrofit
 *
 * Cấu hình:
 * - BASE_URL: http://10.0.2.2:8089/api/ (cho Android Emulator)
 * - BASE_URL: http://YOUR_COMPUTER_IP:8089/api/ (cho thiết bị thật)
 *
 * Để tìm IP máy tính:
 * - Windows: ipconfig (tìm IPv4 Address)
 * - Mac/Linux: ifconfig hoặc ip addr
 *
 * LƯU Ý: Đổi IP_MÁY_TÍNH_CỦA_BẠN thành IP thật của máy tính chạy backend
 * Ví dụ: "http://192.168.1.100:8089/api/"
 */
public class ApiClient {
    // ============================================
    // CẤU HÌNH IP - ĐỔI IP Ở ĐÂY!
    // ============================================
    // IP máy tính của bạn (thay đổi giá trị này)
    // Khi dùng cùng Wi-Fi, IP là IP của máy tính trên mạng Wi-Fi
    // Chạy 'ipconfig' để tìm IP trong "Wireless LAN adapter Wi-Fi"
    private static final String IP_MÁY_TÍNH_CỦA_BẠN = "10.0.221.236"; // <-- IP Wi-Fi của máy tính

    // Base URL cho Android Emulator (10.0.2.2 là alias cho localhost của máy host)
    // Nếu chạy trên thiết bị thật, dùng IP máy tính của bạn
    private static final String BASE_URL_EMULATOR = "http://10.0.2.2:8089/api/";
    private static final String BASE_URL_DEVICE = "http://" + IP_MÁY_TÍNH_CỦA_BẠN + ":8089/api/";
    private static final String BASE_URL_USB = "http://localhost:8089/api/"; // Dùng khi kết nối USB + adb reverse
    // Ngrok tunnel (HTTPS) do user cung cấp
    private static final String BASE_URL_NGROK = " https://unbuffed-unindicatively-nada.ngrok-free.dev/api/";

    // Chọn phương thức kết nối:
    // - "USB": Dùng USB + adb reverse (ổn định nhất, không cần IP) - KHUYẾN NGHỊ!
    // - "WIFI": Dùng Wi-Fi với IP máy tính (cần cùng subnet)
    // - "EMULATOR": Dùng Android Emulator
    // - "NGROK": Dùng tunnel ngrok (HTTPS)
    // LƯU Ý: Nếu IP điện thoại và máy tính khác subnet (ví dụ: 10.0.220.x vs 10.0.221.x)
    //        → Dùng USB tethering hoặc kiểm tra router settings (AP Isolation)
    private static final String CONNECTION_MODE = "NGROK"; // "USB", "WIFI", "EMULATOR", hoặc "NGROK"

    private static final String BASE_URL;
    static {
        switch (CONNECTION_MODE) {
            case "USB":
                BASE_URL = BASE_URL_USB;
                break;
            case "EMULATOR":
                BASE_URL = BASE_URL_EMULATOR;
                break;
            case "NGROK":
                BASE_URL = BASE_URL_NGROK;
                break;
            case "WIFI":
            default:
                BASE_URL = BASE_URL_DEVICE;
                break;
        }
    }
    private static final String ESMS_BASE_URL = "https://rest.esms.vn/"; // eSMS API base URL

    private static Retrofit retrofit;
    private static Retrofit esmsRetrofit;
    private static AuthApiService authApiService;
    private static AccountApiService accountApiService;
    private static PaymentApiService paymentApiService;
    private static BiometricApiService biometricApiService;
    private static ESmsApiService esmsApiService;
    private static MovieApiService movieApiService;
    private static TransactionApiService transactionApiService;

    private static Context applicationContext;

    /**
     * Khởi tạo ApiClient với context
     * Nên gọi trong Application class hoặc Activity onCreate
     */
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    /**
     * Interceptor để tự động thêm JWT token vào header
     */
    private static Interceptor getAuthInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                // Lấy token từ DataManager
                String token = null;
                if (applicationContext != null) {
                    DataManager dataManager = DataManager.getInstance(applicationContext);
                    token = dataManager.getAccessToken();
                }

                // Nếu có token, thêm vào header
                Request.Builder requestBuilder = original.newBuilder();
                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                requestBuilder.header("Content-Type", "application/json");
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
    }

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create logging interceptor (chỉ log trong debug mode)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with timeout và auth interceptor
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(getAuthInterceptor())
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);

            OkHttpClient okHttpClient = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Lấy AuthApiService instance
     */
    public static AuthApiService getAuthApiService() {
        if (authApiService == null) {
            authApiService = getRetrofitInstance().create(AuthApiService.class);
        }
        return authApiService;
    }

    /**
     * Lấy AccountApiService instance
     */
    public static AccountApiService getAccountApiService() {
        if (accountApiService == null) {
            accountApiService = getRetrofitInstance().create(AccountApiService.class);
        }
        return accountApiService;
    }

    /**
     * Lấy PaymentApiService instance
     */
    public static PaymentApiService getPaymentApiService() {
        if (paymentApiService == null) {
            paymentApiService = getRetrofitInstance().create(PaymentApiService.class);
        }
        return paymentApiService;
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

    /**
     * Lấy MovieApiService instance
     */
    public static MovieApiService getMovieApiService() {
        if (movieApiService == null) {
            movieApiService = getRetrofitInstance().create(MovieApiService.class);
        }
        return movieApiService;
    }

    /**
     * Lấy TransactionApiService instance
     */
    public static TransactionApiService getTransactionApiService() {
        if (transactionApiService == null) {
            transactionApiService = getRetrofitInstance().create(TransactionApiService.class);
        }
        return transactionApiService;
    }

    /**
     * Reset Retrofit instance (dùng khi cần thay đổi BASE_URL)
     */
    public static void reset() {
        retrofit = null;
        authApiService = null;
        accountApiService = null;
        paymentApiService = null;
        biometricApiService = null;
        movieApiService = null;
        transactionApiService = null;
    }
}
package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.TransferApiService;
import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.api.dto.BookingRequest;
import com.example.mobilebanking.api.dto.BookingResponse;
import com.example.mobilebanking.api.dto.LoginRequest;
import com.example.mobilebanking.api.dto.TransferConfirmRequest;
import com.example.mobilebanking.api.dto.TransferConfirmResponse;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.utils.ESmsConfig;
import com.example.mobilebanking.utils.OtpApiService;
import com.example.mobilebanking.utils.OtpResponse;
import com.example.mobilebanking.utils.SessionManager;
import com.example.mobilebanking.utils.SmsService;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * OTP Verification Activity
 * Hỗ trợ nhiều flow:
 * - register: Đăng ký (eSMS)
 * - forgot_password: Quên mật khẩu (Goixe247)
 * - movie_booking: Đặt vé xem phim (Goixe247)
 * - login_verification: Xác thực đăng nhập tài khoản khác (Goixe247)
 */
public class OtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerification";
    
    // Goixe247 API configuration
    private static final String GOIXE_BASE_URL = "https://otp.goixe247.com/";
    private static final String GOIXE_USER_ID = "13";
    private static final String GOIXE_API_KEY = "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da";
    
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify, btnResend;
    private TextView tvPhone, tvTimer;
    private ProgressBar progressBar;
    private String phoneNumber;
    private String fromActivity;
    private String password; // For login_verification flow
    private CountDownTimer countDownTimer;
    
    private SmsService smsService;
    private ESmsConfig esmsConfig;
    private OtpApiService otpApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        phoneNumber = getIntent().getStringExtra("phone");
        fromActivity = getIntent().getStringExtra("from");
        
        Log.d(TAG, "Step 1 - phone: " + phoneNumber + ", from: " + fromActivity);
        
        // Lấy flow từ intent (ưu tiên "flow" và "verificationType" hơn "from")
        String flow = getIntent().getStringExtra("flow");
        if (flow != null && !flow.isEmpty()) {
            fromActivity = flow;
            Log.d(TAG, "Step 2 - Updated fromActivity from flow: " + fromActivity);
        }
        
        // Lấy verificationType (dùng cho SAVING, BILL_PAYMENT, etc.)
        String verificationType = getIntent().getStringExtra("verificationType");
        if (verificationType != null && !verificationType.isEmpty()) {
            fromActivity = verificationType;
            Log.d(TAG, "Step 3 - Updated fromActivity from verificationType: " + fromActivity);
        }
        
        // Lấy FROM_ACTIVITY (dùng cho MORTGAGE_PAYMENT, etc.)
        String fromActivityKey = getIntent().getStringExtra("FROM_ACTIVITY");
        Log.d(TAG, "Step 4 - FROM_ACTIVITY key value: " + fromActivityKey);
        if (fromActivityKey != null && !fromActivityKey.isEmpty()) {
            fromActivity = fromActivityKey;
            Log.d(TAG, "Step 5 - Updated fromActivity from FROM_ACTIVITY: " + fromActivity);
        }
        
        // Lấy PHONE_NUMBER nếu có
        String phoneNumberKey = getIntent().getStringExtra("PHONE_NUMBER");
        Log.d(TAG, "Step 6 - PHONE_NUMBER key value: " + phoneNumberKey);
        if (phoneNumberKey != null && !phoneNumberKey.isEmpty()) {
            phoneNumber = phoneNumberKey;
            Log.d(TAG, "Step 7 - Updated phoneNumber from PHONE_NUMBER: " + phoneNumber);
        }
        
        // Lấy password nếu là login_verification flow
        password = getIntent().getStringExtra("password");

        // Debug log
        Log.d(TAG, "FINAL - OTP Verification - fromActivity: " + fromActivity + ", phone: " + phoneNumber);

        // Initialize SMS service
        smsService = new SmsService(this);
        esmsConfig = new ESmsConfig(this);
        
        // Initialize Goixe247 service
        initGoixeService();

        initializeViews();
        setupOtpInputs();
        setupListeners();
        
        // Xử lý theo flow
        if ("forgot_password".equals(fromActivity)) {
            // Luồng quên mật khẩu - OTP đã được gửi từ ForgotPasswordActivity
            // Không cần gửi lại OTP ở đây
            Toast.makeText(this, "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_SHORT).show();
        } else if ("movie_booking".equals(fromActivity)) {
            // Luồng đặt vé - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("login_verification".equals(fromActivity)) {
            // Luồng xác thực đăng nhập - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("BILL_PAYMENT".equals(fromActivity)) {
            // Luồng thanh toán hóa đơn - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("SAVING".equals(fromActivity)) {
            // Luồng tạo sổ tiết kiệm - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("SAVING_WITHDRAW".equals(fromActivity)) {
            // Luồng rút tiền tiết kiệm - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("MORTGAGE_PAYMENT".equals(fromActivity)) {
            // Luồng thanh toán kỳ vay - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("MORTGAGE_SETTLEMENT".equals(fromActivity)) {
            // Luồng tất toán khoản vay - gửi OTP với Goixe247
            sendOtpWithGoixe();
        } else if ("register".equals(fromActivity)) {
            // Luồng đăng ký - dùng Goixe247
            sendOtpWithGoixe();
        } else {
            // Luồng mặc định - dùng Goixe247
            sendOtpWithGoixe();
        }
        
        startTimer();
    }
    
    private void initGoixeService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOIXE_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        otpApiService = retrofit.create(OtpApiService.class);
    }

    private void initializeViews() {
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        btnVerify = findViewById(R.id.btn_verify);
        btnResend = findViewById(R.id.btn_resend);
        tvPhone = findViewById(R.id.tv_phone);
        tvTimer = findViewById(R.id.tv_timer);
        progressBar = findViewById(R.id.progress_bar);

        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.GONE);
        }

        if (tvPhone != null && phoneNumber != null) {
            tvPhone.setText("Đã gửi đến " + phoneNumber);
        }
    }

    
    private void showApiKeyConfigDialog() {
        android.widget.EditText etApiKey = new android.widget.EditText(this);
        etApiKey.setHint("Nhập ApiKey");
        etApiKey.setText(esmsConfig.getApiKey());
        
        android.widget.EditText etSecretKey = new android.widget.EditText(this);
        etSecretKey.setHint("Nhập SecretKey");
        etSecretKey.setText(esmsConfig.getSecretKey());
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(etApiKey);
        layout.addView(etSecretKey);
        
        new AlertDialog.Builder(this)
                .setTitle("Cấu hình eSMS API")
                .setMessage("Vui lòng nhập ApiKey và SecretKey từ eSMS để gửi OTP thực tế.\n\n" +
                           "Nếu chưa có, bạn có thể:\n" +
                           "1. Đăng ký tại https://esms.vn/\n" +
                           "2. Hoặc bấm 'Bỏ qua' để dùng chế độ test (chấp nhận mọi mã 6 số)")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String apiKey = etApiKey.getText().toString().trim();
                    String secretKey = etSecretKey.getText().toString().trim();
                    
                    if (!apiKey.isEmpty() && !secretKey.isEmpty()) {
                        esmsConfig.setApiKey(apiKey);
                        esmsConfig.setSecretKey(secretKey);
                        sendOtp();
                    } else {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ ApiKey và SecretKey", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Bỏ qua", (dialog, which) -> {
                    Toast.makeText(this, "Chế độ test: Chấp nhận mọi mã 6 số", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }
    
    private void sendOtp() {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnResend.setEnabled(false);

        smsService.sendOtp(phoneNumber, new SmsService.SmsCallback() {
            @Override
            public void onSuccess(String otpCode, String smsId) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }

                    // For testing: show OTP in log (remove in production)
                    if (esmsConfig.isUseSandbox()) {
                        Log.d(TAG, "OTP Code (for testing): " + otpCode);
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Mã OTP (test): " + otpCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Đã gửi mã OTP thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }

                    // Show error dialog
                    new AlertDialog.Builder(OtpVerificationActivity.this)
                            .setTitle("Lỗi gửi OTP")
                            .setMessage(errorMessage + "\n\nBạn có muốn thử lại không?")
                            .setPositiveButton("Thử lại", (dialog, which) -> sendOtp())
                            .setNegativeButton("Bỏ qua", (dialog, which) -> {
                                Toast.makeText(OtpVerificationActivity.this, 
                                        "Chế độ test: Chấp nhận mọi mã 6 số", Toast.LENGTH_LONG).show();
                            })
                            .show();
                });
            }
        });
    }
    
    private void sendOtpWithGoixe() {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Sending OTP to: " + phoneNumber);
        
        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnResend.setEnabled(false);
        
        // Sử dụng đúng tên field: recipient_phone (theo API Goixe247)
        Call<OtpResponse> call = otpApiService.requestOtp(
                GOIXE_USER_ID,
                GOIXE_API_KEY,
                phoneNumber
        );
        
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    
                    Log.d(TAG, "OTP Request Response Code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        OtpResponse otpResponse = response.body();
                        Log.d(TAG, "OTP Response: success=" + otpResponse.isSuccess() + ", message=" + otpResponse.getMessage());
                        
                        if (otpResponse.isSuccess()) {
                            Toast.makeText(OtpVerificationActivity.this, 
                                    "Mã OTP đã được gửi đến " + phoneNumber, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, 
                                    "Lỗi: " + otpResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "OTP Request failed: " + response.code());
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Không thể gửi OTP. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    
                    Log.e(TAG, "OTP Request failed", t);
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupOtpInputs() {
        EditText[] otpInputs = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};
        
        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpInputs[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> handleOtpVerification());
        btnResend.setOnClickListener(v -> resendOtp());
    }

    private void handleOtpVerification() {
        String otp = etOtp1.getText().toString() + etOtp2.getText().toString() +
                     etOtp3.getText().toString() + etOtp4.getText().toString() +
                     etOtp5.getText().toString() + etOtp6.getText().toString();

        if (otp.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!otp.matches("\\d{6}")) {
            Toast.makeText(this, "Mã OTP không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tất cả các flow đều dùng Goixe247
        verifyOtpWithGoixe(otp);
    }
    
    private void verifyOtpWithESms(String otp) {
        // Verify OTP - Fake OTP is 123456
        boolean isValid = false;
        
        // Check if OTP is 123456 (fake OTP for testing)
        if ("123456".equals(otp)) {
            isValid = true;
            Log.d(TAG, "OTP verification successful with fake OTP: 123456");
        } else if (esmsConfig.isConfigured() && smsService != null) {
            // Verify with saved OTP from SMS service
            isValid = smsService.verifyOtp(phoneNumber, otp);
        }

        if (isValid) {
            Toast.makeText(this, "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
            handleOtpSuccess();
        } else {
            Toast.makeText(this, "Mã OTP không đúng hoặc đã hết hạn. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            clearOtpInputs();
        }
    }
    
    private void verifyOtpWithGoixe(String otp) {
        Log.d(TAG, "Verifying OTP: " + otp + " for phone: " + phoneNumber);
        
        // Check for test OTP first
        if ("123456".equals(otp)) {
            Log.d(TAG, "Test OTP detected: 123456 - bypassing verification");
            Toast.makeText(this, "Xác thực OTP thành công! (Test Mode)", Toast.LENGTH_SHORT).show();
            handleOtpSuccess();
            return;
        }
        
        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        // Sử dụng đúng tên field: recipient_phone và otp_code (theo API Goixe247)
        Call<OtpResponse> call = otpApiService.verifyOtp(
                GOIXE_USER_ID,
                GOIXE_API_KEY,
                phoneNumber,
                otp
        );
        
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Log.d(TAG, "OTP Verify Response Code: " + response.code());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        OtpResponse otpResponse = response.body();
                        Log.d(TAG, "OTP Verify Response: success=" + otpResponse.isSuccess() + ", message=" + otpResponse.getMessage());
                        
                        if (otpResponse.isSuccess()) {
                            Toast.makeText(OtpVerificationActivity.this, 
                                    "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();
                            handleOtpSuccess();
                        } else {
                            // OTP sai - hiển thị thông báo và xóa input để nhập lại
                            String errorMsg = otpResponse.getMessage() != null ? otpResponse.getMessage() : "Mã OTP không đúng";
                            Toast.makeText(OtpVerificationActivity.this, 
                                    errorMsg + ". Vui lòng nhập lại.", Toast.LENGTH_LONG).show();
                            clearOtpInputs();
                            // Focus vào ô đầu tiên
                            etOtp1.requestFocus();
                        }
                    } else {
                        // Lỗi response
                        Log.e(TAG, "OTP Verify failed: " + response.code());
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                        
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Xác thực OTP thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                        clearOtpInputs();
                        etOtp1.requestFocus();
                    }
                });
            }
            
            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Log.e(TAG, "OTP verification failed", t);
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Handle successful OTP verification
     */
    private void handleOtpSuccess() {
        Log.d(TAG, "handleOtpSuccess - fromActivity: " + fromActivity);
        
        if ("forgot_password".equals(fromActivity)) {
            // Chuyển sang màn hình đặt lại mật khẩu
            Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
            intent.putExtra("phone", phoneNumber);
            startActivity(intent);
            finish();
        } else if ("movie_booking".equals(fromActivity)) {
            // Xác thực thành công → Gọi API đặt vé
            processMovieBooking();
        } else if ("BILL_PAYMENT".equals(fromActivity)) {
            // Xác thực thành công → Gọi API thanh toán hóa đơn
            Log.d(TAG, "BILL_PAYMENT - Processing payment");
            processBillPayment();
        } else if ("SAVING".equals(fromActivity)) {
            // Xác thực thành công → Return result to SavingConfirmActivity
            Log.d(TAG, "SAVING - Returning RESULT_OK");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("OTP_VERIFIED", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if ("SAVING_WITHDRAW".equals(fromActivity)) {
            // Xác thực thành công → Return result to SavingWithdrawConfirmActivity
            Log.d(TAG, "SAVING_WITHDRAW - Returning RESULT_OK");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("OTP_VERIFIED", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if ("MORTGAGE_PAYMENT".equals(fromActivity)) {
            // Xác thực thành công → Gọi API thanh toán kỳ vay
            Log.d(TAG, "MORTGAGE_PAYMENT - Processing payment");
            processMortgagePayment();
        } else if ("MORTGAGE_SETTLEMENT".equals(fromActivity)) {
            // Xác thực thành công → Gọi API tất toán khoản vay
            Log.d(TAG, "MORTGAGE_SETTLEMENT - Processing settlement");
            processMortgageSettlement();
        } else if ("login_verification".equals(fromActivity)) {
            // Xác thực thành công → Đăng nhập
            performLogin();
        } else if ("register".equals(fromActivity)) {
            // Xác thực thành công → Trả về Step1BasicInfoFragment để chuyển sang Step 2
            setResult(RESULT_OK);
            finish();
        } else if ("transaction".equals(fromActivity)) {
            // Xác thực thành công → Gọi API confirm transfer
            processTransferConfirm();
        } else {
            Log.w(TAG, "Unknown fromActivity: " + fromActivity);
        }
    }
    
    private void performLogin() {
        if (phoneNumber == null || password == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        // Call login API
        LoginRequest loginRequest = new LoginRequest(phoneNumber, password);
        AuthApiService authApiService = ApiClient.getAuthApiService();
        
        Call<AuthResponse> call = authApiService.login(loginRequest);
        
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Save session and user info
                    DataManager dataManager = DataManager.getInstance(OtpVerificationActivity.this);
                    User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole()) 
                            ? User.UserRole.CUSTOMER 
                            : User.UserRole.OFFICER;
                    
                    dataManager.saveLoggedInUser(phoneNumber, role);
                    dataManager.saveLastUsername(phoneNumber);
                    
                    if (authResponse.getUserId() != null) {
                        dataManager.saveUserId(authResponse.getUserId());
                    }
                    if (authResponse.getPhone() != null) {
                        dataManager.saveUserPhone(authResponse.getPhone());
                    }
                    if (authResponse.getFullName() != null) {
                        dataManager.saveUserFullName(authResponse.getFullName());
                        dataManager.saveLastFullName(authResponse.getFullName());
                    }
                    if (authResponse.getEmail() != null) {
                        dataManager.saveUserEmail(authResponse.getEmail());
                    }
                    
                    if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
                        dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
                    }
                    
                    SessionManager sessionManager = SessionManager.getInstance(OtpVerificationActivity.this);
                    sessionManager.onLoginSuccess();
                    
                    // Đăng ký FCM token sau khi đăng nhập thành công
                    com.example.mobilebanking.utils.FcmTokenManager.registerFcmToken(OtpVerificationActivity.this);
                    
                    Toast.makeText(OtpVerificationActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to dashboard
                    Intent intent = new Intent(OtpVerificationActivity.this, 
                            com.example.mobilebanking.ui_home.UiHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Đăng nhập thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                Toast.makeText(OtpVerificationActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void processMovieBooking() {
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        // Lấy thông tin booking từ Intent
        String customerName = getIntent().getStringExtra("customer_name");
        String customerPhone = getIntent().getStringExtra("customer_phone");
        String customerEmail = getIntent().getStringExtra("customer_email");
        Long screeningId = getIntent().getLongExtra("screening_id", -1);
        long[] seatIds = getIntent().getLongArrayExtra("seat_ids");
        
        if (screeningId == -1 || seatIds == null || seatIds.length == 0) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin đặt vé", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Convert seatIds to List
        List<Long> seatIdList = new ArrayList<>();
        for (long id : seatIds) {
            seatIdList.add(id);
        }
        
        // Build request
        BookingRequest request = new BookingRequest(
                screeningId,
                seatIdList,
                customerName,
                customerPhone,
                customerEmail
        );
        
        // Call API
        MovieApiService apiService = ApiClient.getMovieApiService();
        Call<BookingResponse> call = apiService.createBooking(request);
        
        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse bookingResponse = response.body();
                    
                    if (bookingResponse.getSuccess() != null && bookingResponse.getSuccess()) {
                        // Success - navigate to success screen
                        navigateToMovieSuccessScreen(bookingResponse.getData());
                    } else {
                        // Error
                        String errorMsg = parseMovieBookingError(response.code(), bookingResponse.getMessage());
                        Toast.makeText(OtpVerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = parseMovieBookingError(response.code(), null);
                    Toast.makeText(OtpVerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                Toast.makeText(OtpVerificationActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private String parseMovieBookingError(int statusCode, String message) {
        if (statusCode == 402) {
            return "Số dư tài khoản không đủ để thanh toán.";
        } else if (statusCode == 409) {
            return "Ghế đã được đặt bởi người khác. Vui lòng chọn ghế khác.";
        } else if (message != null && !message.isEmpty()) {
            return message;
        } else {
            return "Đặt vé thất bại. Vui lòng thử lại.";
        }
    }
    
    private void navigateToMovieSuccessScreen(BookingResponse.BookingData data) {
        Intent intent = new Intent(this, MovieTicketSuccessActivity.class);
        
        if (data != null) {
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_BOOKING_CODE, data.getBookingCode());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_MOVIE_TITLE, data.getMovieTitle());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_NAME, data.getCinemaName());
            
            // Combine screening date and start time for showtime
            String showtime = "";
            if (data.getScreeningDate() != null && data.getStartTime() != null) {
                showtime = data.getScreeningDate() + " " + data.getStartTime();
            } else if (data.getStartTime() != null) {
                showtime = data.getStartTime();
            }
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SHOWTIME, showtime);
            
            // Convert seat labels list to comma-separated string
            String seats = "";
            if (data.getSeatLabels() != null && !data.getSeatLabels().isEmpty()) {
                seats = String.join(", ", data.getSeatLabels());
            }
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SEATS, seats);
            
            // Use EXTRA_TOTAL_AMOUNT (not EXTRA_TOTAL_PRICE)
            if (data.getTotalAmount() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_TOTAL_AMOUNT, data.getTotalAmount());
            }
            
            // Additional fields from BookingData
            if (data.getCinemaAddress() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_ADDRESS, data.getCinemaAddress());
            }
            if (data.getHallName() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_HALL_NAME, data.getHallName());
            }
            if (data.getScreeningDate() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_SCREENING_DATE, data.getScreeningDate());
            }
            if (data.getStartTime() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_START_TIME, data.getStartTime());
            }
            if (data.getSeatCount() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_SEAT_COUNT, data.getSeatCount());
            }
            if (data.getCustomerName() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_CUSTOMER_NAME, data.getCustomerName());
            }
            if (data.getQrCode() != null) {
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_QR_CODE, data.getQrCode());
            }
        } else {
            // Fallback: use data from intent
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_MOVIE_TITLE, getIntent().getStringExtra("movie_title"));
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_NAME, getIntent().getStringExtra("cinema_name"));
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SHOWTIME, getIntent().getStringExtra("showtime"));
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SEATS, getIntent().getStringExtra("seats"));
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_TOTAL_AMOUNT, getIntent().getDoubleExtra("total_amount", 0));
        }
        
        startActivity(intent);
        finish();
    }
    
    /**
     * Process transfer confirmation after OTP verification
     */
    private void processTransferConfirm() {
        // Get transaction code and bank from intent
        String transactionCode = getIntent().getStringExtra("transaction_code");
        String bankCode = getIntent().getStringExtra("bank");
        
        Log.d(TAG, "Processing transfer confirm - transactionCode: " + transactionCode + ", bank: " + bankCode);
        
        if (transactionCode == null || transactionCode.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        TransferApiService transferApiService = ApiClient.getTransferApiService();
        
        // Check if internal or external transfer
        if ("HATBANK".equals(bankCode)) {
            // Internal transfer - use JSON body
            Log.d(TAG, "Calling internal transfer confirm API");
            TransferConfirmRequest request = new TransferConfirmRequest(transactionCode);
            Call<TransferConfirmResponse> call = transferApiService.confirmInternalTransfer(request);
            
            call.enqueue(new Callback<TransferConfirmResponse>() {
                @Override
                public void onResponse(Call<TransferConfirmResponse> call, Response<TransferConfirmResponse> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Log.d(TAG, "Internal transfer confirm response code: " + response.code());
                    
                    if (response.code() == 401) {
                        Log.e(TAG, "Unauthorized - Token may be expired or invalid");
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        TransferConfirmResponse confirmResponse = response.body();
                        Log.d(TAG, "Response - status: " + confirmResponse.getStatus() + 
                                ", transactionId: " + confirmResponse.getTransactionId() +
                                ", isSuccess: " + confirmResponse.isSuccess());
                        
                        if (confirmResponse.isSuccess()) {
                            Log.d(TAG, "Internal transfer confirmed successfully - navigating to success");
                            Toast.makeText(OtpVerificationActivity.this, 
                                    "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();
                            navigateToTransferSuccess();
                        } else {
                            String errorMsg = confirmResponse.getMessage() != null ? 
                                    confirmResponse.getMessage() : "Giao dịch thất bại";
                            Toast.makeText(OtpVerificationActivity.this, 
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to confirm internal transfer: " + response.code());
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Xác nhận giao dịch thất bại (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<TransferConfirmResponse> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Log.e(TAG, "Error confirming internal transfer", t);
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // External transfer - use form-data
            Log.d(TAG, "Calling external transfer confirm API");
            Call<TransferConfirmResponse> call = transferApiService.confirmExternalTransfer(transactionCode);
            
            call.enqueue(new Callback<TransferConfirmResponse>() {
                @Override
                public void onResponse(Call<TransferConfirmResponse> call, Response<TransferConfirmResponse> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Log.d(TAG, "External transfer confirm response code: " + response.code());
                    
                    if (response.code() == 401) {
                        Log.e(TAG, "Unauthorized - Token may be expired or invalid");
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        TransferConfirmResponse confirmResponse = response.body();
                        Log.d(TAG, "Response - status: " + confirmResponse.getStatus() + 
                                ", success: " + confirmResponse.isSuccess() +
                                ", message: " + confirmResponse.getMessage());
                        
                        if (confirmResponse.isSuccess()) {
                            Log.d(TAG, "External transfer confirmed successfully - navigating to success");
                            Toast.makeText(OtpVerificationActivity.this, 
                                    "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show();
                            navigateToTransferSuccess();
                        } else {
                            String errorMsg = confirmResponse.getMessage() != null ? 
                                    confirmResponse.getMessage() : "Giao dịch thất bại";
                            Toast.makeText(OtpVerificationActivity.this, 
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to confirm external transfer: " + response.code());
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                        Toast.makeText(OtpVerificationActivity.this, 
                                "Xác nhận giao dịch thất bại (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<TransferConfirmResponse> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Log.e(TAG, "Error confirming external transfer", t);
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    /**
     * Navigate to transfer success screen
     */
    private void navigateToTransferSuccess() {
        Intent successIntent = new Intent(OtpVerificationActivity.this, TransferSuccessActivity.class);

        // Pass transaction data from previous intent
        Intent originalIntent = getIntent();
        successIntent.putExtra("amount", originalIntent.getDoubleExtra("amount", 0));
        successIntent.putExtra("to_account", originalIntent.getStringExtra("to_account"));
        successIntent.putExtra("to_name", originalIntent.getStringExtra("to_name"));
        successIntent.putExtra("note", originalIntent.getStringExtra("note"));
        successIntent.putExtra("bank", originalIntent.getStringExtra("bank"));
        successIntent.putExtra("transaction_code", originalIntent.getStringExtra("transaction_code"));

        // Add flag to indicate we need to clear the transaction stack
        successIntent.putExtra("clear_transaction_stack", true);

        // Start success activity
        startActivity(successIntent);
        finish();
    }
    
    /**
     * Process bill payment after OTP verification
     */
    private void processBillPayment() {
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        // Get bill code from intent
        String billCode = getIntent().getStringExtra("BILL_CODE");
        
        if (billCode == null || billCode.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Build request
        com.example.mobilebanking.api.dto.BillPaymentRequest request = 
            new com.example.mobilebanking.api.dto.BillPaymentRequest(billCode);
        
        // Call API
        ApiClient.getUtilityBillApiService()
            .payBill(request)
            .enqueue(new retrofit2.Callback<com.example.mobilebanking.api.dto.BillPaymentResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.mobilebanking.api.dto.BillPaymentResponse> call, 
                                     retrofit2.Response<com.example.mobilebanking.api.dto.BillPaymentResponse> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.mobilebanking.api.dto.BillPaymentResponse paymentResponse = response.body();
                        if (paymentResponse.getSuccess() && paymentResponse.getData() != null) {
                            // Payment successful - navigate to success screen
                            navigateToBillPaymentSuccess(paymentResponse.getData());
                        } else {
                            Toast.makeText(OtpVerificationActivity.this, 
                                paymentResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, 
                            "Lỗi thanh toán: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.mobilebanking.api.dto.BillPaymentResponse> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnVerify.setEnabled(true);
                    
                    Toast.makeText(OtpVerificationActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
    
    /**
     * Navigate to bill payment success screen
     */
    private void navigateToBillPaymentSuccess(com.example.mobilebanking.api.dto.BillPaymentResponse.PaymentData paymentData) {
        Intent intent = new Intent(this, BillPaymentSuccessActivity.class);
        
        // Pass payment result data
        intent.putExtra("transaction_id", paymentData.getTransactionId());
        intent.putExtra("bill_code", paymentData.getBillCode());
        intent.putExtra("amount", paymentData.getAmount() != null ? 
            paymentData.getAmount().toString() : getIntent().getStringExtra("AMOUNT"));
        intent.putExtra("payment_time", paymentData.getPaymentTime());
        intent.putExtra("balance_after", paymentData.getBalanceAfter() != null ? 
            paymentData.getBalanceAfter().toString() : "0");
        intent.putExtra("status", paymentData.getStatus());
        intent.putExtra("message", paymentData.getMessage());
        
        // Pass bill info from original intent
        intent.putExtra("provider_name", getIntent().getStringExtra("PROVIDER_NAME"));
        intent.putExtra("bill_type", getIntent().getStringExtra("BILL_TYPE"));
        intent.putExtra("billing_period", getIntent().getStringExtra("BILLING_PERIOD"));
        intent.putExtra("account_number", getIntent().getStringExtra("ACCOUNT_NUMBER"));

        startActivity(intent);
        finish();
    }
    
    private void clearOtpInputs() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void resendOtp() {
        Log.d(TAG, "Resending OTP for flow: " + fromActivity);
        
        // Xóa các ô input trước
        clearOtpInputs();
        
        // Tất cả các flow đều dùng Goixe247
        sendOtpWithGoixe();
        
        // Reset timer và focus
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startTimer();
        etOtp1.requestFocus();
    }

    private void startTimer() {
        btnResend.setEnabled(false);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Gửi lại sau " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Chưa nhận được mã?");
                btnResend.setEnabled(true);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    
    /**
     * Process mortgage payment after OTP verification
     */
    private void processMortgagePayment() {
        Long mortgageId = getIntent().getLongExtra("MORTGAGE_ID", 0);
        Double paymentAmount = getIntent().getDoubleExtra("PAYMENT_AMOUNT", 0);
        String paymentAccount = getIntent().getStringExtra("PAYMENT_ACCOUNT");
        String mortgageAccount = getIntent().getStringExtra("MORTGAGE_ACCOUNT");
        Integer periodNumber = getIntent().getIntExtra("PERIOD_NUMBER", 0);
        
        if (mortgageId == 0 || paymentAmount == 0 || paymentAccount == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        // Create request
        com.example.mobilebanking.api.dto.MortgagePaymentRequest request = 
                new com.example.mobilebanking.api.dto.MortgagePaymentRequest(
                        mortgageId, paymentAmount, paymentAccount);
        
        // Call API
        com.example.mobilebanking.api.AccountApiService service = ApiClient.getAccountApiService();
        service.payCurrentPeriod(request).enqueue(new Callback<com.example.mobilebanking.api.dto.MortgageAccountDTO>() {
            @Override
            public void onResponse(Call<com.example.mobilebanking.api.dto.MortgageAccountDTO> call, 
                                   Response<com.example.mobilebanking.api.dto.MortgageAccountDTO> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.mobilebanking.api.dto.MortgageAccountDTO result = response.body();
                    
                    // Chuyển sang màn hình thành công
                    Intent intent = new Intent(OtpVerificationActivity.this, MortgagePaymentSuccessActivity.class);
                    intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccount);
                    intent.putExtra("PERIOD_NUMBER", periodNumber);
                    intent.putExtra("PAYMENT_AMOUNT", paymentAmount);
                    intent.putExtra("PAYMENT_ACCOUNT", paymentAccount);
                    intent.putExtra("REMAINING_BALANCE", result.getRemainingBalance());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Thanh toán thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.mobilebanking.api.dto.MortgageAccountDTO> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                Toast.makeText(OtpVerificationActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Process mortgage settlement after OTP verification
     */
    private void processMortgageSettlement() {
        Long mortgageId = getIntent().getLongExtra("MORTGAGE_ID", 0);
        Double settlementAmount = getIntent().getDoubleExtra("SETTLEMENT_AMOUNT", 0);
        String paymentAccount = getIntent().getStringExtra("PAYMENT_ACCOUNT");
        String mortgageAccount = getIntent().getStringExtra("MORTGAGE_ACCOUNT");
        
        if (mortgageId == 0 || settlementAmount == 0 || paymentAccount == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin tất toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnVerify.setEnabled(false);
        
        // Create request
        com.example.mobilebanking.api.dto.MortgagePaymentRequest request = 
                new com.example.mobilebanking.api.dto.MortgagePaymentRequest(
                        mortgageId, settlementAmount, paymentAccount);
        
        // Call API
        com.example.mobilebanking.api.AccountApiService service = ApiClient.getAccountApiService();
        service.settleMortgage(request).enqueue(new Callback<com.example.mobilebanking.api.dto.MortgageAccountDTO>() {
            @Override
            public void onResponse(Call<com.example.mobilebanking.api.dto.MortgageAccountDTO> call, 
                                   Response<com.example.mobilebanking.api.dto.MortgageAccountDTO> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.mobilebanking.api.dto.MortgageAccountDTO result = response.body();
                    
                    // Chuyển sang màn hình thành công
                    Intent intent = new Intent(OtpVerificationActivity.this, MortgageSettlementSuccessActivity.class);
                    intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccount);
                    intent.putExtra("SETTLEMENT_AMOUNT", settlementAmount);
                    intent.putExtra("PAYMENT_ACCOUNT", paymentAccount);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OtpVerificationActivity.this, 
                            "Tất toán thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.mobilebanking.api.dto.MortgageAccountDTO> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(android.view.View.GONE);
                }
                btnVerify.setEnabled(true);
                
                Toast.makeText(OtpVerificationActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

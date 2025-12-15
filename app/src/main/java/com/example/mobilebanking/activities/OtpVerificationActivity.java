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
import com.example.mobilebanking.utils.ESmsConfig;
import com.example.mobilebanking.utils.SmsService;

/**
 * OTP Verification Activity
 */
public class OtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerification";
    
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify, btnResend;
    private TextView tvPhone, tvTimer;
    private ProgressBar progressBar;
    private String phoneNumber;
    private String fromActivity;
    private CountDownTimer countDownTimer;
    
    private SmsService smsService;
    private ESmsConfig esmsConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        phoneNumber = getIntent().getStringExtra("phone");
        fromActivity = getIntent().getStringExtra("from");

        // Initialize SMS service
        smsService = new SmsService(this);
        esmsConfig = new ESmsConfig(this);

        initializeViews();
        setupOtpInputs();
        setupListeners();
        
        // Check if eSMS is configured
        if (esmsConfig.isConfigured()) {
            // Send OTP automatically when activity starts
            sendOtp();
        } else {
            // Show dialog to configure API keys
            showApiKeyConfigDialog();
        }
        
        startTimer();
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

        tvPhone.setText("Đã gửi đến " + phoneNumber);
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
        tvPhone.setText("Đang gửi OTP đến " + phoneNumber + "...");
        
        smsService.sendOtp(phoneNumber, new SmsService.SmsCallback() {
            @Override
            public void onSuccess(String otpCode, String smsId) {
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    tvPhone.setText("Đã gửi OTP đến " + phoneNumber);
                    
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
                    tvPhone.setText("Lỗi gửi OTP đến " + phoneNumber);
                    
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

        // Verify OTP
        boolean isValid = false;
        
        if (esmsConfig.isConfigured() && smsService != null) {
            // Verify with saved OTP
            isValid = smsService.verifyOtp(phoneNumber, otp);
        } else {
            // Fallback: accept any 6-digit code in test mode
            isValid = true;
            Log.d(TAG, "Test mode: Accepting OTP without verification");
        }

        if (isValid) {
            Toast.makeText(this, "Xác thực OTP thành công!", Toast.LENGTH_SHORT).show();

            if ("register".equals(fromActivity)) {
                // Registration successful, go to login
                Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Transaction verification, return to previous activity
                setResult(RESULT_OK);
                finish();
            }
        } else {
            Toast.makeText(this, "Mã OTP không đúng hoặc đã hết hạn. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            // Clear OTP inputs
            etOtp1.setText("");
            etOtp2.setText("");
            etOtp3.setText("");
            etOtp4.setText("");
            etOtp5.setText("");
            etOtp6.setText("");
            etOtp1.requestFocus();
        }
    }

    private void resendOtp() {
        if (esmsConfig.isConfigured()) {
            sendOtp();
        } else {
            Toast.makeText(this, "Đã gửi lại OTP đến " + phoneNumber, Toast.LENGTH_SHORT).show();
        }
        startTimer();
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
}

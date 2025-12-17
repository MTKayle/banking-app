package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.OtpApiService;
import com.example.mobilebanking.utils.OtpResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * OTP verification screen for Forgot Password flow.
 * Gửi mã OTP người dùng nhập lên Goixe247 (verify_otp.php) để kiểm tra đúng/sai.
 */
public class ForgotOtpVerificationActivity extends AppCompatActivity {

    // Đếm ngược 15 giây cho nút \"Gửi lại OTP\"
    private static final long COUNTDOWN_MILLIS = 15_000;

    // Thông tin tích hợp Goixe247 (giống ForgotPasswordActivity)
    private static final String BASE_URL = "https://otp.goixe247.com/";
    private static final String GOIXE_USER_ID = "13";
    private static final String GOIXE_API_KEY = "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da";

    private TextView tvPhoneDisplay;
    private TextView tvError;
    private TextView tvTimer;
    private ProgressBar progressBar;
    private MaterialButton btnVerify;
    private MaterialButton btnResendOtp;

    private TextInputEditText et1, et2, et3, et4, et5, et6;

    private CountDownTimer countDownTimer;
    private OtpApiService otpApiService;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_forgot);

        phone = getIntent().getStringExtra("phone");

        initViews();
        initOtpService();
        setupListeners();
        startCountdown();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        tvError = findViewById(R.id.tv_error);
        tvTimer = findViewById(R.id.tv_timer);
        progressBar = findViewById(R.id.progress_bar);
        btnVerify = findViewById(R.id.btn_verify);
        btnResendOtp = findViewById(R.id.btn_resend_otp);

        et1 = findViewById(R.id.et_otp_1);
        et2 = findViewById(R.id.et_otp_2);
        et3 = findViewById(R.id.et_otp_3);
        et4 = findViewById(R.id.et_otp_4);
        et5 = findViewById(R.id.et_otp_5);
        et6 = findViewById(R.id.et_otp_6);

        btnBack.setOnClickListener(v -> onBackPressed());

        if (!TextUtils.isEmpty(phone)) {
            String display = getString(R.string.sent_to_phone) + " " + phone;
            tvPhoneDisplay.setText(display);
        }
    }

    private void setupListeners() {
        setupOtpTextWatchers();

        btnVerify.setOnClickListener(v -> {
            verifyOtp();
        });

        btnResendOtp.setOnClickListener(v -> {
            // Tạm thời chỉ reset countdown. Khi backend sẵn sàng, sẽ gọi lại API gửi OTP.
            Toast.makeText(this, getString(R.string.resend_otp), Toast.LENGTH_SHORT).show();
            startCountdown();
        });
    }

    private void initOtpService() {
        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();
        otpApiService = retrofit.create(OtpApiService.class);
    }

    private void setupOtpTextWatchers() {
        TextInputEditText[] editTexts = {et1, et2, et3, et4, et5, et6};
        
        for (int i = 0; i < editTexts.length; i++) {
            final int index = i;
            final TextInputEditText currentEdit = editTexts[i];
            final TextInputEditText nextEdit = (i < editTexts.length - 1) ? editTexts[i + 1] : null;
            
            currentEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString();
                    
                    // Chỉ cho phép nhập 1 số
                    if (text.length() > 1) {
                        currentEdit.setText(text.substring(0, 1));
                        currentEdit.setSelection(1);
                        return;
                    }
                    
                    // Xóa lỗi cũ khi bắt đầu nhập lại
                    hideError();
                    
                    // Tự động chuyển sang ô tiếp theo khi nhập xong
                    if (text.length() == 1 && nextEdit != null) {
                        nextEdit.requestFocus();
                    }
                    
                    // Khi nhập đủ 6 số, tự động verify
                    if (index == 5 && text.length() == 1) {
                        // Đợi một chút để đảm bảo text đã được set vào tất cả các ô
                        currentEdit.postDelayed(() -> {
                            String collectedOtp = collectOtp();
                            if (collectedOtp.length() == 6) {
                                verifyOtp();
                            }
                        }, 200);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void verifyOtp() {
        String otp = collectOtp();

        if (otp.length() != 6) {
            showError("Vui lòng nhập đủ 6 số OTP");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            showError("Thiếu số điện thoại để xác thực OTP.");
            return;
        }

        // Gửi mã OTP người dùng nhập lên Goixe247 để xác thực
        setLoading(true);
        retrofit2.Call<OtpResponse> call = otpApiService.verifyOtp(
                GOIXE_USER_ID,
                GOIXE_API_KEY,
                phone,
                otp
        );

        call.enqueue(new retrofit2.Callback<OtpResponse>() {
            @Override
            public void onResponse(retrofit2.Call<OtpResponse> call, retrofit2.Response<OtpResponse> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    showError(getString(R.string.network_error));
                    return;
                }

                OtpResponse otpResponse = response.body();
                if (otpResponse.isSuccess()) {
                    hideError();
                    clearAllErrors();
                    Toast.makeText(ForgotOtpVerificationActivity.this,
                            "Xác thực OTP thành công.", Toast.LENGTH_SHORT).show();

                    // TODO: chuyển sang màn hình đặt lại mật khẩu nếu cần
                } else {
                    // OTP sai hoặc hết hạn
                    String msg = "Xác thực OTP thất bại. Vui lòng kiểm tra lại mã và nhập lại.";
                    showError(msg);
                    clearOtpInputs();
                    clearAllErrors();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<OtpResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.network_error));
            }
        });
    }
    
    private void clearAllErrors() {
        if (et1 != null) et1.setError(null);
        if (et2 != null) et2.setError(null);
        if (et3 != null) et3.setError(null);
        if (et4 != null) et4.setError(null);
        if (et5 != null) et5.setError(null);
        if (et6 != null) et6.setError(null);
    }

    private String collectOtp() {
        StringBuilder sb = new StringBuilder(6);
        String s1 = getTextFromEditText(et1);
        String s2 = getTextFromEditText(et2);
        String s3 = getTextFromEditText(et3);
        String s4 = getTextFromEditText(et4);
        String s5 = getTextFromEditText(et5);
        String s6 = getTextFromEditText(et6);
        
        sb.append(s1 != null ? s1 : "");
        sb.append(s2 != null ? s2 : "");
        sb.append(s3 != null ? s3 : "");
        sb.append(s4 != null ? s4 : "");
        sb.append(s5 != null ? s5 : "");
        sb.append(s6 != null ? s6 : "");
        
        return sb.toString();
    }

    private String getTextFromEditText(TextInputEditText editText) {
        if (editText == null) {
            return null;
        }
        CharSequence text = editText.getText();
        if (text == null) {
            return null;
        }
        String result = text.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(COUNTDOWN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(getString(R.string.resend_timer, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("");
                btnResendOtp.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    private void clearOtpInputs() {
        // Xóa text trong tất cả các ô
        if (et1 != null) {
            et1.setText("");
            et1.setError(null);
        }
        if (et2 != null) {
            et2.setText("");
            et2.setError(null);
        }
        if (et3 != null) {
            et3.setText("");
            et3.setError(null);
        }
        if (et4 != null) {
            et4.setText("");
            et4.setError(null);
        }
        if (et5 != null) {
            et5.setText("");
            et5.setError(null);
        }
        if (et6 != null) {
            et6.setText("");
            et6.setError(null);
        }
        // Focus vào ô đầu tiên
        if (et1 != null) {
            et1.requestFocus();
        }
    }


    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}



package com.example.mobilebanking.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Forgot Password screen:
 * - Input registered phone number
 * - Validate format
 * - Call Goixe247 OTP API to send OTP
 *
 * Backend verification & password reset will be implemented later.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://otp.goixe247.com/";

    // user_id thực tế trong app Goixe247 OTP Gateway
    private static final String GOIXE_USER_ID = "13";

    // API key do bạn cung cấp – lưu ý: không nên commit key thật vào repo public
    private static final String GOIXE_API_KEY = "328945bfca039d9663890e71f4d9e2203669dd1e49fd3cb9a44fa86a48d915da";

    // Regex: ^(0|84)[2-9][0-9]{8}$
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(0|84)[2-9][0-9]{8}$");

    private TextInputEditText etPhone;
    private TextView tvError;
    private ProgressBar progressBar;
    private MaterialButton btnSendOtp;

    private OtpApiService otpApiService;

    private static final String PREFS_NAME = "OtpPrefs";
    private static final String KEY_OTP = "expected_otp";
    private static final String KEY_OTP_PHONE = "otp_phone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        initOtpService();
        setupListeners();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        etPhone = findViewById(R.id.et_phone);
        tvError = findViewById(R.id.tv_error);
        progressBar = findViewById(R.id.progress_bar);
        btnSendOtp = findViewById(R.id.btn_send_otp);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void initOtpService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        otpApiService = retrofit.create(OtpApiService.class);
    }

    private void setupListeners() {
        btnSendOtp.setOnClickListener(v -> {
            String phone = etPhone.getText() != null
                    ? etPhone.getText().toString().trim()
                    : "";

            if (!isValidPhone(phone)) {
                showError(getString(R.string.invalid_phone_format));
                return;
            }

            hideError();
            requestOtp(phone);
        });
    }

    private boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private void requestOtp(String phone) {
        setLoading(true);

        Call<OtpResponse> call = otpApiService.requestOtp(
                GOIXE_USER_ID,
                GOIXE_API_KEY,
                phone
        );

        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            getString(R.string.network_error),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                OtpResponse otpResponse = response.body();
                if (otpResponse.isSuccess()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            getString(R.string.otp_sent_success),
                            Toast.LENGTH_SHORT).show();

                    // LƯU Ý: API Goixe247 không trả về mã OTP trong response
                    // Mã OTP sẽ được nhập từ SMS và lưu vào SharedPreferences ở màn hình verify
                    // TODO: Khi có backend verify, sẽ không cần lưu OTP này nữa
                    
                    // Sau khi gửi OTP thành công, chuyển sang màn hình nhập OTP
                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("phone", phone);
                    intent.putExtra("from", "forgot_password");
                    startActivity(intent);

                } else {
                    String message = otpResponse.getMessage() != null
                            ? otpResponse.getMessage()
                            : getString(R.string.sms_delivery_failed);
                    showError(message);
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this,
                        getString(R.string.network_error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSendOtp.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

}



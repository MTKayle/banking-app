package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.ChangePasswordRequest;
import com.example.mobilebanking.api.dto.ChangePasswordResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reset Password screen for Forgot Password flow.
 * Gọi API /api/password/change để đổi mật khẩu.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private TextView tvError;
    private ProgressBar progressBar;
    private MaterialButton btnResetPassword;

    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        phone = getIntent().getStringExtra("phone");

        initViews();
        setupListeners();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvError = findViewById(R.id.tv_error);
        progressBar = findViewById(R.id.progress_bar);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText() != null
                    ? etNewPassword.getText().toString()
                    : "";
            String confirmPassword = etConfirmPassword.getText() != null
                    ? etConfirmPassword.getText().toString()
                    : "";

            if (!validatePasswords(newPassword, confirmPassword)) {
                return;
            }

            hideError();
            performPasswordReset(newPassword);
        });
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            showError(getString(R.string.error_required_fields));
            return false;
        }

        if (newPassword.length() < 6) {
            showError(getString(R.string.password_too_short));
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError(getString(R.string.password_mismatch));
            return false;
        }

        return true;
    }

    private void performPasswordReset(String newPassword) {
        if (TextUtils.isEmpty(phone)) {
            showError("Thiếu số điện thoại");
            return;
        }

        setLoading(true);

        ChangePasswordRequest request = new ChangePasswordRequest(phone, newPassword);
        AuthApiService apiService = ApiClient.getAuthApiService();
        
        apiService.changePassword(request).enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ChangePasswordResponse result = response.body();
                    if (result.isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this,
                                "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                                Toast.LENGTH_LONG).show();

                        // Chuyển về màn hình Login
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(result.getMessage() != null ? result.getMessage() : "Đổi mật khẩu thất bại");
                    }
                } else {
                    String errorMsg = "Đổi mật khẩu thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                setLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!loading);
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

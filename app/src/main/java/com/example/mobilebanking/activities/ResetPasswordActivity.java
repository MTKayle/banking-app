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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Reset Password screen for Forgot Password flow.
 * Hiện tại chỉ xử lý validation & UI, chưa gọi backend thật.
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
            performMockReset();
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

    private void performMockReset() {
        setLoading(true);

        // Hiện tại chỉ mock: delay nhẹ rồi báo thành công và quay về Login
        progressBar.postDelayed(() -> {
            setLoading(false);

            Toast.makeText(ResetPasswordActivity.this,
                    getString(R.string.password_reset_success),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 800);
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



package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.CccdQrParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Registration Activity with phone-first flow and QR code scanning
 * Step 1: Phone number input
 * Step 2: Full registration form with QR scan option
 */
public class RegisterActivity extends AppCompatActivity {
    private static final int QR_SCANNER_REQUEST_CODE = 200;
    private static final String EXTRA_PHONE = "phone_number";
    private static final String EXTRA_STEP = "step";
    private static final int STEP_1_PHONE = 1;
    private static final int STEP_2_FORM = 2;
    
    // Step 1 views
    private EditText etPhoneStep1;
    private Button btnContinue;
    private TextView tvLoginStep1;
    
    // Step 2 views
    private EditText etFullName, etEmail, etPhone, etIdNumber, etDateOfBirth, 
                     etGender, etAddress, etIssueDate, etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private FloatingActionButton fabScanQr;
    private ProgressBar progressBar;
    
    private String phoneNumber;
    private int currentStep = STEP_1_PHONE;
    private boolean isQrDataLoaded = false; // Track if QR data has been loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if coming from step 1 or step 2
        if (getIntent().hasExtra(EXTRA_STEP)) {
            currentStep = getIntent().getIntExtra(EXTRA_STEP, STEP_1_PHONE);
            phoneNumber = getIntent().getStringExtra(EXTRA_PHONE);
        }
        
        if (currentStep == STEP_2_FORM && phoneNumber != null) {
            loadStep2();
        } else {
            loadStep1();
        }
    }
    
    private void loadStep1() {
        setContentView(R.layout.activity_register_step1);
        currentStep = STEP_1_PHONE;
        
        etPhoneStep1 = findViewById(R.id.et_phone);
        btnContinue = findViewById(R.id.btn_continue);
        tvLoginStep1 = findViewById(R.id.tv_login);
        
        btnContinue.setOnClickListener(v -> handlePhoneContinue());
        tvLoginStep1.setOnClickListener(v -> finish());
    }
    
    private void loadStep2() {
        setContentView(R.layout.activity_register_step2);
        currentStep = STEP_2_FORM;
        
        initializeStep2Views();
        setupStep2Listeners();
        
        // Set phone number (read-only)
        if (phoneNumber != null) {
            etPhone.setText(phoneNumber);
        }
    }
    
    private void initializeStep2Views() {
        etPhone = findViewById(R.id.et_phone);
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etIdNumber = findViewById(R.id.et_id_number);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        etGender = findViewById(R.id.et_gender);
        etAddress = findViewById(R.id.et_address);
        etIssueDate = findViewById(R.id.et_issue_date);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        fabScanQr = findViewById(R.id.fab_scan_qr);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupStep2Listeners() {
        btnRegister.setOnClickListener(v -> handleRegistration());
        fabScanQr.setOnClickListener(v -> openQrScanner());
    }
    
    private void handlePhoneContinue() {
        String phone = etPhoneStep1.getText().toString().trim();
        
        // Remove leading 0 if present (for +84 format)
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }
        
        // Validation: 10 digits, starts with 0 (after removing leading 0, should be 9 digits)
        if (phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if phone starts with 0 (original format)
        String originalPhone = etPhoneStep1.getText().toString().trim();
        if (!originalPhone.startsWith("0")) {
            Toast.makeText(this, "Số điện thoại phải bắt đầu bằng 0", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (originalPhone.length() != 10) {
            Toast.makeText(this, "Số điện thoại phải có 10 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate phone number format (10 digits starting with 0)
        if (!originalPhone.matches("^0[0-9]{9}$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        phoneNumber = originalPhone;
        
        // Navigate to step 2
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra(EXTRA_STEP, STEP_2_FORM);
        intent.putExtra(EXTRA_PHONE, phoneNumber);
        startActivity(intent);
            finish();
    }

    private void openQrScanner() {
        Intent intent = new Intent(this, QrScannerActivity.class);
        startActivityForResult(intent, QR_SCANNER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == QR_SCANNER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                String qrData = data.getStringExtra("qr_data");
                if (qrData != null && !qrData.isEmpty()) {
                    processQrData(qrData);
                } else {
                    Toast.makeText(this, R.string.qr_scan_error, Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled, do nothing
            }
        }
    }

    private void processQrData(String qrData) {
        try {
            android.util.Log.d("RegisterActivity", "=== QR SCAN RESULT ===");
            android.util.Log.d("RegisterActivity", "Raw QR data length: " + qrData.length());
            android.util.Log.d("RegisterActivity", "Raw QR data (full): " + qrData);
            android.util.Log.d("RegisterActivity", "Raw QR data (first 200 chars): " + 
                (qrData.length() > 200 ? qrData.substring(0, 200) : qrData));
            
            // Show loading indicator
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            CccdQrParser.CccdData cccdData = CccdQrParser.parseQrData(qrData);
            
            android.util.Log.d("RegisterActivity", "=== PARSED DATA ===");
            if (cccdData != null) {
                android.util.Log.d("RegisterActivity", "Full Name: " + cccdData.getFullName());
                android.util.Log.d("RegisterActivity", "ID Number: " + cccdData.getIdNumber());
                android.util.Log.d("RegisterActivity", "Date of Birth: " + cccdData.getDateOfBirth());
                android.util.Log.d("RegisterActivity", "Gender: " + cccdData.getGender());
                android.util.Log.d("RegisterActivity", "Address: " + cccdData.getPermanentAddress());
                android.util.Log.d("RegisterActivity", "Issue Date: " + cccdData.getIssueDate());
            } else {
                android.util.Log.e("RegisterActivity", "Parsed data is NULL");
            }
            
            // Hide loading indicator
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            
            if (cccdData == null) {
                android.util.Log.e("RegisterActivity", "Failed to parse QR data");
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Lỗi quét mã QR")
                        .setMessage("Không thể đọc thông tin từ mã QR. Mã QR có thể không đúng định dạng CCCD hoặc bị hỏng.\n\n" +
                                   "Vui lòng:\n" +
                                   "• Kiểm tra mã QR còn rõ ràng không\n" +
                                   "• Thử quét lại\n" +
                                   "• Hoặc nhập thông tin thủ công")
                        .setPositiveButton("Thử lại", (dialog, which) -> openQrScanner())
                        .setNegativeButton("Nhập thủ công", null)
                        .show();
                return;
            }

            // Auto-fill fields with parsed data
            boolean hasData = false;
            
            if (cccdData.getFullName() != null && !cccdData.getFullName().isEmpty()) {
                etFullName.setText(cccdData.getFullName());
                hasData = true;
                android.util.Log.d("RegisterActivity", "Filled Full Name: " + cccdData.getFullName());
            }

            if (cccdData.getIdNumber() != null && !cccdData.getIdNumber().isEmpty()) {
                etIdNumber.setText(cccdData.getIdNumber());
                hasData = true;
                android.util.Log.d("RegisterActivity", "Filled ID Number: " + cccdData.getIdNumber());
            }

            if (cccdData.getDateOfBirth() != null && !cccdData.getDateOfBirth().isEmpty()) {
                etDateOfBirth.setText(cccdData.getDateOfBirth());
                hasData = true;
                android.util.Log.d("RegisterActivity", "Filled Date of Birth: " + cccdData.getDateOfBirth());
            }

            if (cccdData.getGender() != null && !cccdData.getGender().isEmpty()) {
                etGender.setText(cccdData.getGender());
                hasData = true;
                android.util.Log.d("RegisterActivity", "Filled Gender: " + cccdData.getGender());
            }

            if (cccdData.getPermanentAddress() != null && !cccdData.getPermanentAddress().isEmpty()) {
                etAddress.setText(cccdData.getPermanentAddress());
                hasData = true;
                android.util.Log.d("RegisterActivity", "Filled Address: " + cccdData.getPermanentAddress());
            }

            if (cccdData.getIssueDate() != null && !cccdData.getIssueDate().isEmpty()) {
                etIssueDate.setText(cccdData.getIssueDate());
                hasData = true;
                android.util.Log.d("RegisterActivity", "Filled Issue Date: " + cccdData.getIssueDate());
            }

            if (hasData) {
                // Lock all CCCD fields to prevent editing
                lockCccdFields();
                isQrDataLoaded = true;
                Toast.makeText(this, R.string.qr_scan_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin trong mã QR", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "Error processing QR data", e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, R.string.qr_scan_error + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Lock all CCCD-related fields to prevent editing after QR scan
     */
    private void lockCccdFields() {
        // Lock fields that come from CCCD QR code
        etFullName.setEnabled(false);
        etFullName.setFocusable(false);
        etFullName.setClickable(false);
        etFullName.setAlpha(0.6f); // Visual indicator that field is locked
        
        etIdNumber.setEnabled(false);
        etIdNumber.setFocusable(false);
        etIdNumber.setClickable(false);
        etIdNumber.setAlpha(0.6f);
        
        etDateOfBirth.setEnabled(false);
        etDateOfBirth.setFocusable(false);
        etDateOfBirth.setClickable(false);
        etDateOfBirth.setAlpha(0.6f);
        
        etGender.setEnabled(false);
        etGender.setFocusable(false);
        etGender.setClickable(false);
        etGender.setAlpha(0.6f);
        
        etAddress.setEnabled(false);
        etAddress.setFocusable(false);
        etAddress.setClickable(false);
        etAddress.setAlpha(0.6f);
        
        etIssueDate.setEnabled(false);
        etIssueDate.setFocusable(false);
        etIssueDate.setClickable(false);
        etIssueDate.setAlpha(0.6f);
        
        android.util.Log.d("RegisterActivity", "CCCD fields locked after QR scan");
    }

    private void handleRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIdNumber.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
            idNumber.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to OTP verification
        Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("from", "register");
        startActivity(intent);
    }
}

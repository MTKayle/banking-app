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
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.AuthResponse;
import com.example.mobilebanking.api.dto.RegisterRequest;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.CccdQrParser;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
            idNumber.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone format (10-11 digits)
        if (!phone.matches("^[0-9]{10,11}$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (10-11 chữ số)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate CCCD number (9-12 digits)
        if (!idNumber.matches("^[0-9]{9,12}$")) {
            Toast.makeText(this, "Số CCCD không hợp lệ (9-12 chữ số)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        // Khởi tạo ApiClient nếu chưa
        ApiClient.init(this);

        // Create RegisterRequest
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPhone(phone);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFullName(fullName);
        registerRequest.setCccdNumber(idNumber);
        
        // Optional fields
        if (!dateOfBirth.isEmpty()) {
            // Convert date format if needed (from DD/MM/YYYY to yyyy-MM-dd)
            registerRequest.setDateOfBirth(convertDateFormat(dateOfBirth));
        }
        if (!address.isEmpty()) {
            registerRequest.setPermanentAddress(address);
        }

        // Call API
        AuthApiService authApiService = ApiClient.getAuthApiService();
        Call<AuthResponse> call = authApiService.register(registerRequest);
        
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Save session
                    DataManager dataManager = DataManager.getInstance(RegisterActivity.this);
                    User.UserRole role = "CUSTOMER".equalsIgnoreCase(authResponse.getRole()) 
                            ? User.UserRole.CUSTOMER 
                            : User.UserRole.OFFICER;
                    dataManager.saveLoggedInUser(phone, role);
                    dataManager.saveLastUsername(phone);
                    
                    // Lưu đầy đủ thông tin từ AuthResponse
                    // Lưu userId
                    if (authResponse.getUserId() != null) {
                        dataManager.saveUserId(authResponse.getUserId());
                    }
                    
                    // Lưu phone từ AuthResponse
                    if (authResponse.getPhone() != null && !authResponse.getPhone().isEmpty()) {
                        dataManager.saveUserPhone(authResponse.getPhone());
                    }
                    
                    // Lưu fullName từ AuthResponse
                    if (authResponse.getFullName() != null && !authResponse.getFullName().isEmpty()) {
                        dataManager.saveLastFullName(authResponse.getFullName());
                        dataManager.saveUserFullName(authResponse.getFullName());
                    }
                    
                    // Lưu email từ AuthResponse
                    if (authResponse.getEmail() != null && !authResponse.getEmail().isEmpty()) {
                        dataManager.saveUserEmail(authResponse.getEmail());
                    }
                    
                    // Lưu token
                    if (authResponse.getToken() != null && authResponse.getRefreshToken() != null) {
                        dataManager.saveTokens(authResponse.getToken(), authResponse.getRefreshToken());
                    } else if (authResponse.getToken() != null) {
                        dataManager.saveTokens(authResponse.getToken(), authResponse.getToken());
                    }

                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to dashboard based on role
                    Intent intent;
                    if (role == User.UserRole.OFFICER) {
                        intent = new Intent(RegisterActivity.this, OfficerDashboardActivity.class);
                    } else {
                        intent = new Intent(RegisterActivity.this, 
                                com.example.mobilebanking.ui_home.UiHomeActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // Parse error message
                    String errorMessage = "Đăng ký thất bại";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                            if (jsonObject.has("message")) {
                                errorMessage = jsonObject.get("message").getAsString();
                            } else if (jsonObject.has("error")) {
                                errorMessage = jsonObject.get("error").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                
                String errorMessage = "Không thể kết nối đến server";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra:\n" +
                                "1. Backend đã chạy chưa?\n" +
                                "2. Địa chỉ IP trong ApiClient đúng chưa?";
                    } else {
                        errorMessage = "Lỗi: " + t.getMessage();
                    }
                }
                
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Convert date format from DD/MM/YYYY to yyyy-MM-dd
     */
    private String convertDateFormat(String date) {
        try {
            if (date.contains("/")) {
                String[] parts = date.split("/");
                if (parts.length == 3) {
                    return parts[2] + "-" + parts[1] + "-" + parts[0];
                }
            }
            // If already in yyyy-MM-dd format, return as is
            return date;
        } catch (Exception e) {
            return date;
        }
    }
}

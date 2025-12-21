package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.CreateSavingRequest;
import com.example.mobilebanking.api.dto.CreateSavingResponse;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavingConfirmActivity extends AppCompatActivity {

    private TextView tvSourceAccount, tvAmount, tvTerm, tvMaturityDate;
    private TextView tvInterestRate, tvEstimatedInterest, tvTransactionDate;
    private Button btnConfirm;

    private String termType;
    private int termMonths;
    private double interestRate;
    private double amount;
    private String sourceAccountNumber;
    private String maturityDate;
    
    private DataManager dataManager;
    private boolean isProcessing = false;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> faceVerificationLauncher;
    private ActivityResultLauncher<Intent> otpVerificationLauncher;
    
    private static final double FACE_VERIFICATION_THRESHOLD = 10000000; // 10 triệu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_confirm);

        dataManager = DataManager.getInstance(this);

        // Get data from intent
        termType = getIntent().getStringExtra("termType");
        termMonths = getIntent().getIntExtra("termMonths", 1);
        interestRate = getIntent().getDoubleExtra("interestRate", 0.0);
        amount = getIntent().getDoubleExtra("amount", 0.0);
        sourceAccountNumber = getIntent().getStringExtra("sourceAccountNumber");
        maturityDate = getIntent().getStringExtra("maturityDate");

        initViews();
        setupToolbar();
        setupActivityResultLaunchers();
        displayInfo();
        setupConfirmButton();
    }
    
    /**
     * Setup activity result launchers for face and OTP verification
     */
    private void setupActivityResultLaunchers() {
        // Face verification launcher
        faceVerificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Face verification successful, proceed to OTP
                        navigateToOtpVerification();
                    } else {
                        // Face verification failed or cancelled
                        Toast.makeText(this, "Xác thực khuôn mặt thất bại", Toast.LENGTH_SHORT).show();
                    }
                });

        // OTP verification launcher
        otpVerificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    android.util.Log.d("SavingConfirm", "OTP result code: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        // OTP verification successful, create saving
                        android.util.Log.d("SavingConfirm", "OTP verified, calling createSaving()");
                        createSaving();
                    } else {
                        // OTP verification failed or cancelled
                        android.util.Log.w("SavingConfirm", "OTP verification failed or cancelled");
                        Toast.makeText(this, "Xác thực OTP thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        tvSourceAccount = findViewById(R.id.tv_source_account);
        tvAmount = findViewById(R.id.tv_amount);
        tvTerm = findViewById(R.id.tv_term);
        tvMaturityDate = findViewById(R.id.tv_maturity_date);
        tvInterestRate = findViewById(R.id.tv_interest_rate);
        tvEstimatedInterest = findViewById(R.id.tv_estimated_interest);
        tvTransactionDate = findViewById(R.id.tv_transaction_date);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayInfo() {
        tvSourceAccount.setText(sourceAccountNumber);
        tvAmount.setText(formatCurrency(amount) + " VND");
        tvTerm.setText(termMonths + " Tháng");
        tvMaturityDate.setText(maturityDate);
        tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%/năm", interestRate));
        
        // Calculate estimated interest
        double estimatedInterest = calculateEstimatedInterest();
        tvEstimatedInterest.setText(formatCurrency(estimatedInterest) + " VND");
        
        // Transaction date (today)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvTransactionDate.setText(sdf.format(new Date()));
    }

    private double calculateEstimatedInterest() {
        // Lãi = Tiền gửi x Lãi suất x Số tháng / 12
        return amount * (interestRate / 100) * termMonths / 12;
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            if (!isProcessing) {
                startVerificationFlow();
            }
        });
    }
    
    /**
     * Start verification flow based on amount
     */
    private void startVerificationFlow() {
        if (amount >= FACE_VERIFICATION_THRESHOLD) {
            // Amount >= 10 triệu: Face verification first, then OTP
            navigateToFaceVerification();
        } else {
            // Amount < 10 triệu: OTP only
            navigateToOtpVerification();
        }
    }
    
    /**
     * Navigate to face verification
     */
    private void navigateToFaceVerification() {
        Intent intent = new Intent(this, FaceVerificationTransactionActivity.class);
        intent.putExtra("transaction_type", "SAVING");
        intent.putExtra("amount", amount);
        faceVerificationLauncher.launch(intent);
    }
    
    /**
     * Navigate to OTP verification
     */
    private void navigateToOtpVerification() {
        android.util.Log.d("SavingConfirm", "Navigating to OTP verification");
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("verificationType", "SAVING");
        intent.putExtra("amount", amount);
        intent.putExtra("termType", termType);
        intent.putExtra("termMonths", termMonths);
        
        // Get phone number from DataManager
        String phone = dataManager.getUserPhone();
        intent.putExtra("phone", phone);
        
        android.util.Log.d("SavingConfirm", "OTP intent extras - verificationType: SAVING, phone: " + phone);
        otpVerificationLauncher.launch(intent);
    }

    private void createSaving() {
        isProcessing = true;
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");

        // Validate inputs before sending
        if (sourceAccountNumber == null || sourceAccountNumber.isEmpty()) {
            Toast.makeText(this, "Thiếu thông tin tài khoản nguồn", Toast.LENGTH_SHORT).show();
            isProcessing = false;
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Xác nhận");
            return;
        }

        if (termType == null || termType.isEmpty()) {
            Toast.makeText(this, "Thiếu thông tin kỳ hạn", Toast.LENGTH_SHORT).show();
            isProcessing = false;
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Xác nhận");
            return;
        }

        android.util.Log.d("SavingConfirm", "Creating saving: account=" + sourceAccountNumber 
                + ", amount=" + amount + ", term=" + termType);

        CreateSavingRequest request = new CreateSavingRequest(
                sourceAccountNumber,
                amount,
                termType
        );

        AccountApiService apiService = ApiClient.getAccountApiService();
        apiService.createSaving(request).enqueue(new Callback<CreateSavingResponse>() {
            @Override
            public void onResponse(Call<CreateSavingResponse> call, Response<CreateSavingResponse> response) {
                isProcessing = false;
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận");

                android.util.Log.d("SavingConfirm", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    navigateToSuccess(response.body());
                } else {
                    String errorMsg = "Giao dịch thất bại (code: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("SavingConfirm", "Error: " + errorBody);
                            // Parse error message if possible
                            if (errorBody.contains("message")) {
                                errorMsg = errorBody;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(SavingConfirmActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CreateSavingResponse> call, Throwable t) {
                isProcessing = false;
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận");
                Toast.makeText(SavingConfirmActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSuccess(CreateSavingResponse response) {
        Intent intent = new Intent(this, SavingSuccessActivity.class);
        intent.putExtra("savingBookNumber", response.getSavingBookNumber());
        intent.putExtra("amount", response.getBalance() != null ? response.getBalance() : amount);
        intent.putExtra("termMonths", response.getTermMonths() != null ? response.getTermMonths() : termMonths);
        intent.putExtra("interestRate", response.getInterestRate() != null ? response.getInterestRate() : interestRate);
        intent.putExtra("savingId", response.getSavingId() != null ? response.getSavingId() : 0L);
        intent.putExtra("accountNumber", response.getAccountNumber());
        intent.putExtra("openedDate", response.getOpenedDate());
        intent.putExtra("maturityDate", response.getMaturityDate());
        intent.putExtra("status", response.getStatus());
        
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }
}


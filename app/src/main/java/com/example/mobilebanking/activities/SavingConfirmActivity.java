package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        displayInfo();
        setupConfirmButton();
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
                createSaving();
            }
        });
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
        intent.putExtra("amount", response.getBalance());
        intent.putExtra("termMonths", response.getTermMonths());
        intent.putExtra("interestRate", response.getInterestRate());
        intent.putExtra("savingId", response.getSavingId());
        
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }
}


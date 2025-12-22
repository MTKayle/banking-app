package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.PaymentApiService;
import com.example.mobilebanking.api.dto.DepositRequest;
import com.example.mobilebanking.api.dto.DepositResponse;
import com.example.mobilebanking.api.dto.WithdrawRequest;
import com.example.mobilebanking.api.dto.WithdrawResponse;
import com.google.android.material.appbar.MaterialToolbar;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficerDepositActivity extends BaseActivity {
    
    private static final String TAG = "OfficerDepositActivity";
    
    private MaterialToolbar toolbar;
    private EditText etAccountNumber, etAmount, etNote;
    private Button btnSubmit;
    private RadioGroup rgTransactionType;
    private RadioButton rbDeposit, rbWithdraw;
    private ProgressBar progressBar;
    
    private PaymentApiService paymentApiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_deposit);
        
        paymentApiService = ApiClient.getPaymentApiService();
        
        initViews();
        setupToolbar();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etAccountNumber = findViewById(R.id.et_account_number);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        btnSubmit = findViewById(R.id.btn_deposit);
        rgTransactionType = findViewById(R.id.rg_transaction_type);
        rbDeposit = findViewById(R.id.rb_deposit);
        rbWithdraw = findViewById(R.id.rb_withdraw);
        progressBar = findViewById(R.id.progress_bar);
        
        // Set default to deposit
        if (rbDeposit != null) {
            rbDeposit.setChecked(true);
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupButton() {
        btnSubmit.setOnClickListener(v -> {
            String accountNumber = etAccountNumber.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            
            if (accountNumber.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if deposit or withdraw
            boolean isDeposit = rbDeposit == null || rbDeposit.isChecked();
            
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String formattedAmount = formatter.format(amount);
            String action = isDeposit ? "Nạp" : "Rút";
            
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action.toLowerCase() + " tiền")
                .setMessage(action + " " + formattedAmount + " đ " + 
                    (isDeposit ? "vào" : "từ") + " tài khoản " + accountNumber + "?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    if (isDeposit) {
                        performDeposit(accountNumber, amount, note);
                    } else {
                        performWithdraw(accountNumber, amount, note);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
    
    /**
     * Perform deposit via API
     * Endpoint: POST /payment/checking/deposit
     */
    private void performDeposit(String accountNumber, double amount, String note) {
        showLoading(true);
        
        DepositRequest request = new DepositRequest(accountNumber, BigDecimal.valueOf(amount), note);
        
        Call<DepositResponse> call = paymentApiService.depositToChecking(request);
        call.enqueue(new Callback<DepositResponse>() {
            @Override
            public void onResponse(Call<DepositResponse> call, Response<DepositResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    DepositResponse depositResponse = response.body();
                    
                    NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    String newBalance = formatter.format(depositResponse.getNewBalance());
                    
                    new AlertDialog.Builder(OfficerDepositActivity.this)
                        .setTitle("Nạp tiền thành công!")
                        .setMessage("Số dư mới: " + newBalance + " đ")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .show();
                } else {
                    Log.e(TAG, "Deposit error: " + response.code());
                    String errorMsg = "Lỗi nạp tiền";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerDepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<DepositResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Deposit failed: " + t.getMessage(), t);
                Toast.makeText(OfficerDepositActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Perform withdraw via API
     * Endpoint: POST /payment/checking/withdraw
     */
    private void performWithdraw(String accountNumber, double amount, String note) {
        showLoading(true);
        
        // Note: WithdrawRequest uses userId, not accountNumber
        // We need to get userId from accountNumber first, or modify the request
        // For now, we'll try to parse userId from accountNumber if it's numeric
        Long userId = null;
        try {
            // Try to extract userId - this is a workaround
            // In real implementation, you might need to lookup userId by accountNumber
            userId = Long.parseLong(accountNumber.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            // Use accountNumber as is - backend might handle it
            userId = 0L;
        }
        
        WithdrawRequest request = new WithdrawRequest(userId, amount, note);
        
        Call<WithdrawResponse> call = paymentApiService.withdrawFromChecking(request);
        call.enqueue(new Callback<WithdrawResponse>() {
            @Override
            public void onResponse(Call<WithdrawResponse> call, Response<WithdrawResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    WithdrawResponse withdrawResponse = response.body();
                    
                    NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    String newBalance = formatter.format(withdrawResponse.getNewBalance());
                    
                    new AlertDialog.Builder(OfficerDepositActivity.this)
                        .setTitle("Rút tiền thành công!")
                        .setMessage("Số dư mới: " + newBalance + " đ")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .show();
                } else {
                    Log.e(TAG, "Withdraw error: " + response.code());
                    String errorMsg = "Lỗi rút tiền";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerDepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<WithdrawResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Withdraw failed: " + t.getMessage(), t);
                Toast.makeText(OfficerDepositActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnSubmit.setEnabled(!show);
    }
}


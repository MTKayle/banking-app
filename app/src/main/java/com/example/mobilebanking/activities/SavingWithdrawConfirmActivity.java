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
import com.example.mobilebanking.api.dto.WithdrawConfirmResponse;
import com.example.mobilebanking.api.dto.WithdrawPreviewResponse;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity xác nhận rút tiền tiết kiệm
 */
public class SavingWithdrawConfirmActivity extends AppCompatActivity {

    private TextView tvSavingBookNumber, tvPrincipalAmount, tvInterestRate;
    private TextView tvInterestEarned, tvTotalAmount, tvOpenedDate;
    private TextView tvWithdrawDate, tvDaysHeld, tvMessage;
    private Button btnConfirm;
    
    private String savingBookNumber;
    private WithdrawPreviewResponse previewData;
    private DecimalFormat numberFormatter;
    private SimpleDateFormat dateFormatter;
    private boolean isProcessing = false;
    
    private ActivityResultLauncher<Intent> otpVerificationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_withdraw_confirm);

        savingBookNumber = getIntent().getStringExtra("savingBookNumber");
        
        if (savingBookNumber == null || savingBookNumber.isEmpty()) {
            Toast.makeText(this, "Thiếu thông tin sổ tiết kiệm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        numberFormatter = new DecimalFormat("#,###");
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        initViews();
        setupToolbar();
        setupOtpLauncher();
        loadWithdrawPreview();
    }
    
    private void setupOtpLauncher() {
        otpVerificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // OTP verified, call withdraw-confirm API
                        callWithdrawConfirmApi();
                    } else {
                        Toast.makeText(this, "Xác thực OTP thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initViews() {
        tvSavingBookNumber = findViewById(R.id.tv_saving_book_number);
        tvPrincipalAmount = findViewById(R.id.tv_principal_amount);
        tvInterestRate = findViewById(R.id.tv_interest_rate);
        tvInterestEarned = findViewById(R.id.tv_interest_earned);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvOpenedDate = findViewById(R.id.tv_opened_date);
        tvWithdrawDate = findViewById(R.id.tv_withdraw_date);
        tvDaysHeld = findViewById(R.id.tv_days_held);
        tvMessage = findViewById(R.id.tv_message);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xác nhận rút tiền");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadWithdrawPreview() {
        AccountApiService service = ApiClient.getAccountApiService();
        service.getWithdrawPreview(savingBookNumber).enqueue(new Callback<WithdrawPreviewResponse>() {
            @Override
            public void onResponse(Call<WithdrawPreviewResponse> call, Response<WithdrawPreviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    previewData = response.body();
                    displayPreview(previewData);
                } else {
                    Toast.makeText(SavingWithdrawConfirmActivity.this, 
                            "Không thể tải thông tin rút tiền", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<WithdrawPreviewResponse> call, Throwable t) {
                Toast.makeText(SavingWithdrawConfirmActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayPreview(WithdrawPreviewResponse preview) {
        // Số sổ
        tvSavingBookNumber.setText(preview.getSavingBookNumber());
        
        // Số tiền gốc
        tvPrincipalAmount.setText(formatCurrency(preview.getPrincipalAmount()) + " VNĐ");
        
        // Lãi suất áp dụng
        tvInterestRate.setText(String.format(Locale.getDefault(), "%.2f%%/năm", 
                preview.getAppliedInterestRate()));
        
        // Tiền lãi
        tvInterestEarned.setText(formatCurrency(preview.getInterestEarned()) + " VNĐ");
        
        // Tổng tiền nhận
        tvTotalAmount.setText(formatCurrency(preview.getTotalAmount()) + " VNĐ");
        
        // Ngày mở
        tvOpenedDate.setText(formatDate(preview.getOpenedDate()));
        
        // Ngày rút
        tvWithdrawDate.setText(formatDate(preview.getWithdrawDate()));
        
        // Số ngày gửi
        tvDaysHeld.setText(preview.getDaysHeld() + " ngày");
        
        // Thông báo (màu đỏ nếu rút trước hạn)
        tvMessage.setText(preview.getMessage());
        if (Boolean.TRUE.equals(preview.getEarlyWithdrawal())) {
            tvMessage.setTextColor(getResources().getColor(R.color.red_negative, null));
        }
        
        // Setup button
        btnConfirm.setOnClickListener(v -> confirmWithdraw());
    }

    private void confirmWithdraw() {
        if (isProcessing) return;
        
        // Navigate to OTP verification
        DataManager dataManager = DataManager.getInstance(this);
        String phone = dataManager.getUserPhone();
        
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("verificationType", "SAVING_WITHDRAW");
        intent.putExtra("phone", phone);
        intent.putExtra("savingBookNumber", savingBookNumber);
        otpVerificationLauncher.launch(intent);
    }
    
    private void callWithdrawConfirmApi() {
        if (isProcessing) return;
        isProcessing = true;
        
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Đang xử lý...");
        
        AccountApiService service = ApiClient.getAccountApiService();
        service.confirmWithdraw(savingBookNumber).enqueue(new Callback<WithdrawConfirmResponse>() {
            @Override
            public void onResponse(Call<WithdrawConfirmResponse> call, Response<WithdrawConfirmResponse> response) {
                isProcessing = false;
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận rút tiền");
                
                if (response.isSuccessful() && response.body() != null) {
                    WithdrawConfirmResponse confirmResponse = response.body();
                    navigateToSuccess(confirmResponse);
                } else {
                    Toast.makeText(SavingWithdrawConfirmActivity.this, 
                            "Rút tiền thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WithdrawConfirmResponse> call, Throwable t) {
                isProcessing = false;
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Xác nhận rút tiền");
                
                Toast.makeText(SavingWithdrawConfirmActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToSuccess(WithdrawConfirmResponse confirmResponse) {
        Intent intent = new Intent(this, SavingWithdrawSuccessActivity.class);
        intent.putExtra("savingBookNumber", confirmResponse.getSavingBookNumber());
        intent.putExtra("totalAmount", confirmResponse.getTotalAmount());
        intent.putExtra("interestEarned", confirmResponse.getInterestEarned());
        intent.putExtra("withdrawDate", formatDate(confirmResponse.getClosedDate()));
        intent.putExtra("transactionCode", confirmResponse.getTransactionCode());
        intent.putExtra("message", confirmResponse.getMessage());
        intent.putExtra("checkingAccountNumber", confirmResponse.getCheckingAccountNumber());
        intent.putExtra("newCheckingBalance", confirmResponse.getNewCheckingBalance());
        
        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "0";
        return numberFormatter.format(amount);
    }

    private String formatDate(String date) {
        if (date == null) return "";
        // Nếu đã là format dd/MM/yyyy thì return luôn
        if (date.contains("/")) {
            return date;
        }
        // Parse ISO format
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormatter.format(isoFormat.parse(date));
        } catch (Exception e) {
            return date;
        }
    }
}

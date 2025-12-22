package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;
import com.example.mobilebanking.api.dto.MortgagePaymentRequest;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.utils.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MortgagePaymentConfirmActivity extends AppCompatActivity {
    
    private TextView tvMortgageAccount, tvPeriodInfo, tvPaymentAmount;
    private TextView tvPaymentAccount, tvCurrentBalance;
    private Button btnCancel, btnConfirm;
    
    private Long mortgageId;
    private String mortgageAccountNumber;
    private Double paymentAmount;
    private String paymentAccountNumber;
    private Integer periodNumber;
    private Double currentBalance;
    private NumberFormat currencyFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mortgage_payment_confirm);
        
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        // Get data from intent
        mortgageId = getIntent().getLongExtra("MORTGAGE_ID", 0);
        mortgageAccountNumber = getIntent().getStringExtra("MORTGAGE_ACCOUNT");
        paymentAmount = getIntent().getDoubleExtra("PAYMENT_AMOUNT", 0);
        periodNumber = getIntent().getIntExtra("PERIOD_NUMBER", 0);
        
        initViews();
        setupToolbar();
        loadAccountBalance();
        displayInfo();
        setupClickListeners();
    }
    
    private void initViews() {
        tvMortgageAccount = findViewById(R.id.tv_mortgage_account);
        tvPeriodInfo = findViewById(R.id.tv_period_info);
        tvPaymentAmount = findViewById(R.id.tv_payment_amount);
        tvPaymentAccount = findViewById(R.id.tv_payment_account);
        tvCurrentBalance = findViewById(R.id.tv_current_balance);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadAccountBalance() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        Long userId = DataManager.getInstance(this).getUserId();
        
        AccountApiService service = ApiClient.getAccountApiService();
        service.getCheckingAccountInfo(userId).enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse account = response.body();
                    paymentAccountNumber = account.getAccountNumber();
                    currentBalance = account.getBalance() != null ? account.getBalance().doubleValue() : 0.0;
                    
                    tvPaymentAccount.setText(paymentAccountNumber);
                    tvCurrentBalance.setText(formatCurrency(currentBalance));
                }
            }
            
            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                Toast.makeText(MortgagePaymentConfirmActivity.this, 
                        "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayInfo() {
        tvMortgageAccount.setText(mortgageAccountNumber);
        tvPeriodInfo.setText("Kỳ " + periodNumber);
        tvPaymentAmount.setText(formatCurrency(paymentAmount));
    }
    
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());
        
        btnConfirm.setOnClickListener(v -> {
            // Kiểm tra số dư
            if (currentBalance == null || currentBalance < paymentAmount) {
                Toast.makeText(this, "Số dư tài khoản không đủ để thanh toán", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Kiểm tra nếu > 10 triệu thì cần xác thực khuôn mặt
            if (paymentAmount > 10000000) {
                // Xác thực khuôn mặt trước
                Intent intent = new Intent(this, FaceVerificationTransactionActivity.class);
                intent.putExtra("from", "MORTGAGE_PAYMENT");  // Sửa từ TRANSACTION_TYPE thành from
                intent.putExtra("MORTGAGE_ID", mortgageId);
                intent.putExtra("PAYMENT_AMOUNT", paymentAmount);
                intent.putExtra("PAYMENT_ACCOUNT", paymentAccountNumber);
                intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccountNumber);
                intent.putExtra("PERIOD_NUMBER", periodNumber);
                startActivity(intent);
                finish();
            } else {
                // Chỉ cần OTP
                sendOtpAndVerify();
            }
        });
    }
    
    private void sendOtpAndVerify() {
        DataManager dataManager = DataManager.getInstance(this);
        
        // Lấy phone từ nhiều nguồn
        String phone = dataManager.getUserPhone();
        if (phone == null || phone.isEmpty()) {
            phone = dataManager.getLastUsername(); // Username thường là phone
        }
        
        android.util.Log.d("MortgagePayment", "Phone for OTP: " + phone);
        
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy số điện thoại. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Gửi OTP
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("PHONE_NUMBER", phone);
        intent.putExtra("FROM_ACTIVITY", "MORTGAGE_PAYMENT");
        intent.putExtra("MORTGAGE_ID", mortgageId);
        intent.putExtra("PAYMENT_AMOUNT", paymentAmount);
        intent.putExtra("PAYMENT_ACCOUNT", paymentAccountNumber);
        intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccountNumber);
        intent.putExtra("PERIOD_NUMBER", periodNumber);
        startActivity(intent);
        finish();
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "0 đ";
        return currencyFormatter.format(amount) + " đ";
    }
}

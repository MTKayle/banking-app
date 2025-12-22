package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.Locale;

public class MortgageSettlementConfirmActivity extends AppCompatActivity {
    
    private TextView tvMortgageAccount, tvSettlementAmount;
    private TextView tvPaymentAccount, tvCurrentBalance;
    private Button btnCancel, btnConfirm;
    
    private Long mortgageId;
    private String mortgageAccountNumber;
    private Double settlementAmount;
    private String paymentAccountNumber;
    private Double currentBalance;
    private NumberFormat currencyFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mortgage_settlement_confirm);
        
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        // Get data from intent
        mortgageId = getIntent().getLongExtra("MORTGAGE_ID", 0);
        mortgageAccountNumber = getIntent().getStringExtra("MORTGAGE_ACCOUNT");
        settlementAmount = getIntent().getDoubleExtra("SETTLEMENT_AMOUNT", 0);
        paymentAccountNumber = getIntent().getStringExtra("PAYMENT_ACCOUNT");
        currentBalance = getIntent().getDoubleExtra("CURRENT_BALANCE", 0);
        
        initViews();
        setupToolbar();
        displayInfo();
        setupClickListeners();
    }
    
    private void initViews() {
        tvMortgageAccount = findViewById(R.id.tv_mortgage_account);
        tvSettlementAmount = findViewById(R.id.tv_settlement_amount);
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
    
    private void displayInfo() {
        tvMortgageAccount.setText(mortgageAccountNumber);
        tvSettlementAmount.setText(formatCurrency(settlementAmount));
        tvPaymentAccount.setText(paymentAccountNumber);
        tvCurrentBalance.setText(formatCurrency(currentBalance));
    }
    
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());
        
        btnConfirm.setOnClickListener(v -> {
            // Kiểm tra số dư lần nữa
            if (currentBalance == null || currentBalance < settlementAmount) {
                Toast.makeText(this, "Số dư tài khoản không đủ để tất toán", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Kiểm tra nếu > 10 triệu thì cần xác thực khuôn mặt
            if (settlementAmount > 10000000) {
                // Xác thực khuôn mặt trước
                Intent intent = new Intent(this, FaceVerificationTransactionActivity.class);
                intent.putExtra("from", "MORTGAGE_SETTLEMENT");
                intent.putExtra("MORTGAGE_ID", mortgageId);
                intent.putExtra("SETTLEMENT_AMOUNT", settlementAmount);
                intent.putExtra("PAYMENT_ACCOUNT", paymentAccountNumber);
                intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccountNumber);
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
        
        android.util.Log.d("MortgageSettlement", "Phone for OTP: " + phone);
        
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy số điện thoại. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Gửi OTP
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("PHONE_NUMBER", phone);
        intent.putExtra("FROM_ACTIVITY", "MORTGAGE_SETTLEMENT");
        intent.putExtra("MORTGAGE_ID", mortgageId);
        intent.putExtra("SETTLEMENT_AMOUNT", settlementAmount);
        intent.putExtra("PAYMENT_ACCOUNT", paymentAccountNumber);
        intent.putExtra("MORTGAGE_ACCOUNT", mortgageAccountNumber);
        startActivity(intent);
        finish();
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "0 đ";
        return currencyFormatter.format(amount) + " đ";
    }
}

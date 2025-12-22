package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.ui_home.UiHomeActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class MortgagePaymentSuccessActivity extends AppCompatActivity {
    
    private TextView tvMortgageAccount, tvPeriodInfo, tvPaymentAmount;
    private TextView tvPaymentAccount, tvRemainingBalance;
    private Button btnBack;
    
    private NumberFormat currencyFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mortgage_payment_success);
        
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        initViews();
        displayInfo();
        setupClickListeners();
    }
    
    private void initViews() {
        tvMortgageAccount = findViewById(R.id.tv_mortgage_account);
        tvPeriodInfo = findViewById(R.id.tv_period_info);
        tvPaymentAmount = findViewById(R.id.tv_payment_amount);
        tvPaymentAccount = findViewById(R.id.tv_payment_account);
        tvRemainingBalance = findViewById(R.id.tv_remaining_balance);
        btnBack = findViewById(R.id.btn_back);
    }
    
    private void displayInfo() {
        String mortgageAccount = getIntent().getStringExtra("MORTGAGE_ACCOUNT");
        Integer periodNumber = getIntent().getIntExtra("PERIOD_NUMBER", 0);
        Double paymentAmount = getIntent().getDoubleExtra("PAYMENT_AMOUNT", 0);
        String paymentAccount = getIntent().getStringExtra("PAYMENT_ACCOUNT");
        Double remainingBalance = getIntent().getDoubleExtra("REMAINING_BALANCE", 0);
        
        tvMortgageAccount.setText(mortgageAccount);
        tvPeriodInfo.setText("Kỳ " + periodNumber);
        tvPaymentAmount.setText(formatCurrency(paymentAmount));
        tvPaymentAccount.setText(paymentAccount);
        tvRemainingBalance.setText(formatCurrency(remainingBalance));
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            // Quay về màn hình tài khoản -> tiền vay
            Intent intent = new Intent(this, UiHomeActivity.class);
            intent.putExtra("NAVIGATE_TO", "MORTGAGE_ACCOUNT");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "0 đ";
        return currencyFormatter.format(amount) + " đ";
    }
    
    @Override
    public void onBackPressed() {
        // Chặn back button, bắt buộc dùng nút quay lại
        btnBack.performClick();
    }
}

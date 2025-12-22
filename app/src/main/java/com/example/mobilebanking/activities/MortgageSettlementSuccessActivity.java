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

public class MortgageSettlementSuccessActivity extends AppCompatActivity {
    
    private TextView tvMortgageAccount, tvSettlementAmount;
    private TextView tvPaymentAccount;
    private Button btnBack;
    
    private NumberFormat currencyFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mortgage_settlement_success);
        
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        initViews();
        displayInfo();
        setupClickListeners();
    }
    
    private void initViews() {
        tvMortgageAccount = findViewById(R.id.tv_mortgage_account);
        tvSettlementAmount = findViewById(R.id.tv_settlement_amount);
        tvPaymentAccount = findViewById(R.id.tv_payment_account);
        btnBack = findViewById(R.id.btn_back);
    }
    
    private void displayInfo() {
        String mortgageAccount = getIntent().getStringExtra("MORTGAGE_ACCOUNT");
        Double settlementAmount = getIntent().getDoubleExtra("SETTLEMENT_AMOUNT", 0);
        String paymentAccount = getIntent().getStringExtra("PAYMENT_ACCOUNT");
        
        tvMortgageAccount.setText(mortgageAccount);
        tvSettlementAmount.setText(formatCurrency(settlementAmount));
        tvPaymentAccount.setText(paymentAccount);
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

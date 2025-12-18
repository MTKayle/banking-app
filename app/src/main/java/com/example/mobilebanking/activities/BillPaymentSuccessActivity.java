package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Bill Payment Success Activity - Display success confirmation after bill payment
 * MB Bank style with dark mode and green theme
 */
public class BillPaymentSuccessActivity extends AppCompatActivity {
    
    private TextView tvProviderName, tvBillType, tvCustomerCode, tvBillingPeriod, tvAmountPaid;
    private TextView tvTransactionId, tvTransactionTime, tvPaymentAccount, tvPaymentMethod, tvTransactionStatus;
    private TextView tvBalanceBefore, tvAmountDeducted, tvBalanceAfter;
    private Button btnViewDetails, btnBackHome;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment_success_dark);
        
        initializeViews();
        loadDataFromIntent();
        setupClickListeners();
    }
    
    private void initializeViews() {
        // Bill info
        tvProviderName = findViewById(R.id.tv_provider_name);
        tvBillType = findViewById(R.id.tv_bill_type);
        tvCustomerCode = findViewById(R.id.tv_customer_code);
        tvBillingPeriod = findViewById(R.id.tv_billing_period);
        tvAmountPaid = findViewById(R.id.tv_amount_paid);
        
        // Transaction details
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvTransactionTime = findViewById(R.id.tv_transaction_time);
        tvPaymentAccount = findViewById(R.id.tv_payment_account);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvTransactionStatus = findViewById(R.id.tv_transaction_status);
        
        // Balance update
        tvBalanceBefore = findViewById(R.id.tv_balance_before);
        tvAmountDeducted = findViewById(R.id.tv_amount_deducted);
        tvBalanceAfter = findViewById(R.id.tv_balance_after);
        
        // Buttons
        btnViewDetails = findViewById(R.id.btn_view_details);
        btnBackHome = findViewById(R.id.btn_back_home);
    }
    
    private void loadDataFromIntent() {
        Intent intent = getIntent();
        
        // Get bill information
        String providerName = intent.getStringExtra("provider_name");
        String billType = intent.getStringExtra("bill_type");
        String customerCode = intent.getStringExtra("customer_code");
        String billingPeriod = intent.getStringExtra("billing_period");
        String amount = intent.getStringExtra("amount");
        String accountNumber = intent.getStringExtra("account_number");
        double balanceBefore = intent.getDoubleExtra("balance_before", 50000000);
        
        // Set bill info
        if (providerName != null) tvProviderName.setText(providerName);
        if (billType != null) tvBillType.setText(billType);
        if (customerCode != null) tvCustomerCode.setText(customerCode);
        if (billingPeriod != null) tvBillingPeriod.setText(billingPeriod);
        if (amount != null) tvAmountPaid.setText(amount);
        if (accountNumber != null) tvPaymentAccount.setText(accountNumber);
        
        // Generate transaction ID
        String transactionId = "REF" + generateRandomNumber(9);
        tvTransactionId.setText(transactionId);
        
        // Set current time
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        tvTransactionTime.setText(sdf.format(new Date()));
        
        // Set payment method
        tvPaymentMethod.setText("Chuyển khoản");
        
        // Set status
        tvTransactionStatus.setText("Thành công");
        
        // Calculate balance update
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Parse amount (remove currency symbol and spaces)
        double amountValue = parseAmount(amount);
        double balanceAfter = balanceBefore - amountValue;
        
        tvBalanceBefore.setText(formatter.format(balanceBefore));
        tvAmountDeducted.setText("-" + (amount != null ? amount : "0 ₫"));
        tvBalanceAfter.setText(formatter.format(balanceAfter));
    }
    
    private double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) return 0;
        try {
            // Remove spaces, currency symbol, and parse
            String cleanAmount = amountStr.replaceAll("[\\s₫,]", "");
            return Double.parseDouble(cleanAmount);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    private void setupClickListeners() {
        // View details button - Navigate to Transaction History
        if (btnViewDetails != null) {
            btnViewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(this, TransactionHistoryActivity.class);
                startActivity(intent);
                finish();
            });
        }
        
        // Back to home button
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> {
                // Navigate to home (UiHomeActivity)
                Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button - force user to use action buttons
        // Or navigate to home
        Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}


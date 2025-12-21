package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.ui_home.UiHomeActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Activity hiển thị kết quả rút tiền thành công
 */
public class SavingWithdrawSuccessActivity extends AppCompatActivity {

    private TextView tvSavingBookNumber, tvTotalAmount, tvInterestEarned;
    private TextView tvWithdrawDate, tvTransactionId, tvMessage;
    private TextView tvCheckingAccount, tvNewBalance;
    private Button btnDone;
    
    private DecimalFormat numberFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_withdraw_success);

        numberFormatter = new DecimalFormat("#,###");

        initViews();
        displayInfo();
        setupListeners();
    }

    private void initViews() {
        tvSavingBookNumber = findViewById(R.id.tv_saving_book_number);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvInterestEarned = findViewById(R.id.tv_interest_earned);
        tvWithdrawDate = findViewById(R.id.tv_withdraw_date);
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvMessage = findViewById(R.id.tv_message);
        tvCheckingAccount = findViewById(R.id.tv_checking_account);
        tvNewBalance = findViewById(R.id.tv_new_balance);
        btnDone = findViewById(R.id.btn_done);
    }

    private void displayInfo() {
        // Get data from intent
        String savingBookNumber = getIntent().getStringExtra("savingBookNumber");
        double totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
        double interestEarned = getIntent().getDoubleExtra("interestEarned", 0.0);
        String withdrawDate = getIntent().getStringExtra("withdrawDate");
        String transactionCode = getIntent().getStringExtra("transactionCode");
        String message = getIntent().getStringExtra("message");
        String checkingAccountNumber = getIntent().getStringExtra("checkingAccountNumber");
        double newCheckingBalance = getIntent().getDoubleExtra("newCheckingBalance", 0.0);

        // Display
        tvSavingBookNumber.setText(savingBookNumber != null ? savingBookNumber : "N/A");
        tvTotalAmount.setText(formatCurrency(totalAmount) + " VNĐ");
        tvInterestEarned.setText(formatCurrency(interestEarned) + " VNĐ");
        tvWithdrawDate.setText(withdrawDate != null ? withdrawDate : "N/A");
        tvTransactionId.setText(transactionCode != null ? transactionCode : "N/A");
        
        // Display message (màu đỏ)
        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
            tvMessage.setVisibility(android.view.View.VISIBLE);
        } else {
            tvMessage.setVisibility(android.view.View.GONE);
        }
        
        // Display checking account info
        if (checkingAccountNumber != null && !checkingAccountNumber.isEmpty()) {
            tvCheckingAccount.setText(checkingAccountNumber);
            tvCheckingAccount.setVisibility(android.view.View.VISIBLE);
        } else {
            tvCheckingAccount.setVisibility(android.view.View.GONE);
        }
        
        if (newCheckingBalance > 0) {
            tvNewBalance.setText(formatCurrency(newCheckingBalance) + " VNĐ");
            tvNewBalance.setVisibility(android.view.View.VISIBLE);
        } else {
            tvNewBalance.setVisibility(android.view.View.GONE);
        }
    }

    private void setupListeners() {
        btnDone.setOnClickListener(v -> navigateToHome());
    }

    private void navigateToHome() {
        // Navigate to UiHome, then it will open Account with Saving tab
        Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
        intent.putExtra("OPEN_ACCOUNT_TAB", 1); // 1 = Tiết kiệm tab
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String formatCurrency(double amount) {
        return numberFormatter.format(amount);
    }

    @Override
    public void onBackPressed() {
        // Navigate to home instead of going back
        navigateToHome();
    }
}

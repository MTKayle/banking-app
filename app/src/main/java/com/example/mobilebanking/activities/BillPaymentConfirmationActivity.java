package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Bill Payment Confirmation Activity
 * Display bill details for user review before payment
 */
public class BillPaymentConfirmationActivity extends AppCompatActivity {

    // Intent extras keys
    public static final String EXTRA_PROVIDER_NAME = "provider_name";
    public static final String EXTRA_BILL_TYPE = "bill_type";
    public static final String EXTRA_BILL_CODE = "bill_code";
    public static final String EXTRA_BILLING_PERIOD = "billing_period";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_ACCOUNT_NUMBER = "account_number";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_IS_RECURRING = "is_recurring";
    public static final String EXTRA_REFERRAL_CODE = "referral_code";

    // Views
    private ImageButton btnBack;
    private TextView tvAccountNumber, tvBalance;
    private TextView tvBillType, tvProviderName, tvBillCode, tvBillingPeriod, tvDueDate;
    private TextView tvReferralCode, tvTotalAmount;
    private LinearLayout layoutRecurringInfo, layoutReferralInfo;
    private Button btnConfirmPayment;

    // Data
    private String providerName, billType, billCode, billingPeriod, amount;
    private String accountNumber, userName, referralCode;
    private boolean isRecurring;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment_confirmation);

        dataManager = DataManager.getInstance(this);

        initializeViews();
        loadDataFromIntent();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header
        btnBack = findViewById(R.id.btn_back);

        // Account info
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvBalance = findViewById(R.id.tv_balance);

        // Bill details
        tvBillType = findViewById(R.id.tv_bill_type);
        tvProviderName = findViewById(R.id.tv_provider_name);
        tvBillCode = findViewById(R.id.tv_bill_code);
        tvBillingPeriod = findViewById(R.id.tv_billing_period);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvReferralCode = findViewById(R.id.tv_referral_code);
        tvTotalAmount = findViewById(R.id.tv_total_amount);

        // Optional sections
        layoutRecurringInfo = findViewById(R.id.layout_recurring_info);
        layoutReferralInfo = findViewById(R.id.layout_referral_info);

        // Bottom button
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();

        // Get data from intent
        providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        billType = intent.getStringExtra(EXTRA_BILL_TYPE);
        billCode = intent.getStringExtra(EXTRA_BILL_CODE);
        billingPeriod = intent.getStringExtra(EXTRA_BILLING_PERIOD);
        amount = intent.getStringExtra(EXTRA_AMOUNT);
        accountNumber = intent.getStringExtra(EXTRA_ACCOUNT_NUMBER);
        userName = intent.getStringExtra(EXTRA_USER_NAME);
        isRecurring = intent.getBooleanExtra(EXTRA_IS_RECURRING, false);
        referralCode = intent.getStringExtra(EXTRA_REFERRAL_CODE);

        // Display account info
        if (accountNumber != null) {
            tvAccountNumber.setText("TK người: " + accountNumber);
        }

        // Get balance from DataManager (mock)
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvBalance.setText("Số dư: " + formatter.format(252827) + " VND");

        // Display bill details
        if (billType != null) {
            tvBillType.setText(billType);
        }

        if (providerName != null) {
            tvProviderName.setText(providerName);
        }

        if (billCode != null) {
            tvBillCode.setText(billCode);
        }

        if (billingPeriod != null) {
            tvBillingPeriod.setText(billingPeriod);
        }

        // Mock due date
        tvDueDate.setText("25/12/2024");

        // Display optional info
        if (isRecurring) {
            layoutRecurringInfo.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(referralCode)) {
            layoutReferralInfo.setVisibility(View.VISIBLE);
            tvReferralCode.setText(referralCode);
        }

        // Display total amount
        if (amount != null) {
            tvTotalAmount.setText(formatter.format(Double.parseDouble(amount)) + " VND");
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Confirm payment button
        btnConfirmPayment.setOnClickListener(v -> handleConfirmPayment());
    }

    /**
     * Handle confirm payment
     */
    private void handleConfirmPayment() {
        // Show final confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn có chắc chắn muốn thanh toán hóa đơn này?\n\n" +
                       "Số tiền: " + tvTotalAmount.getText().toString())
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                // Process payment and navigate to success screen
                processPayment();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Process payment (mock) and navigate to success screen
     */
    private void processPayment() {
        // In real app, call payment API here
        // For now, just navigate to success screen

        Intent intent = new Intent(this, BillPaymentSuccessActivity.class);

        // Pass data to success screen
        intent.putExtra("provider_name", providerName);
        intent.putExtra("bill_type", billType);
        intent.putExtra("bill_code", billCode);
        intent.putExtra("billing_period", billingPeriod);
        intent.putExtra("amount", amount);
        intent.putExtra("account_number", accountNumber);
        intent.putExtra("user_name", userName);
        intent.putExtra("is_recurring", isRecurring);
        intent.putExtra("referral_code", referralCode);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


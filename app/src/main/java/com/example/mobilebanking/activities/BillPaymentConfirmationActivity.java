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
    public static final String EXTRA_DUE_DATE = "due_date";
    public static final String EXTRA_BILL_ID = "bill_id";

    // Views
    private ImageButton btnBack;
    private TextView tvAccountNumber, tvBalance;
    private TextView tvBillType, tvProviderName, tvBillCode, tvBillingPeriod, tvDueDate;
    private TextView tvCustomerName, tvCustomerAddress, tvCustomerPhone;
    private TextView tvUsageLabel, tvUsageAmount, tvUnitPriceLabel, tvUnitPrice;
    private TextView tvAmount, tvVat, tvStatus, tvNotes;
    private TextView tvReferralCode, tvTotalAmount;
    private LinearLayout layoutRecurringInfo, layoutReferralInfo, layoutNotes;
    private Button btnConfirmPayment;

    // Data
    private String providerName, billType, billCode, billingPeriod, amount, dueDate;
    private String accountNumber, userName, referralCode;
    private double currentBalance = 0;
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
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerAddress = findViewById(R.id.tv_customer_address);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvBillingPeriod = findViewById(R.id.tv_billing_period);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvUsageLabel = findViewById(R.id.tv_usage_label);
        tvUsageAmount = findViewById(R.id.tv_usage_amount);
        tvUnitPriceLabel = findViewById(R.id.tv_unit_price_label);
        tvUnitPrice = findViewById(R.id.tv_unit_price);
        tvAmount = findViewById(R.id.tv_amount);
        tvVat = findViewById(R.id.tv_vat);
        tvStatus = findViewById(R.id.tv_status);
        tvNotes = findViewById(R.id.tv_notes);
        tvReferralCode = findViewById(R.id.tv_referral_code);
        tvTotalAmount = findViewById(R.id.tv_total_amount);

        // Optional sections
        layoutRecurringInfo = findViewById(R.id.layout_recurring_info);
        layoutReferralInfo = findViewById(R.id.layout_referral_info);
        layoutNotes = findViewById(R.id.layout_notes);

        // Bottom button
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Get data from intent
        providerName = intent.getStringExtra(EXTRA_PROVIDER_NAME);
        billType = intent.getStringExtra(EXTRA_BILL_TYPE);
        String billTypeValue = intent.getStringExtra("EXTRA_BILL_TYPE_VALUE");
        billCode = intent.getStringExtra(EXTRA_BILL_CODE);
        billingPeriod = intent.getStringExtra(EXTRA_BILLING_PERIOD);
        amount = intent.getStringExtra(EXTRA_AMOUNT);
        accountNumber = intent.getStringExtra(EXTRA_ACCOUNT_NUMBER);
        userName = intent.getStringExtra(EXTRA_USER_NAME);
        isRecurring = intent.getBooleanExtra(EXTRA_IS_RECURRING, false);
        referralCode = intent.getStringExtra(EXTRA_REFERRAL_CODE);
        dueDate = intent.getStringExtra(EXTRA_DUE_DATE);

        // Get additional data
        String customerName = intent.getStringExtra("EXTRA_CUSTOMER_NAME");
        String customerAddress = intent.getStringExtra("EXTRA_CUSTOMER_ADDRESS");
        String customerPhone = intent.getStringExtra("EXTRA_CUSTOMER_PHONE");
        int usageAmount = intent.getIntExtra("EXTRA_USAGE_AMOUNT", 0);
        String unitPrice = intent.getStringExtra("EXTRA_UNIT_PRICE");
        String amountBeforeVat = intent.getStringExtra("EXTRA_AMOUNT_BEFORE_VAT");
        String vat = intent.getStringExtra("EXTRA_VAT");
        String statusDisplay = intent.getStringExtra("EXTRA_STATUS_DISPLAY");
        String notes = intent.getStringExtra("EXTRA_NOTES");

        // Display account info
        if (accountNumber != null) {
            tvAccountNumber.setText("TK người: " + accountNumber);
        }

        // Get balance from intent or API
        String balanceStr = intent.getStringExtra("EXTRA_BALANCE");
        if (!TextUtils.isEmpty(balanceStr)) {
            try {
                currentBalance = Double.parseDouble(balanceStr);
                tvBalance.setText("Số dư: " + formatter.format(currentBalance) + " VND");
            } catch (NumberFormatException e) {
                tvBalance.setText("Số dư: " + formatter.format(252827) + " VND");
            }
        } else {
            tvBalance.setText("Số dư: " + formatter.format(252827) + " VND");
        }

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

        // Customer info
        if (!TextUtils.isEmpty(customerName)) {
            tvCustomerName.setText(customerName);
        }

        if (!TextUtils.isEmpty(customerAddress)) {
            tvCustomerAddress.setText(customerAddress);
        }

        if (!TextUtils.isEmpty(customerPhone)) {
            tvCustomerPhone.setText(customerPhone);
        }

        if (billingPeriod != null) {
            tvBillingPeriod.setText(billingPeriod);
        }

        // Display due date from API or use default
        if (!TextUtils.isEmpty(dueDate)) {
            tvDueDate.setText(dueDate);
        } else {
            tvDueDate.setText("25/12/2024");
        }

        // Usage amount - change label based on bill type
        if ("ELECTRICITY".equals(billTypeValue)) {
            tvUsageLabel.setText("Số điện sử dụng");
            tvUnitPriceLabel.setText("Đơn giá/kWh");
            tvUsageAmount.setText(usageAmount + " kWh");
        } else if ("WATER".equals(billTypeValue)) {
            tvUsageLabel.setText("Số nước sử dụng");
            tvUnitPriceLabel.setText("Đơn giá/m³");
            tvUsageAmount.setText(usageAmount + " m³");
        } else {
            tvUsageAmount.setText(String.valueOf(usageAmount));
        }

        // Unit price
        if (!TextUtils.isEmpty(unitPrice)) {
            try {
                double price = Double.parseDouble(unitPrice);
                tvUnitPrice.setText(formatter.format(price) + " VND");
            } catch (NumberFormatException e) {
                tvUnitPrice.setText(unitPrice);
            }
        }

        // Amount before VAT
        if (!TextUtils.isEmpty(amountBeforeVat)) {
            try {
                double amtValue = Double.parseDouble(amountBeforeVat);
                tvAmount.setText(formatter.format(amtValue) + " VND");
            } catch (NumberFormatException e) {
                tvAmount.setText(amountBeforeVat);
            }
        }

        // VAT
        if (!TextUtils.isEmpty(vat)) {
            try {
                double vatValue = Double.parseDouble(vat);
                tvVat.setText(formatter.format(vatValue) + " VND");
            } catch (NumberFormatException e) {
                tvVat.setText(vat);
            }
        }

        // Status
        if (!TextUtils.isEmpty(statusDisplay)) {
            tvStatus.setText(statusDisplay);
            // Color based on status
            if (statusDisplay.contains("Quá hạn") || statusDisplay.contains("OVERDUE")) {
                tvStatus.setTextColor(0xFFF44336); // Red
            } else if (statusDisplay.contains("Chưa thanh toán") || statusDisplay.contains("UNPAID")) {
                tvStatus.setTextColor(0xFFFF9800); // Orange
            } else {
                tvStatus.setTextColor(0xFF4CAF50); // Green
            }
        }

        // Notes
        if (!TextUtils.isEmpty(notes)) {
            layoutNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(notes);
        }

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
            try {
                double amountValue = Double.parseDouble(amount);
                tvTotalAmount.setText(formatter.format(amountValue) + " VND");
            } catch (NumberFormatException e) {
                tvTotalAmount.setText(amount + " VND");
            }
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
        // Check balance
        if (amount != null) {
            try {
                double totalAmount = Double.parseDouble(amount);
                if (currentBalance < totalAmount) {
                    // Insufficient balance
                    new AlertDialog.Builder(this)
                        .setTitle("Số dư không đủ")
                        .setMessage("Số dư tài khoản của bạn không đủ để thanh toán hóa đơn này.\n\n" +
                                   "Số dư hiện tại: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(currentBalance) + " VND\n" +
                                   "Số tiền cần thanh toán: " + NumberFormat.getInstance(new Locale("vi", "VN")).format(totalAmount) + " VND")
                        .setPositiveButton("Đóng", null)
                        .show();
                    return;
                }
                
                // Check if amount >= 10 million -> require face verification first
                if (totalAmount >= 10000000) {
                    // Show confirmation dialog then navigate to face verification
                    new AlertDialog.Builder(this)
                        .setTitle("Xác nhận thanh toán")
                        .setMessage("Giao dịch trên 10 triệu đồng yêu cầu xác thực khuôn mặt.\n\n" +
                                   "Số tiền: " + tvTotalAmount.getText().toString())
                        .setPositiveButton("Tiếp tục", (dialog, which) -> {
                            navigateToFaceVerification();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                } else {
                    // Show final confirmation dialog for normal amount
                    new AlertDialog.Builder(this)
                        .setTitle("Xác nhận thanh toán")
                        .setMessage("Bạn có chắc chắn muốn thanh toán hóa đơn này?\n\n" +
                                   "Số tiền: " + tvTotalAmount.getText().toString())
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            // Navigate to OTP verification
                            navigateToOtpVerification();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Lỗi: Không thể xác định số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
    
    /**
     * Navigate to face verification for high-value transactions (>= 10M)
     */
    private void navigateToFaceVerification() {
        // Get phone number from DataManager
        String userPhone = dataManager.getUserPhone();
        if (TextUtils.isEmpty(userPhone)) {
            Toast.makeText(this, "Không tìm thấy số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, FaceVerificationTransactionActivity.class);
        intent.putExtra("from", "BILL_PAYMENT");
        intent.putExtra("phone", userPhone);
        
        // Pass bill payment data
        intent.putExtra("BILL_CODE", billCode);
        intent.putExtra("BILL_TYPE", billType);
        intent.putExtra("PROVIDER_NAME", providerName);
        intent.putExtra("AMOUNT", amount);
        intent.putExtra("ACCOUNT_NUMBER", accountNumber);
        intent.putExtra("BILLING_PERIOD", billingPeriod);
        
        startActivity(intent);
        finish();
    }
    
    /**
     * Navigate to OTP verification
     */
    private void navigateToOtpVerification() {
        // Get phone number from DataManager
        String userPhone = dataManager.getUserPhone();
        if (TextUtils.isEmpty(userPhone)) {
            Toast.makeText(this, "Không tìm thấy số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, com.example.mobilebanking.activities.OtpVerificationActivity.class);
        
        // Pass transaction type
        intent.putExtra("from", "BILL_PAYMENT");
        intent.putExtra("phone", userPhone);
        
        // Pass bill payment data
        intent.putExtra("BILL_CODE", billCode);
        intent.putExtra("BILL_TYPE", billType);
        intent.putExtra("PROVIDER_NAME", providerName);
        intent.putExtra("AMOUNT", amount);
        intent.putExtra("ACCOUNT_NUMBER", accountNumber);
        intent.putExtra("BILLING_PERIOD", billingPeriod);
        
        startActivityForResult(intent, 100);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // OTP verified successfully - process payment
            if (data != null && data.getBooleanExtra("OTP_VERIFIED", false)) {
                processPayment();
            }
        }
    }
    
    /**
     * Process payment after OTP verification
     */
    private void processPayment() {
        // Show loading
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Đang xử lý...");
        
        // Call payment API
        com.example.mobilebanking.api.dto.BillPaymentRequest request = 
            new com.example.mobilebanking.api.dto.BillPaymentRequest(billCode);
        
        com.example.mobilebanking.api.ApiClient.getUtilityBillApiService()
            .payBill(request)
            .enqueue(new retrofit2.Callback<com.example.mobilebanking.api.dto.BillPaymentResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.mobilebanking.api.dto.BillPaymentResponse> call, 
                                     retrofit2.Response<com.example.mobilebanking.api.dto.BillPaymentResponse> response) {
                    btnConfirmPayment.setEnabled(true);
                    btnConfirmPayment.setText("Xác nhận thanh toán");
                    
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.mobilebanking.api.dto.BillPaymentResponse paymentResponse = response.body();
                        if (paymentResponse.getSuccess() && paymentResponse.getData() != null) {
                            // Payment successful - navigate to success screen
                            navigateToSuccessScreen(paymentResponse.getData());
                        } else {
                            Toast.makeText(BillPaymentConfirmationActivity.this, 
                                paymentResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(BillPaymentConfirmationActivity.this, 
                            "Lỗi thanh toán: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.mobilebanking.api.dto.BillPaymentResponse> call, Throwable t) {
                    btnConfirmPayment.setEnabled(true);
                    btnConfirmPayment.setText("Xác nhận thanh toán");
                    
                    Toast.makeText(BillPaymentConfirmationActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
    
    /**
     * Navigate to success screen
     */
    private void navigateToSuccessScreen(com.example.mobilebanking.api.dto.BillPaymentResponse.PaymentData paymentData) {
        Intent intent = new Intent(this, BillPaymentSuccessActivity.class);
        
        // Pass payment result data
        intent.putExtra("transaction_id", paymentData.getTransactionId());
        intent.putExtra("bill_code", paymentData.getBillCode());
        intent.putExtra("amount", paymentData.getAmount() != null ? paymentData.getAmount().toString() : amount);
        intent.putExtra("payment_time", paymentData.getPaymentTime());
        intent.putExtra("balance_after", paymentData.getBalanceAfter() != null ? 
            paymentData.getBalanceAfter().toString() : "0");
        intent.putExtra("status", paymentData.getStatus());
        intent.putExtra("message", paymentData.getMessage());
        
        // Pass bill info
        intent.putExtra("provider_name", providerName);
        intent.putExtra("bill_type", billType);
        intent.putExtra("billing_period", billingPeriod);
        intent.putExtra("account_number", accountNumber);
        intent.putExtra("user_name", userName);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


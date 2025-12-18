package com.example.mobilebanking.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Bill Payment Activity - Pay utility bills (Electricity & Water)
 * MB Bank style with dark mode and green theme
 */
public class BillPaymentActivity extends AppCompatActivity {
    
    // Bill type constants
    private static final int BILL_TYPE_NONE = 0;
    private static final int BILL_TYPE_ELECTRICITY = 1;
    private static final int BILL_TYPE_WATER = 2;
    
    // Views
    private LinearLayout llBillElectricity, llBillWater;
    private EditText etCustomerCode;
    private LinearLayout llBillDetails;
    private TextView tvProviderName, tvBillingPeriod, tvAmountDue, tvDueDate, tvBillStatus;
    private TextView tvAccountNumber, tvAvailableBalance;
    private Button btnCheckBill, btnPayNow, btnCancel;
    
    private int selectedBillType = BILL_TYPE_NONE;
    private DataManager dataManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_bill_dark);

        dataManager = DataManager.getInstance(this);
        
        initializeViews();
        loadAccountData();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header actions (MB Bank style)
        View backButton = findViewById(R.id.btn_bill_back);
        View historyButton = findViewById(R.id.btn_bill_history);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
        if (historyButton != null) {
            historyButton.setOnClickListener(v ->
                    startActivity(new Intent(this, BillHistoryActivity.class)));
        }
        
        // Bill type selection
        llBillElectricity = findViewById(R.id.ll_bill_electricity);
        llBillWater = findViewById(R.id.ll_bill_water);
        
        // Customer input
        etCustomerCode = findViewById(R.id.et_customer_code);
        
        // Bill details
        llBillDetails = findViewById(R.id.ll_bill_details);
        tvProviderName = findViewById(R.id.tv_provider_name);
        tvBillingPeriod = findViewById(R.id.tv_billing_period);
        tvAmountDue = findViewById(R.id.tv_amount_due);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvBillStatus = findViewById(R.id.tv_bill_status);
        
        // Account info
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvAvailableBalance = findViewById(R.id.tv_available_balance);
        
        // Buttons
        btnCheckBill = findViewById(R.id.btn_check_bill);
        btnPayNow = findViewById(R.id.btn_pay_now);
        btnCancel = findViewById(R.id.btn_cancel);
    }
    
    private void loadAccountData() {
        // Load checking account data
        List<Account> accounts = dataManager.getMockAccounts("U001");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        for (Account account : accounts) {
            if (account.getType() == Account.AccountType.CHECKING) {
                String maskedAccount = maskAccountNumber(account.getAccountNumber());
                tvAccountNumber.setText(maskedAccount);
                tvAvailableBalance.setText(formatter.format(account.getBalance()));
                break;
            }
        }
    }
    
    private void setupClickListeners() {
        // Bill type selection
        if (llBillElectricity != null) {
            llBillElectricity.setOnClickListener(v -> selectBillType(BILL_TYPE_ELECTRICITY));
        }
        
        if (llBillWater != null) {
            llBillWater.setOnClickListener(v -> selectBillType(BILL_TYPE_WATER));
        }
        
        // Check Bill button
        if (btnCheckBill != null) {
            btnCheckBill.setOnClickListener(v -> handleCheckBill());
        }
        
        // Pay Now button
        if (btnPayNow != null) {
            btnPayNow.setOnClickListener(v -> handlePayNow());
        }
        
        // Cancel button
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }
    }
    
    /**
     * Select bill type and update UI
     */
    private void selectBillType(int billType) {
        selectedBillType = billType;
        
        // Reset all bill type cards về trạng thái mặc định (bo tròn)
        resetBillTypeStyles();
        
        // Highlight card được chọn nhưng vẫn giữ bo tròn
        if (billType == BILL_TYPE_ELECTRICITY && llBillElectricity != null) {
            llBillElectricity.setBackgroundResource(R.drawable.bg_bill_type_pill_selected);
        } else if (billType == BILL_TYPE_WATER && llBillWater != null) {
            llBillWater.setBackgroundResource(R.drawable.bg_bill_type_pill_selected);
        }
    }
    
    /**
     * Reset bill type card styles
     */
    private void resetBillTypeStyles() {
        if (llBillElectricity != null) {
            llBillElectricity.setBackgroundResource(R.drawable.bg_bill_type_pill);
        }
        if (llBillWater != null) {
            llBillWater.setBackgroundResource(R.drawable.bg_bill_type_pill);
        }
    }
    
    /**
     * Handle check bill action
     */
    private void handleCheckBill() {
        // Validate bill type selected
        if (selectedBillType == BILL_TYPE_NONE) {
            Toast.makeText(this, "Vui lòng chọn loại hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate customer code
        String customerCode = etCustomerCode.getText().toString().trim();
        if (TextUtils.isEmpty(customerCode)) {
            Toast.makeText(this, "Vui lòng nhập mã khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show bill details (mock data)
        showBillDetails(customerCode);
    }
    
    /**
     * Show bill details with mock data
     */
    private void showBillDetails(String customerCode) {
        // Set provider name based on bill type
        if (selectedBillType == BILL_TYPE_ELECTRICITY) {
            tvProviderName.setText("EVN Hà Nội");
        } else if (selectedBillType == BILL_TYPE_WATER) {
            tvProviderName.setText("Công ty Cấp nước Hà Nội");
        }
        
        // Set mock bill data
        tvBillingPeriod.setText("12/2024");
        tvAmountDue.setText("2.500.000 ₫");
        tvDueDate.setText("25/12/2024");
        tvBillStatus.setText("Chưa thanh toán");
        tvBillStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
        
        // Show bill details section
        llBillDetails.setVisibility(View.VISIBLE);
        
        // Show Pay Now button
        btnPayNow.setVisibility(View.VISIBLE);
        
        // Hide Check Bill button
        btnCheckBill.setVisibility(View.GONE);
        
        Toast.makeText(this, "Đã tìm thấy hóa đơn", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Handle pay now action
     */
    private void handlePayNow() {
        if (selectedBillType == BILL_TYPE_NONE) {
            Toast.makeText(this, "Vui lòng chọn loại hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String customerCode = etCustomerCode.getText().toString().trim();
        if (TextUtils.isEmpty(customerCode)) {
            Toast.makeText(this, "Vui lòng nhập mã khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog
        String billTypeName = selectedBillType == BILL_TYPE_ELECTRICITY ? "Điện" : "Nước";
        String amount = tvAmountDue.getText().toString();
        
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận thanh toán")
            .setMessage("Bạn có chắc chắn muốn thanh toán hóa đơn " + billTypeName + "?\n\n" +
                       "Mã khách hàng: " + customerCode + "\n" +
                       "Số tiền: " + amount)
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                // Navigate to success screen
                navigateToSuccessScreen(billTypeName, customerCode, amount);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Navigate to success screen with payment data
     */
    private void navigateToSuccessScreen(String billTypeName, String customerCode, String amount) {
        Intent intent = new Intent(this, BillPaymentSuccessActivity.class);
        
        // Get provider name
        String providerName = selectedBillType == BILL_TYPE_ELECTRICITY ? "EVN Hà Nội" : "Công ty Cấp nước Hà Nội";
        
        // Get account info
        List<Account> accounts = dataManager.getMockAccounts("U001");
        String accountNumber = "";
        double balanceBefore = 0;
        
        for (Account account : accounts) {
            if (account.getType() == Account.AccountType.CHECKING) {
                accountNumber = maskAccountNumber(account.getAccountNumber());
                balanceBefore = account.getBalance();
                break;
            }
        }
        
        // Pass data to success screen
        intent.putExtra("provider_name", providerName);
        intent.putExtra("bill_type", billTypeName);
        intent.putExtra("customer_code", customerCode);
        intent.putExtra("billing_period", tvBillingPeriod.getText().toString());
        intent.putExtra("amount", amount);
        intent.putExtra("account_number", accountNumber);
        intent.putExtra("balance_before", balanceBefore);
        
        startActivity(intent);
        finish();
    }
    
    /**
     * Mask account number
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return accountNumber;
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


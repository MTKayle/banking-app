package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
 * Bill Payment Activity - Light theme
 * Redesigned based on user requirements
 */
public class BillPaymentActivity extends AppCompatActivity {
    
    // Views - Header
    private ImageButton btnBack, btnHome;
    
    // Views - Account section
    private LinearLayout layoutAccountHeader, layoutAccountDetails;
    private ImageView ivAccountExpand;
    private TextView tvAccountNumberHeader, tvBalanceHeader;
    private boolean isAccountExpanded = false;
    
    // Views - Bill Type dropdown
    private LinearLayout layoutBillTypeHeader, layoutBillTypeOptions;
    private LinearLayout optionElectricity, optionWater;
    private TextView tvBillTypeSelected;
    private ImageView ivBillTypeIcon, ivBillTypeExpand;
    private boolean isBillTypeExpanded = false;
    private String selectedBillType = "electricity"; // "electricity" or "water"
    
    // Views - Payment Info
    private EditText etBillCode, etReferralCode;
    private CheckBox cbRecurringPayment;
    private Button btnContinue;
    
    private DataManager dataManager;
    private String accountNumber = "";
    private double availableBalance = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment_light);
        
        dataManager = DataManager.getInstance(this);
        
        initializeViews();
        loadAccountData();
        setupClickListeners();
    }
    
    private void initializeViews() {
        // Header
        btnBack = findViewById(R.id.btn_back);
        btnHome = findViewById(R.id.btn_home);
        
        // Account section
        layoutAccountHeader = findViewById(R.id.layout_account_header);
        layoutAccountDetails = findViewById(R.id.layout_account_details);
        ivAccountExpand = findViewById(R.id.iv_account_expand);
        tvAccountNumberHeader = findViewById(R.id.tv_account_number_header);
        tvBalanceHeader = findViewById(R.id.tv_balance_header);
        
        // Bill Type dropdown
        layoutBillTypeHeader = findViewById(R.id.layout_bill_type_header);
        layoutBillTypeOptions = findViewById(R.id.layout_bill_type_options);
        optionElectricity = findViewById(R.id.option_electricity);
        optionWater = findViewById(R.id.option_water);
        tvBillTypeSelected = findViewById(R.id.tv_bill_type_selected);
        ivBillTypeIcon = findViewById(R.id.iv_bill_type_icon);
        ivBillTypeExpand = findViewById(R.id.iv_bill_type_expand);
        
        // Payment info
        etBillCode = findViewById(R.id.et_bill_code);
        etReferralCode = findViewById(R.id.et_referral_code);
        cbRecurringPayment = findViewById(R.id.cb_recurring_payment);
        btnContinue = findViewById(R.id.btn_continue);
    }
    
    private void loadAccountData() {
        // Load checking account data
        List<Account> accounts = dataManager.getMockAccounts("U001");
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        for (Account account : accounts) {
            if (account.getType() == Account.AccountType.CHECKING) {
                accountNumber = account.getAccountNumber();
                availableBalance = account.getBalance();
                
                // Display in header
                tvAccountNumberHeader.setText("TK người: " + accountNumber);
                tvBalanceHeader.setText("Số dư: " + formatter.format(availableBalance) + " VND");
                break;
            }
        }
    }
    
    private void setupClickListeners() {
        // Header buttons
        btnBack.setOnClickListener(v -> onBackPressed());
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        // Account section collapse/expand
        layoutAccountHeader.setOnClickListener(v -> toggleAccountSection());
        
        // Bill Type dropdown toggle
        layoutBillTypeHeader.setOnClickListener(v -> toggleBillTypeDropdown());
        
        // Bill Type options
        optionElectricity.setOnClickListener(v -> selectBillType("electricity"));
        optionWater.setOnClickListener(v -> selectBillType("water"));
        
        // Continue button
        btnContinue.setOnClickListener(v -> handleContinue());
    }
    
    /**
     * Toggle bill type dropdown
     */
    private void toggleBillTypeDropdown() {
        isBillTypeExpanded = !isBillTypeExpanded;
        
        if (isBillTypeExpanded) {
            layoutBillTypeOptions.setVisibility(View.VISIBLE);
            rotateIcon(ivBillTypeExpand, 0, 180);
        } else {
            layoutBillTypeOptions.setVisibility(View.GONE);
            rotateIcon(ivBillTypeExpand, 180, 0);
        }
    }
    
    /**
     * Select bill type and update UI
     */
    private void selectBillType(String type) {
        selectedBillType = type;
        
        // Update header display
        if (type.equals("electricity")) {
            tvBillTypeSelected.setText("Tiền điện");
            ivBillTypeIcon.setImageResource(R.drawable.ic_lightbulb);
            ivBillTypeIcon.setColorFilter(0xFFFFA726); // Orange
        } else {
            tvBillTypeSelected.setText("Tiền nước");
            ivBillTypeIcon.setImageResource(R.drawable.ic_water_drop);
            ivBillTypeIcon.setColorFilter(0xFF2196F3); // Blue
        }
        
        // Collapse dropdown
        isBillTypeExpanded = false;
        layoutBillTypeOptions.setVisibility(View.GONE);
        rotateIcon(ivBillTypeExpand, 180, 0);
    }
    
    /**
     * Toggle account section expand/collapse
     */
    private void toggleAccountSection() {
        isAccountExpanded = !isAccountExpanded;
        
        if (isAccountExpanded) {
            layoutAccountDetails.setVisibility(View.VISIBLE);
            rotateIcon(ivAccountExpand, 0, 180);
        } else {
            layoutAccountDetails.setVisibility(View.GONE);
            rotateIcon(ivAccountExpand, 180, 0);
        }
    }
    
    
    /**
     * Rotate icon animation
     */
    private void rotateIcon(ImageView imageView, float fromDegrees, float toDegrees) {
        RotateAnimation rotate = new RotateAnimation(
            fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        imageView.startAnimation(rotate);
    }
    
    /**
     * Handle continue button click
     */
    private void handleContinue() {
        // Validate bill type
        if (TextUtils.isEmpty(selectedBillType)) {
            Toast.makeText(this, "Vui lòng chọn loại hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate bill code
        String billCode = etBillCode.getText().toString().trim();
        if (TextUtils.isEmpty(billCode)) {
            Toast.makeText(this, "Vui lòng nhập mã hóa đơn", Toast.LENGTH_SHORT).show();
            etBillCode.requestFocus();
            return;
        }
        
        // Get other info
        String referralCode = etReferralCode.getText().toString().trim();
        boolean isRecurring = cbRecurringPayment.isChecked();
        
        // Navigate to confirmation screen (no dialog)
        navigateToConfirmationScreen(billCode, referralCode, isRecurring);
    }
    
    /**
     * Navigate to confirmation screen
     */
    private void navigateToConfirmationScreen(String billCode, String referralCode, boolean isRecurring) {
        // Mock bill data based on type
        String providerName;
        String billTypeName;
        
        if (selectedBillType.equals("electricity")) {
            providerName = "Công ty Điện lực Duy Tân";
            billTypeName = "Tiền điện";
        } else {
            providerName = "Công ty Cấp nước Sài Gòn";
            billTypeName = "Tiền nước";
        }
        
        String billingPeriod = "12/2024";
        String amount = "2500000"; // Without formatting for easier parsing
        
        // Get current user name from DataManager
        String userName = dataManager.getUserFullName();
        if (TextUtils.isEmpty(userName)) {
            userName = "Khách hàng";
        }
        
        // Navigate to confirmation screen
        Intent intent = new Intent(this, BillPaymentConfirmationActivity.class);
        
        // Pass data
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_PROVIDER_NAME, providerName);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILL_TYPE, billTypeName);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILL_CODE, billCode);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILLING_PERIOD, billingPeriod);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_AMOUNT, amount);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_ACCOUNT_NUMBER, accountNumber);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_USER_NAME, userName);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_IS_RECURRING, isRecurring);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_REFERRAL_CODE, referralCode);
        
        startActivity(intent);
    }
    
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

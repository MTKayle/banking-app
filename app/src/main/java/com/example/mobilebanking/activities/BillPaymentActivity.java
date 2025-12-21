package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.UtilityBillApiService;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.dto.BillSearchResponse;
import com.example.mobilebanking.api.dto.BillTypesResponse;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Bill Payment Activity - Light theme
 * Redesigned based on user requirements
 */
public class BillPaymentActivity extends AppCompatActivity {
    
    private static final String TAG = "BillPaymentActivity";
    
    // Views - Header
    private ImageButton btnBack, btnHome;
    
    // Views - Account section
    private LinearLayout layoutAccountHeader, layoutAccountDetails;
    private ImageView ivAccountExpand;
    private TextView tvAccountNumberHeader, tvBalanceHeader;
    private boolean isAccountExpanded = false;
    
    // Views - Bill Type dropdown
    private LinearLayout layoutBillTypeHeader, layoutBillTypeOptions;
    private TextView tvBillTypeSelected;
    private ImageView ivBillTypeIcon, ivBillTypeExpand;
    private boolean isBillTypeExpanded = false;
    
    // Views - Payment Info
    private EditText etBillCode;
    private TextView tvBillCodeError;
    private Button btnContinue;
    
    private DataManager dataManager;
    private UtilityBillApiService utilityBillApiService;
    private AccountApiService accountApiService;
    private String accountNumber = "";
    private double availableBalance = 0;
    
    // Bill types data
    private List<BillTypesResponse.BillType> billTypes = new ArrayList<>();
    private String selectedBillTypeValue = ""; // "ELECTRICITY" or "WATER"
    private String selectedBillTypeDisplay = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment_light);
        
        dataManager = DataManager.getInstance(this);
        utilityBillApiService = ApiClient.getUtilityBillApiService();
        accountApiService = ApiClient.getAccountApiService();
        
        initializeViews();
        loadAccountData();
        loadBillTypes();
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
        tvBillTypeSelected = findViewById(R.id.tv_bill_type_selected);
        ivBillTypeIcon = findViewById(R.id.iv_bill_type_icon);
        ivBillTypeExpand = findViewById(R.id.iv_bill_type_expand);
        
        // Payment info
        etBillCode = findViewById(R.id.et_bill_code);
        tvBillCodeError = findViewById(R.id.tv_bill_code_error);
        btnContinue = findViewById(R.id.btn_continue);
    }
    
    private void loadAccountData() {
        // Get userId from DataManager
        Long userId = dataManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call API to get checking account info
        accountApiService.getCheckingAccountInfo(userId).enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse accountInfo = response.body();
                    accountNumber = accountInfo.getAccountNumber();
                    availableBalance = accountInfo.getBalance() != null ? 
                        accountInfo.getBalance().doubleValue() : 0;
                    
                    // Display in header
                    NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                    tvAccountNumberHeader.setText("TK người: " + accountNumber);
                    tvBalanceHeader.setText("Số dư: " + formatter.format(availableBalance) + " VND");
                } else {
                    Log.e(TAG, "Failed to load account info: " + response.code());
                    Toast.makeText(BillPaymentActivity.this, 
                        "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                Log.e(TAG, "Error loading account info", t);
                Toast.makeText(BillPaymentActivity.this, 
                    "Lỗi kết nối khi tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Load bill types from API
     */
    private void loadBillTypes() {
        utilityBillApiService.getBillTypes().enqueue(new Callback<BillTypesResponse>() {
            @Override
            public void onResponse(Call<BillTypesResponse> call, Response<BillTypesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BillTypesResponse billTypesResponse = response.body();
                    if (billTypesResponse.getSuccess() && billTypesResponse.getData() != null) {
                        // Filter out INTERNET
                        billTypes.clear();
                        for (BillTypesResponse.BillType type : billTypesResponse.getData()) {
                            if (!"INTERNET".equals(type.getValue())) {
                                billTypes.add(type);
                            }
                        }
                        
                        // Build dynamic UI for bill types
                        buildBillTypeOptions();
                        
                        // Set default selection to first item
                        if (!billTypes.isEmpty()) {
                            selectBillType(billTypes.get(0));
                        }
                        
                        Log.d(TAG, "Loaded " + billTypes.size() + " bill types");
                    } else {
                        Toast.makeText(BillPaymentActivity.this, 
                            "Không thể tải danh sách loại hóa đơn", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BillPaymentActivity.this, 
                        "Lỗi kết nối: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BillTypesResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load bill types", t);
                Toast.makeText(BillPaymentActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Build bill type options dynamically
     */
    private void buildBillTypeOptions() {
        layoutBillTypeOptions.removeAllViews();
        
        for (int i = 0; i < billTypes.size(); i++) {
            final BillTypesResponse.BillType billType = billTypes.get(i);
            
            // Add divider before each option (except first)
            if (i > 0) {
                View divider = new View(this);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    (int) (1 * getResources().getDisplayMetrics().density)
                );
                dividerParams.setMargins(
                    (int) (16 * getResources().getDisplayMetrics().density), 
                    0, 
                    (int) (16 * getResources().getDisplayMetrics().density), 
                    0
                );
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(0xFFE0E0E0);
                layoutBillTypeOptions.addView(divider);
            }
            
            // Create option layout
            LinearLayout optionLayout = new LinearLayout(this);
            optionLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            optionLayout.setLayoutParams(optionParams);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            optionLayout.setPadding(padding, padding, padding, padding);
            optionLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            optionLayout.setClickable(true);
            optionLayout.setFocusable(true);
            
            // Set selectable background using TypedValue
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            optionLayout.setBackgroundResource(outValue.resourceId);
            
            // Icon
            ImageView icon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                (int) (24 * getResources().getDisplayMetrics().density),
                (int) (24 * getResources().getDisplayMetrics().density)
            );
            iconParams.setMarginEnd((int) (12 * getResources().getDisplayMetrics().density));
            icon.setLayoutParams(iconParams);
            
            // Set icon based on type
            if ("ELECTRICITY".equals(billType.getValue())) {
                icon.setImageResource(R.drawable.ic_lightbulb);
                icon.setColorFilter(0xFFFFA726);
            } else if ("WATER".equals(billType.getValue())) {
                icon.setImageResource(R.drawable.ic_water_drop);
                icon.setColorFilter(0xFF2196F3);
            }
            
            // Text
            TextView text = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            );
            text.setLayoutParams(textParams);
            text.setText(billType.getDisplayName());
            text.setTextColor(0xFF333333);
            text.setTextSize(15);
            
            optionLayout.addView(icon);
            optionLayout.addView(text);
            
            // Click listener
            optionLayout.setOnClickListener(v -> selectBillType(billType));
            
            layoutBillTypeOptions.addView(optionLayout);
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
        
        // Clear error when user types
        etBillCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user starts typing
                if (tvBillCodeError.getVisibility() == View.VISIBLE) {
                    tvBillCodeError.setVisibility(View.GONE);
                    tvBillCodeError.setText("");
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
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
    private void selectBillType(BillTypesResponse.BillType billType) {
        selectedBillTypeValue = billType.getValue();
        selectedBillTypeDisplay = billType.getDisplayName();
        
        // Update header display
        tvBillTypeSelected.setText(billType.getDisplayName());
        
        // Update icon
        if ("ELECTRICITY".equals(billType.getValue())) {
            ivBillTypeIcon.setImageResource(R.drawable.ic_lightbulb);
            ivBillTypeIcon.setColorFilter(0xFFFFA726); // Orange
        } else if ("WATER".equals(billType.getValue())) {
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
        // Clear previous error
        tvBillCodeError.setVisibility(View.GONE);
        tvBillCodeError.setText("");
        
        // Validate bill type
        if (TextUtils.isEmpty(selectedBillTypeValue)) {
            Toast.makeText(this, "Vui lòng chọn loại hóa đơn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate bill code
        String billCode = etBillCode.getText().toString().trim();
        if (TextUtils.isEmpty(billCode)) {
            tvBillCodeError.setText("Vui lòng nhập mã hóa đơn");
            tvBillCodeError.setVisibility(View.VISIBLE);
            etBillCode.requestFocus();
            return;
        }
        
        // Search bill via API
        searchBill(billCode, selectedBillTypeValue);
    }
    
    /**
     * Search bill via API
     */
    private void searchBill(String billCode, String billType) {
        // Show loading
        btnContinue.setEnabled(false);
        btnContinue.setText("Đang tìm kiếm...");
        
        utilityBillApiService.searchBill(billCode, billType).enqueue(new Callback<BillSearchResponse>() {
            @Override
            public void onResponse(Call<BillSearchResponse> call, Response<BillSearchResponse> response) {
                btnContinue.setEnabled(true);
                btnContinue.setText("Tiếp tục");
                
                if (response.isSuccessful() && response.body() != null) {
                    BillSearchResponse billResponse = response.body();
                    if (billResponse.getSuccess() && billResponse.getData() != null) {
                        // Check if bill is already paid
                        BillSearchResponse.BillData billData = billResponse.getData();
                        if ("PAID".equals(billData.getStatus())) {
                            // Show error: bill already paid
                            tvBillCodeError.setText("Hóa đơn đã thanh toán");
                            tvBillCodeError.setVisibility(View.VISIBLE);
                            return;
                        }
                        
                        // Success - navigate to confirmation
                        navigateToConfirmationScreen(billData);
                    } else {
                        // API returned error - show error below input
                        String errorMsg = billResponse.getMessage();
                        if (errorMsg != null && errorMsg.contains("Không tìm thấy hóa đơn")) {
                            tvBillCodeError.setText("Mã hóa đơn không tồn tại");
                            tvBillCodeError.setVisibility(View.VISIBLE);
                        } else {
                            // Other errors - show in toast
                            if (errorMsg == null || errorMsg.isEmpty()) {
                                errorMsg = "Không tìm thấy hóa đơn";
                            }
                            Toast.makeText(BillPaymentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                } else if (response.code() == 400 && response.errorBody() != null) {
                    // Handle HTTP 400 Bad Request
                    try {
                        String errorJson = response.errorBody().string();
                        // Parse error response manually
                        if (errorJson.contains("Không tìm thấy hóa đơn")) {
                            tvBillCodeError.setText("Mã hóa đơn không tồn tại");
                            tvBillCodeError.setVisibility(View.VISIBLE);
                        } else {
                            // Try to parse as JSON and get message
                            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonParser()
                                .parse(errorJson).getAsJsonObject();
                            String message = jsonObject.has("message") ? 
                                jsonObject.get("message").getAsString() : "Lỗi không xác định";
                            
                            if (message.contains("Không tìm thấy hóa đơn")) {
                                tvBillCodeError.setText("Mã hóa đơn không tồn tại");
                                tvBillCodeError.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(BillPaymentActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        tvBillCodeError.setText("Mã hóa đơn không tồn tại");
                        tvBillCodeError.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Other HTTP errors
                    Toast.makeText(BillPaymentActivity.this, 
                        "Lỗi: " + response.code() + " - " + response.message(), 
                        Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BillSearchResponse> call, Throwable t) {
                btnContinue.setEnabled(true);
                btnContinue.setText("Tiếp tục");
                
                Log.e(TAG, "Failed to search bill", t);
                Toast.makeText(BillPaymentActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Navigate to confirmation screen with bill data
     */
    private void navigateToConfirmationScreen(BillSearchResponse.BillData billData) {
        // Get current user name from DataManager
        String userName = dataManager.getUserFullName();
        if (TextUtils.isEmpty(userName)) {
            userName = "Khách hàng";
        }
        
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        // Navigate to confirmation screen
        Intent intent = new Intent(this, BillPaymentConfirmationActivity.class);
        
        // Pass basic data
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_PROVIDER_NAME, billData.getProviderName());
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILL_TYPE, billData.getBillTypeDisplay());
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILL_CODE, billData.getBillCode());
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILLING_PERIOD, billData.getPeriod());
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_AMOUNT, 
            billData.getTotalAmount() != null ? billData.getTotalAmount().toString() : "0");
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_ACCOUNT_NUMBER, accountNumber);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_USER_NAME, userName);
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_DUE_DATE, billData.getDueDate());
        intent.putExtra(BillPaymentConfirmationActivity.EXTRA_BILL_ID, billData.getBillId());
        intent.putExtra("EXTRA_BALANCE", String.valueOf(availableBalance));
        
        // Pass customer info
        intent.putExtra("EXTRA_CUSTOMER_NAME", billData.getCustomerName());
        intent.putExtra("EXTRA_CUSTOMER_ADDRESS", billData.getCustomerAddress());
        intent.putExtra("EXTRA_CUSTOMER_PHONE", billData.getCustomerPhone());
        
        // Pass usage and pricing info
        intent.putExtra("EXTRA_USAGE_AMOUNT", billData.getUsageAmount());
        intent.putExtra("EXTRA_UNIT_PRICE", 
            billData.getUnitPrice() != null ? billData.getUnitPrice().toString() : "0");
        intent.putExtra("EXTRA_AMOUNT_BEFORE_VAT", 
            billData.getAmount() != null ? billData.getAmount().toString() : "0");
        intent.putExtra("EXTRA_VAT", 
            billData.getVat() != null ? billData.getVat().toString() : "0");
        
        // Pass status and notes
        intent.putExtra("EXTRA_STATUS_DISPLAY", billData.getStatusDisplay());
        intent.putExtra("EXTRA_NOTES", billData.getNotes());
        intent.putExtra("EXTRA_BILL_TYPE_VALUE", billData.getBillType());

        startActivity(intent);
    }
    
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

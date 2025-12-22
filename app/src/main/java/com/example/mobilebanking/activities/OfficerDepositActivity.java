package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.PaymentApiService;
import com.example.mobilebanking.api.UserApiService;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.api.dto.DepositRequest;
import com.example.mobilebanking.api.dto.DepositResponse;
import com.example.mobilebanking.api.dto.UserResponse;
import com.example.mobilebanking.api.dto.WithdrawRequest;
import com.example.mobilebanking.api.dto.WithdrawResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficerDepositActivity extends BaseActivity {
    
    private static final String TAG = "OfficerDepositActivity";
    
    private MaterialToolbar toolbar;
    private EditText etPhone, etAmount, etNote;
    private Button btnSearch, btnSubmit;
    private RadioGroup rgTransactionType;
    private RadioButton rbDeposit, rbWithdraw;
    private ProgressBar progressBar, progressSearch;
    private CardView cardCustomerInfo;
    private LinearLayout layoutTransactionInput;
    private TextView tvCustomerName, tvAccountNumber, tvCustomerPhone;
    
    private PaymentApiService paymentApiService;
    private UserApiService userApiService;
    private AccountApiService accountApiService;
    
    // Customer data
    private Long currentUserId;
    private String currentAccountNumber;
    private String currentCustomerName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_deposit);
        
        paymentApiService = ApiClient.getPaymentApiService();
        userApiService = ApiClient.getUserApiService();
        accountApiService = ApiClient.getAccountApiService();
        
        initViews();
        setupToolbar();
        setupListeners();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etPhone = findViewById(R.id.et_phone);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        btnSearch = findViewById(R.id.btn_search);
        btnSubmit = findViewById(R.id.btn_submit);
        rgTransactionType = findViewById(R.id.rg_transaction_type);
        rbDeposit = findViewById(R.id.rb_deposit);
        rbWithdraw = findViewById(R.id.rb_withdraw);
        progressBar = findViewById(R.id.progress_bar);
        progressSearch = findViewById(R.id.progress_search);
        cardCustomerInfo = findViewById(R.id.card_customer_info);
        layoutTransactionInput = findViewById(R.id.layout_transaction_input);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        
        // Set default to deposit
        if (rbDeposit != null) {
            rbDeposit.setChecked(true);
        }
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchCustomer());
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }
    
    private void searchCustomer() {
        String phone = etPhone.getText().toString().trim();
        
        if (phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!phone.matches("^0[0-9]{9}$")) {
            Toast.makeText(this, "Số điện thoại không hợp lệ (10 chữ số, bắt đầu bằng 0)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        progressSearch.setVisibility(View.VISIBLE);
        btnSearch.setEnabled(false);
        cardCustomerInfo.setVisibility(View.GONE);
        layoutTransactionInput.setVisibility(View.GONE);
        
        // Reset current data
        currentUserId = null;
        currentAccountNumber = null;
        currentCustomerName = null;
        
        // Step 1: Get user by phone - use raw response to handle date parsing issues
        Log.d(TAG, "Searching for phone: " + phone);
        Call<ResponseBody> call = userApiService.getUserByPhoneRaw(phone);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "getUserByPhone response code: " + response.code());
                Log.d(TAG, "getUserByPhone isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        Log.d(TAG, "Raw JSON response: " + jsonString);
                        
                        // Kiểm tra JSON string có rỗng không
                        if (jsonString == null || jsonString.trim().isEmpty()) {
                            progressSearch.setVisibility(View.GONE);
                            btnSearch.setEnabled(true);
                            Log.e(TAG, "JSON response is empty");
                            Toast.makeText(OfficerDepositActivity.this, 
                                "Không tìm thấy khách hàng với số điện thoại này", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Parse JSON manually
                        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
                        
                        // Kiểm tra xem có phải là error response không (có field "status" và "message")
                        if (jsonObject.has("status") && jsonObject.has("message")) {
                            int status = jsonObject.get("status").getAsInt();
                            String message = jsonObject.get("message").getAsString();
                            Log.e(TAG, "Error response - status: " + status + ", message: " + message);
                            progressSearch.setVisibility(View.GONE);
                            btnSearch.setEnabled(true);
                            Toast.makeText(OfficerDepositActivity.this, message, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        Long userId = jsonObject.has("userId") && !jsonObject.get("userId").isJsonNull() 
                            ? jsonObject.get("userId").getAsLong() : null;
                        String fullName = jsonObject.has("fullName") && !jsonObject.get("fullName").isJsonNull() 
                            ? jsonObject.get("fullName").getAsString() : null;
                        String userPhone = jsonObject.has("phone") && !jsonObject.get("phone").isJsonNull() 
                            ? jsonObject.get("phone").getAsString() : phone;
                        
                        Log.d(TAG, "Parsed - userId: " + userId + ", fullName: " + fullName + ", phone: " + userPhone);
                        
                        if (userId != null) {
                            currentUserId = userId;
                            currentCustomerName = fullName;
                            
                            // Create a simple UserResponse for display
                            UserResponse user = new UserResponse();
                            user.setUserId(userId);
                            user.setFullName(fullName);
                            user.setPhone(userPhone);
                            
                            // Step 2: Get checking account info to get accountNumber
                            fetchCheckingAccount(user);
                        } else {
                            progressSearch.setVisibility(View.GONE);
                            btnSearch.setEnabled(true);
                            Log.e(TAG, "userId is null in response");
                            Toast.makeText(OfficerDepositActivity.this, 
                                "Không tìm thấy khách hàng với số điện thoại này", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        progressSearch.setVisibility(View.GONE);
                        btnSearch.setEnabled(true);
                        Log.e(TAG, "Error parsing JSON response", e);
                        Toast.makeText(OfficerDepositActivity.this, 
                            "Lỗi xử lý dữ liệu từ server: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressSearch.setVisibility(View.GONE);
                    btnSearch.setEnabled(true);
                    Log.e(TAG, "Search error: " + response.code());
                    String errorMsg = "Không tìm thấy khách hàng";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            // Parse error message if available
                            if (errorBody.contains("message")) {
                                try {
                                    JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();
                                    if (errorJson.has("message")) {
                                        errorMsg = errorJson.get("message").getAsString();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing error body", e);
                                    // Nếu không parse được, dùng errorBody trực tiếp nếu nó ngắn
                                    if (errorBody.length() < 200) {
                                        errorMsg = errorBody;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerDepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressSearch.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                Log.e(TAG, "Search failed: " + t.getMessage(), t);
                Toast.makeText(OfficerDepositActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Step 2: Fetch checking account to get accountNumber
     * API: GET /accounts/{userId}/checking
     */
    private void fetchCheckingAccount(UserResponse user) {
        Log.d(TAG, "Fetching checking account for userId: " + user.getUserId());
        Call<CheckingAccountInfoResponse> call = accountApiService.getCheckingAccountInfo(user.getUserId());
        call.enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                progressSearch.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                
                Log.d(TAG, "getCheckingAccountInfo response code: " + response.code());
                Log.d(TAG, "getCheckingAccountInfo isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse checkingAccount = response.body();
                    String accountNumber = checkingAccount.getAccountNumber();
                    
                    Log.d(TAG, "Checking account - accountNumber: " + accountNumber + 
                        ", checkingId: " + checkingAccount.getCheckingId());
                    
                    // Kiểm tra accountNumber có hợp lệ không
                    if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                        currentAccountNumber = accountNumber;
                        // Display customer info
                        displayCustomerInfo(user, accountNumber);
                    } else {
                        Log.e(TAG, "AccountNumber is null or empty in response");
                        Toast.makeText(OfficerDepositActivity.this, 
                            "Không thể lấy thông tin tài khoản: Không tìm thấy số tài khoản", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Get checking account error: " + response.code());
                    String errorMsg = "Không thể lấy thông tin tài khoản";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            // Parse error message if available
                            if (errorBody.contains("message")) {
                                try {
                                    JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();
                                    if (errorJson.has("message")) {
                                        errorMsg = errorJson.get("message").getAsString();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing error body", e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerDepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                progressSearch.setVisibility(View.GONE);
                btnSearch.setEnabled(true);
                Log.e(TAG, "Get checking account failed: " + t.getMessage(), t);
                Toast.makeText(OfficerDepositActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayCustomerInfo(UserResponse user, String accountNumber) {
        tvCustomerName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        tvAccountNumber.setText(accountNumber != null ? accountNumber : "N/A");
        tvCustomerPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        
        cardCustomerInfo.setVisibility(View.VISIBLE);
        layoutTransactionInput.setVisibility(View.VISIBLE);
        
        // Clear previous input
        etAmount.setText("");
        etNote.setText("");
    }
    
    private void handleSubmit() {
        if (currentUserId == null || currentAccountNumber == null) {
            Toast.makeText(this, "Vui lòng tìm kiếm khách hàng trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if deposit or withdraw
        boolean isDeposit = rbDeposit == null || rbDeposit.isChecked();
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount);
        String action = isDeposit ? "Nạp" : "Rút";
        
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận " + action.toLowerCase() + " tiền")
            .setMessage("Khách hàng: " + currentCustomerName + "\n" +
                "Số tài khoản: " + currentAccountNumber + "\n\n" +
                action + " " + formattedAmount + " đ " + 
                (isDeposit ? "vào" : "từ") + " tài khoản?")
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                if (isDeposit) {
                    performDeposit(amount, note);
                } else {
                    performWithdraw(amount, note);
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Perform deposit via API
     * Endpoint: POST /payment/checking/deposit
     * Input: {"accountNumber": "xxx", "amount": xxx, "description": "xxx"}
     */
    private void performDeposit(double amount, String note) {
        showLoading(true);
        
        String description = note.isEmpty() ? "Nạp tiền từ quầy giao dịch" : note;
        DepositRequest request = new DepositRequest(currentAccountNumber, BigDecimal.valueOf(amount), description);
        
        Log.d(TAG, "Deposit request - accountNumber: " + currentAccountNumber + ", amount: " + amount);
        
        Call<DepositResponse> call = paymentApiService.depositToChecking(request);
        call.enqueue(new Callback<DepositResponse>() {
            @Override
            public void onResponse(Call<DepositResponse> call, Response<DepositResponse> response) {
                showLoading(false);
                
                Log.d(TAG, "Deposit response code: " + response.code());
                Log.d(TAG, "Deposit isSuccessful: " + response.isSuccessful());
                Log.d(TAG, "Deposit body is null: " + (response.body() == null));
                
                if (response.isSuccessful()) {
                    DepositResponse depositResponse = response.body();
                    
                    // Handle case where response is successful but body parsing failed
                    if (depositResponse == null) {
                        Log.w(TAG, "Response body is null but status is 200 - treating as success");
                        // Show success without balance info
                        new AlertDialog.Builder(OfficerDepositActivity.this)
                            .setTitle("Nạp tiền thành công!")
                            .setMessage("Khách hàng: " + currentCustomerName + "\n" +
                                "Số tài khoản: " + currentAccountNumber + "\n" +
                                "Số tiền nạp: " + NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " đ")
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                        return;
                    }
                    
                    NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    String newBalance = depositResponse.getNewBalance() != null 
                        ? formatter.format(depositResponse.getNewBalance()) 
                        : "N/A";
                    
                    new AlertDialog.Builder(OfficerDepositActivity.this)
                        .setTitle("Nạp tiền thành công!")
                        .setMessage("Khách hàng: " + currentCustomerName + "\n" +
                            "Số tài khoản: " + currentAccountNumber + "\n" +
                            "Số dư mới: " + newBalance + " đ")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .show();
                } else {
                    Log.e(TAG, "Deposit error: " + response.code());
                    String errorMsg = "Lỗi nạp tiền";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                            errorMsg = errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerDepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<DepositResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Deposit failed: " + t.getMessage(), t);
                Toast.makeText(OfficerDepositActivity.this, 
                    "Không thể kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Perform withdraw via API
     * Endpoint: POST /payment/checking/withdraw
     * Input: {"userId": xxx, "amount": xxx, "description": "xxx"}
     */
    private void performWithdraw(double amount, String note) {
        showLoading(true);
        
        String description = note.isEmpty() ? "Rút tiền mặt tại quầy" : note;
        WithdrawRequest request = new WithdrawRequest(currentUserId, amount, description);
        
        Call<WithdrawResponse> call = paymentApiService.withdrawFromChecking(request);
        call.enqueue(new Callback<WithdrawResponse>() {
            @Override
            public void onResponse(Call<WithdrawResponse> call, Response<WithdrawResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    WithdrawResponse withdrawResponse = response.body();
                    
                    NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    String newBalance = formatter.format(withdrawResponse.getNewBalance());
                    
                    new AlertDialog.Builder(OfficerDepositActivity.this)
                        .setTitle("Rút tiền thành công!")
                        .setMessage("Khách hàng: " + currentCustomerName + "\n" +
                            "Số tài khoản: " + currentAccountNumber + "\n" +
                            "Số dư mới: " + newBalance + " đ")
                        .setPositiveButton("OK", (dialog, which) -> finish())
                        .show();
                } else {
                    Log.e(TAG, "Withdraw error: " + response.code());
                    String errorMsg = "Lỗi rút tiền";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerDepositActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<WithdrawResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Withdraw failed: " + t.getMessage(), t);
                Toast.makeText(OfficerDepositActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnSubmit.setEnabled(!show);
    }
}

package com.example.mobilebanking.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.TransferApiService;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.api.dto.ExternalTransferInitiateResponse;
import com.example.mobilebanking.api.dto.ExternalTransferRequest;
import com.example.mobilebanking.api.dto.InternalTransferRequest;
import com.example.mobilebanking.api.dto.TransferInitiateResponse;
import com.example.mobilebanking.utils.DataManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Transaction Confirmation Activity - Confirms transaction details before processing
 */
public class TransactionConfirmationActivity extends AppCompatActivity {
    private static final String TAG = "TransactionConfirm";
    private static final int REQUEST_FACE_VERIFICATION = 1001;
    private static final double MIN_AMOUNT_FOR_FACE_VERIFICATION = 10000000; // 10 million VND
    
    private TextView tvAmount, tvAmountInWords, tvFromName, tvFromAccount, tvFromBank;
    private TextView tvToName, tvToAccount, tvToBank, tvNote, tvFee, tvTransferType;
    private Button btnConfirm, btnCancel;
    private ImageView ivBack;
    private android.widget.ProgressBar progressBar;
    private DataManager dataManager;
    
    private String senderAccountNumber;
    private String transactionCode; // Store transaction code from initiate API

    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation);

        dataManager = DataManager.getInstance(this);

        // Register broadcast receiver to finish this activity
        IntentFilter filter = new IntentFilter("com.example.mobilebanking.FINISH_TRANSACTION_FLOW");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(finishReceiver, filter);
        }

        initializeViews();
        loadTransactionDetails();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(finishReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.iv_back);
        tvAmount = findViewById(R.id.tv_amount);
        tvAmountInWords = findViewById(R.id.tv_amount_in_words);
        tvFromName = findViewById(R.id.tv_from_name);
        tvFromAccount = findViewById(R.id.tv_from_account);
        tvFromBank = findViewById(R.id.tv_from_bank);
        tvToName = findViewById(R.id.tv_to_name);
        tvToAccount = findViewById(R.id.tv_to_account);
        tvToBank = findViewById(R.id.tv_to_bank);
        tvNote = findViewById(R.id.tv_note);
        tvFee = findViewById(R.id.tv_fee);
        tvTransferType = findViewById(R.id.tv_transfer_type);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progress_bar);
        
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.GONE);
        }
    }

    private void loadTransactionDetails() {
        Intent intent = getIntent();
        String toAccount = intent.getStringExtra("to_account");
        String toName = intent.getStringExtra("to_name");
        double amount = intent.getDoubleExtra("amount", 0);
        String note = intent.getStringExtra("note");
        String bank = intent.getStringExtra("bank");

        // Format amount
        String formattedAmount = formatWithDots(String.valueOf((long)amount)) + " VNĐ";
        tvAmount.setText(formattedAmount);

        // Convert amount to words
        String amountInWords = numberToVietnameseWords((long)amount) + " đồng";
        tvAmountInWords.setText(amountInWords);

        // Load sender account info from API
        loadSenderAccountInfo();

        // To account info
        if (toName != null && !toName.isEmpty()) {
            tvToName.setText(toName);
        } else {
            tvToName.setText("NGƯỜI NHẬN");
        }
        tvToAccount.setText(toAccount);

        // Set bank name from bank code
        String bankFullName = getBankFullName(bank);
        tvToBank.setText(bankFullName);

        // Note
        if (note != null && !note.isEmpty()) {
            tvNote.setText(note);
        } else {
            tvNote.setText("Không có nội dung");
        }

        // Fee - Free for all transfers
        tvFee.setText("Miễn phí");

        // Transfer type
        tvTransferType.setText("Chuyển nhanh");
    }

    /**
     * Load sender account info from API
     */
    private void loadSenderAccountInfo() {
        Long userId = dataManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountApiService accountApiService = ApiClient.getAccountApiService();
        Call<CheckingAccountInfoResponse> call = accountApiService.getCheckingAccountInfo(userId);

        call.enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse accountInfo = response.body();
                    
                    // Get user full name
                    String fullName = dataManager.getUserFullName();
                    if (fullName == null || fullName.isEmpty()) {
                        fullName = "NGƯỜI DÙNG";
                    }
                    tvFromName.setText(fullName.toUpperCase());
                    
                    // Set account number
                    String accountNumber = accountInfo.getAccountNumber();
                    if (accountNumber != null) {
                        tvFromAccount.setText(accountNumber);
                        senderAccountNumber = accountNumber; // Store for transfer API
                    }
                    
                    // Set bank name (HAT Bank)
                    tvFromBank.setText("Ngân hàng công nghệ HAT");
                    
                    Log.d(TAG, "Loaded sender account: " + accountNumber);
                } else {
                    Log.e(TAG, "Failed to load sender account: " + response.code());
                    Toast.makeText(TransactionConfirmationActivity.this, 
                        "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                Log.e(TAG, "Error loading sender account", t);
                Toast.makeText(TransactionConfirmationActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getBankFullName(String bankCode) {
        if (bankCode == null) return "Ngân hàng";
        
        // Map bank codes to full names
        switch (bankCode) {
            case "HATBANK":
                return "Ngân hàng công nghệ HAT";
            case "AGRIBANK":
                return "Ngân hàng Nông nghiệp và Phát triển Nông thôn Việt Nam";
            case "VIETCOMBANK":
                return "Ngân hàng TMCP Ngoại thương Việt Nam";
            case "BIDV":
                return "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam";
            case "TECHCOMBANK":
                return "Ngân hàng TMCP Kỹ thương Việt Nam";
            case "VIETINBANK":
                return "Ngân hàng TMCP Công thương Việt Nam";
            case "ACB":
                return "Ngân hàng TMCP Á Châu";
            case "MB":
                return "Ngân hàng TMCP Quân đội";
            case "VPB":
                return "Ngân hàng TMCP Việt Nam Thịnh Vượng";
            case "TPB":
                return "Ngân hàng TMCP Tiên Phong";
            case "SACOMBANK":
                return "Ngân hàng TMCP Sài Gòn Thương Tín";
            default:
                return bankCode;
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnCancel.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> {
            // Initiate transfer first
            initiateTransfer();
        });
    }
    
    /**
     * Initiate transfer by calling appropriate API
     */
    private void initiateTransfer() {
        Intent originalIntent = getIntent();
        String toAccount = originalIntent.getStringExtra("to_account");
        String toName = originalIntent.getStringExtra("to_name");
        double amount = originalIntent.getDoubleExtra("amount", 0);
        String note = originalIntent.getStringExtra("note");
        String bankCode = originalIntent.getStringExtra("bank");
        
        if (senderAccountNumber == null || senderAccountNumber.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy số tài khoản người gửi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(android.view.View.VISIBLE);
        }
        btnConfirm.setEnabled(false);
        
        TransferApiService transferApiService = ApiClient.getTransferApiService();
        
        // Check if internal or external transfer
        if ("HATBANK".equals(bankCode)) {
            // Internal transfer (HAT to HAT)
            InternalTransferRequest request = new InternalTransferRequest(
                    senderAccountNumber,
                    toAccount,
                    amount,
                    note
            );
            
            Call<TransferInitiateResponse> call = transferApiService.initiateInternalTransfer(request);
            call.enqueue(new Callback<TransferInitiateResponse>() {
                @Override
                public void onResponse(Call<TransferInitiateResponse> call, Response<TransferInitiateResponse> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnConfirm.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        TransferInitiateResponse initiateResponse = response.body();
                        transactionCode = initiateResponse.getTransactionCode();
                        
                        Log.d(TAG, "Internal transfer initiated: " + transactionCode);
                        Toast.makeText(TransactionConfirmationActivity.this, 
                                initiateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        
                        // Proceed to face verification or OTP
                        proceedToVerification();
                    } else {
                        Log.e(TAG, "Failed to initiate internal transfer: " + response.code());
                        Toast.makeText(TransactionConfirmationActivity.this, 
                                "Không thể khởi tạo giao dịch", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<TransferInitiateResponse> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnConfirm.setEnabled(true);
                    
                    Log.e(TAG, "Error initiating internal transfer", t);
                    Toast.makeText(TransactionConfirmationActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // External transfer (HAT to Other Bank)
            // Get bankBin from intent
            String bankBin = originalIntent.getStringExtra("bank_bin");
            
            if (bankBin == null || bankBin.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy thông tin ngân hàng", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(true);
                return;
            }
            
            ExternalTransferRequest request = new ExternalTransferRequest(
                    senderAccountNumber,
                    bankBin,
                    toAccount,
                    toName,
                    amount,
                    note
            );
            
            Call<ExternalTransferInitiateResponse> call = transferApiService.initiateExternalTransfer(request);
            call.enqueue(new Callback<ExternalTransferInitiateResponse>() {
                @Override
                public void onResponse(Call<ExternalTransferInitiateResponse> call, Response<ExternalTransferInitiateResponse> response) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnConfirm.setEnabled(true);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ExternalTransferInitiateResponse initiateResponse = response.body();
                        
                        if (initiateResponse.isSuccess() && initiateResponse.getData() != null) {
                            transactionCode = initiateResponse.getData().getTransactionCode();
                            
                            Log.d(TAG, "External transfer initiated: " + transactionCode);
                            Toast.makeText(TransactionConfirmationActivity.this, 
                                    initiateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            
                            // Proceed to face verification or OTP
                            proceedToVerification();
                        } else {
                            Toast.makeText(TransactionConfirmationActivity.this, 
                                    initiateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to initiate external transfer: " + response.code());
                        Toast.makeText(TransactionConfirmationActivity.this, 
                                "Không thể khởi tạo giao dịch", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ExternalTransferInitiateResponse> call, Throwable t) {
                    if (progressBar != null) {
                        progressBar.setVisibility(android.view.View.GONE);
                    }
                    btnConfirm.setEnabled(true);
                    
                    Log.e(TAG, "Error initiating external transfer", t);
                    Toast.makeText(TransactionConfirmationActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Proceed to face verification or OTP after successful initiate
     */
    private void proceedToVerification() {
        Intent originalIntent = getIntent();
        double amount = originalIntent.getDoubleExtra("amount", 0);
        
        // Check if face verification is required (>= 10 million)
        if (amount >= MIN_AMOUNT_FOR_FACE_VERIFICATION) {
            // Navigate to face verification first
            Intent faceIntent = new Intent(TransactionConfirmationActivity.this, 
                    FaceVerificationTransactionActivity.class);
            
            // Pass all transaction data to face verification
            faceIntent.putExtra("from", "transaction"); // Important: set the flow type
            faceIntent.putExtra("transaction_code", transactionCode);
            faceIntent.putExtra("amount", amount);
            faceIntent.putExtra("to_account", originalIntent.getStringExtra("to_account"));
            faceIntent.putExtra("to_name", originalIntent.getStringExtra("to_name"));
            faceIntent.putExtra("note", originalIntent.getStringExtra("note"));
            faceIntent.putExtra("bank", originalIntent.getStringExtra("bank"));
            
            // Get phone from DataManager
            String userPhone = dataManager.getUserPhone();
            if (userPhone == null || userPhone.isEmpty()) {
                userPhone = dataManager.getLastUsername();
            }
            faceIntent.putExtra("userPhone", userPhone);
            
            Log.d(TAG, "Passing to face verification - Transaction Code: " + transactionCode);
            Log.d(TAG, "Passing to face verification - Bank: " + originalIntent.getStringExtra("bank"));
            
            startActivity(faceIntent);
            finish(); // Finish this activity so user can't go back
        } else {
            // Proceed directly to OTP
            proceedToOTP();
        }
    }
    
    /**
     * Proceed to OTP verification
     */
    private void proceedToOTP() {
        // Get phone from DataManager
        DataManager dataManager = DataManager.getInstance(this);
        String userPhone = dataManager.getUserPhone();
        if (userPhone == null || userPhone.isEmpty()) {
            userPhone = dataManager.getLastUsername();
        }
        
        Log.d(TAG, "Transfer - Phone for OTP: " + userPhone);
        
        Intent intent = new Intent(TransactionConfirmationActivity.this, OtpVerificationActivity.class);
        intent.putExtra("phone", userPhone);
        intent.putExtra("from", "transaction");

        // Pass all transaction data to OTP activity
        Intent originalIntent = getIntent();
        intent.putExtra("amount", originalIntent.getDoubleExtra("amount", 0));
        intent.putExtra("to_account", originalIntent.getStringExtra("to_account"));
        intent.putExtra("to_name", originalIntent.getStringExtra("to_name"));
        intent.putExtra("note", originalIntent.getStringExtra("note"));
        intent.putExtra("bank", originalIntent.getStringExtra("bank"));
        intent.putExtra("transaction_code", transactionCode); // Pass transaction code

        startActivity(intent);
    }
    
    // Helper: insert dots as thousand separators
    private String formatWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "0";
        if (!digits.equals("0")) {
            digits = digits.replaceFirst("^0+", "");
            if (digits.isEmpty()) digits = "0";
        }

        StringBuilder sb = new StringBuilder(digits).reverse();
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < sb.length(); i++) {
            if (i > 0 && i % 3 == 0) grouped.append('.');
            grouped.append(sb.charAt(i));
        }
        return grouped.reverse().toString();
    }

    // Convert number to Vietnamese words
    private String numberToVietnameseWords(long num) {
        if (num == 0) return "Không";
        String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
        String[] scales = {""," nghìn"," triệu"," tỷ"," nghìn tỷ"," triệu tỷ"};

        List<Integer> groups = new ArrayList<>();
        while (num > 0) {
            groups.add((int)(num % 1000));
            num /= 1000;
        }

        StringBuilder sb = new StringBuilder();
        boolean hadHigherNonZero = false;
        for (int i = groups.size()-1; i >= 0; i--) {
            int g = groups.get(i);
            if (g == 0) continue;
            String s = readThreeDigits(g, digits, hadHigherNonZero);
            if (sb.length() > 0) sb.append(" ");
            sb.append(s).append(scales[i]);
            hadHigherNonZero = true;
        }

        if (sb.length() == 0) return "Không";
        String res = sb.toString().trim();
        return Character.toUpperCase(res.charAt(0)) + res.substring(1);
    }

    private String readThreeDigits(int num, String[] digits, boolean hadHigherNonZero) {
        int hundreds = num / 100;
        int tens = (num % 100) / 10;
        int units = num % 10;
        StringBuilder sb = new StringBuilder();
        if (hundreds > 0) {
            sb.append(digits[hundreds]).append(" trăm");
        }
        if (tens == 0) {
            if (units != 0) {
                if (hundreds > 0) {
                    sb.append(" lẻ");
                } else if (hadHigherNonZero) {
                    sb.append(" lẻ");
                }
                sb.append(" ").append(readUnit(units, tens));
            }
        } else if (tens == 1) {
            sb.append(" mười");
            if (units != 0) sb.append(" ").append(readUnit(units, tens));
        } else {
            sb.append(" ").append(digits[tens]).append(" mươi");
            if (units != 0) sb.append(" ").append(readUnit(units, tens));
        }
        return sb.toString().trim();
    }

    private String readUnit(int unit, int tens) {
        switch (unit) {
            case 1:
                return (tens >= 1) ? "mốt" : "một";
            case 5:
                return (tens >= 1) ? "lăm" : "năm";
            default:
                String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
                return digits[unit];
        }
    }
}

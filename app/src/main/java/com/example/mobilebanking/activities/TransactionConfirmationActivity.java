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
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
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
    private DataManager dataManager;

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
            // Get transaction amount
            Intent originalIntent = getIntent();
            double amount = originalIntent.getDoubleExtra("amount", 0);
            
            // Check if face verification is required (>= 10 million)
            if (amount >= MIN_AMOUNT_FOR_FACE_VERIFICATION) {
                // Navigate to face verification first
                Intent faceIntent = new Intent(TransactionConfirmationActivity.this, 
                        FaceVerificationTransactionActivity.class);
                startActivityForResult(faceIntent, REQUEST_FACE_VERIFICATION);
            } else {
                // Proceed directly to OTP
                proceedToOTP();
            }
        });
    }
    
    /**
     * Proceed to OTP verification
     */
    private void proceedToOTP() {
        Intent intent = new Intent(TransactionConfirmationActivity.this, OtpVerificationActivity.class);
        intent.putExtra("phone", "0901234567");
        intent.putExtra("from", "transaction");

        // Pass all transaction data to OTP activity
        Intent originalIntent = getIntent();
        intent.putExtra("amount", originalIntent.getDoubleExtra("amount", 0));
        intent.putExtra("to_account", originalIntent.getStringExtra("to_account"));
        intent.putExtra("to_name", originalIntent.getStringExtra("to_name"));
        intent.putExtra("note", originalIntent.getStringExtra("note"));
        intent.putExtra("bank", originalIntent.getStringExtra("bank"));

        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_FACE_VERIFICATION) {
            if (resultCode == RESULT_OK) {
                // Face verification successful, proceed to OTP
                proceedToOTP();
            } else {
                // Face verification failed or cancelled
                Toast.makeText(this, "Xác thực khuôn mặt thất bại", Toast.LENGTH_SHORT).show();
            }
        }
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

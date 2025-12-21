package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavingDepositActivity extends AppCompatActivity {

    private TextView tvSourceAccount, tvBalance, tvTerm, tvMaturityDate, tvAmountInWords;
    private EditText etAmount;
    private LinearLayout layoutValidation;
    private TextView tvValidation;
    private Button btnBack, btnContinue;

    private String termType;
    private int termMonths;
    private double interestRate;
    private String sourceAccountNumber;
    private double accountBalance;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_deposit);

        dataManager = DataManager.getInstance(this);

        // Get data from intent
        termType = getIntent().getStringExtra("termType");
        termMonths = getIntent().getIntExtra("termMonths", 1);
        interestRate = getIntent().getDoubleExtra("interestRate", 0.0);

        initViews();
        setupToolbar();
        setupListeners();
        loadAccountInfo();
        updateTermInfo();
    }

    private void initViews() {
        tvSourceAccount = findViewById(R.id.tv_source_account);
        tvBalance = findViewById(R.id.tv_balance);
        tvTerm = findViewById(R.id.tv_term);
        tvMaturityDate = findViewById(R.id.tv_maturity_date);
        etAmount = findViewById(R.id.et_amount);
        tvAmountInWords = findViewById(R.id.tv_amount_in_words);
        layoutValidation = findViewById(R.id.layout_validation);
        tvValidation = findViewById(R.id.tv_validation);
        btnBack = findViewById(R.id.btn_back);
        btnContinue = findViewById(R.id.btn_continue);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnContinue.setOnClickListener(v -> {
            if (validateInput()) {
                navigateToConfirm();
            }
        });

        // Setup TextWatcher for amount formatting
        final boolean[] isEditing = {false};
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear validation error when user types
                if (s != null && s.length() > 0) {
                    layoutValidation.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing[0]) return;
                isEditing[0] = true;

                String raw = s == null ? "" : s.toString();
                // Keep only digits
                raw = raw.replaceAll("[^0-9]", "");

                if (raw.isEmpty()) {
                    etAmount.setText("0");
                    etAmount.setSelection(1);
                    tvAmountInWords.setVisibility(View.GONE);
                    isEditing[0] = false;
                    return;
                }

                // Remove leading zeros
                if (raw.length() > 1 && raw.startsWith("0")) {
                    raw = raw.replaceFirst("^0+", "");
                    if (raw.isEmpty()) raw = "0";
                }

                String formatted = formatWithDots(raw);
                etAmount.setText(formatted);
                etAmount.setSelection(formatted.length());

                // Show amount in words
                showAmountInWordsIfNeeded();

                isEditing[0] = false;
            }
        });
    }

    private void loadAccountInfo() {
        Long userId = dataManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountApiService apiService = ApiClient.getAccountApiService();
        apiService.getCheckingAccountInfo(userId).enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse account = response.body();
                    sourceAccountNumber = account.getAccountNumber();
                    accountBalance = account.getBalance().doubleValue();
                    
                    tvSourceAccount.setText(sourceAccountNumber);
                    tvBalance.setText(formatCurrency(accountBalance) + " VND");
                }
            }

            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                Toast.makeText(SavingDepositActivity.this, 
                        "Lỗi tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTermInfo() {
        tvTerm.setText(termMonths + " Tháng");
        
        // Calculate maturity date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, termMonths);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvMaturityDate.setText(sdf.format(cal.getTime()));
    }

    private boolean validateInput() {
        // Kiểm tra tài khoản nguồn đã load xong chưa
        if (TextUtils.isEmpty(sourceAccountNumber)) {
            showValidationError("Đang tải thông tin tài khoản, vui lòng đợi...");
            return false;
        }

        String amountStr = etAmount.getText().toString().trim();
        
        if (TextUtils.isEmpty(amountStr) || amountStr.equals("0")) {
            showValidationError("Quý khách vui lòng nhập số tiền gửi");
            return false;
        }

        try {
            // Remove dots for parsing
            String cleaned = amountStr.replace(".", "");
            double amount = Double.parseDouble(cleaned);
            
            if (amount < 1000000) {
                showValidationError("Số tiền gửi tối thiểu là 1.000.000 VNĐ");
                return false;
            }

            if (amount > accountBalance) {
                showValidationError("Số dư tài khoản không đủ");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            showValidationError("Số tiền không hợp lệ");
            return false;
        }
    }

    private void showValidationError(String message) {
        layoutValidation.setVisibility(View.VISIBLE);
        tvValidation.setText(message);
    }

    private void navigateToConfirm() {
        // Remove dots for parsing
        String amountStr = etAmount.getText().toString().trim().replace(".", "");
        double amount = Double.parseDouble(amountStr);

        Intent intent = new Intent(this, SavingConfirmActivity.class);
        intent.putExtra("termType", termType);
        intent.putExtra("termMonths", termMonths);
        intent.putExtra("interestRate", interestRate);
        intent.putExtra("amount", amount);
        intent.putExtra("sourceAccountNumber", sourceAccountNumber);
        intent.putExtra("maturityDate", tvMaturityDate.getText().toString());
        startActivity(intent);
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }

    /**
     * Format number with dots as thousand separators
     */
    private String formatWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "0";
        // Remove leading zeros unless the value is exactly "0"
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

    /**
     * Show amount in Vietnamese words
     */
    private void showAmountInWordsIfNeeded() {
        String txt = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
        if (txt.isEmpty() || txt.equals("0")) {
            tvAmountInWords.setVisibility(View.GONE);
            return;
        }

        // Clean formatted amount: remove any non-digit characters (grouping dots) and parse full integer
        String intPartStr = txt.replaceAll("[^0-9]", "");

        if (intPartStr.isEmpty()) {
            tvAmountInWords.setVisibility(View.GONE);
            return;
        }

        try {
            long value = Long.parseLong(intPartStr);
            if (value <= 0) {
                tvAmountInWords.setVisibility(View.GONE);
                return;
            }
            String words = numberToVietnameseWords(value);
            tvAmountInWords.setText(words + " đồng");
            tvAmountInWords.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ex) {
            tvAmountInWords.setVisibility(View.GONE);
        }
    }

    /**
     * Convert number to Vietnamese words
     */
    private String numberToVietnameseWords(long num) {
        if (num == 0) return "Không";
        String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
        String[] scales = {""," nghìn"," triệu"," tỷ"," nghìn tỷ"," triệu tỷ"};

        java.util.List<Integer> groups = new java.util.ArrayList<>();
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

        // capitalize first letter
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
                // if there is a hundreds part, say "lẻ"; if not but there was a higher non-zero group, also say "lẻ"
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
                if (tens >= 1) return "mốt"; else return "một";
            case 4:
                if (tens >= 1) return "bốn"; else return "bốn";
            case 5:
                if (tens >= 1) return "lăm"; else return "năm";
            default:
                String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
                return digits[unit];
        }
    }
}


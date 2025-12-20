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

    private TextView tvSourceAccount, tvBalance, tvTerm, tvMaturityDate;
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

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                layoutValidation.setVisibility(View.GONE);
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
        
        if (TextUtils.isEmpty(amountStr)) {
            showValidationError("Quý khách vui lòng nhập số tiền gửi");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr.replace(",", ""));
            
            if (amount < 1000000) {
                showValidationError("Số tiền gửi tối thiểu là 1,000,000 VND");
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
        String amountStr = etAmount.getText().toString().trim().replace(",", "");
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
}


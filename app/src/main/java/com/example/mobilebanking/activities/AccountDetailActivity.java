package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.AccountInfoResponse;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Account Detail Activity
 * Hiển thị chi tiết thông tin tài khoản
 */
public class AccountDetailActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TextView tvAccountNumber, tvAccountHolderName, tvAccountType, tvBankName;
    
    private String accountNumber;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);
        
        accountNumber = getIntent().getStringExtra("accountNumber");
        
        initViews();
        setupToolbar();
        fetchAccountInfo();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvAccountHolderName = findViewById(R.id.tv_account_holder_name);
        tvAccountType = findViewById(R.id.tv_account_type);
        tvBankName = findViewById(R.id.tv_bank_name);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void fetchAccountInfo() {
        if (accountNumber == null || accountNumber.isEmpty()) {
            Toast.makeText(this, "Không có thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AccountApiService service = ApiClient.getAccountApiService();
        service.getAccountInfo(accountNumber).enqueue(new Callback<AccountInfoResponse>() {
            @Override
            public void onResponse(Call<AccountInfoResponse> call, Response<AccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AccountInfoResponse accountInfo = response.body();
                    updateUI(accountInfo);
                } else {
                    Toast.makeText(AccountDetailActivity.this, 
                            "Không thể tải thông tin tài khoản", 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<AccountInfoResponse> call, Throwable t) {
                Toast.makeText(AccountDetailActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(AccountInfoResponse accountInfo) {
        tvAccountNumber.setText(accountInfo.getAccountNumber());
        tvAccountHolderName.setText(accountInfo.getAccountHolderName());
        tvAccountType.setText(formatAccountType(accountInfo.getAccountType()));
        tvBankName.setText(accountInfo.getBankName());
    }
    
    private String formatAccountType(String type) {
        switch (type) {
            case "checking":
                return "Thanh toán";
            case "saving":
                return "Tiết kiệm";
            case "mortgage":
                return "Vay thế chấp";
            default:
                return type;
        }
    }
}

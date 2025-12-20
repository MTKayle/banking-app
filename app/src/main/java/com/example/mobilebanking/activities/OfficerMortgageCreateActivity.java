package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mobilebanking.R;
import com.google.android.material.appbar.MaterialToolbar;

public class OfficerMortgageCreateActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private EditText etPhone, etCollateralType, etDescription, etAmount;
    private Button btnCreate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_create);
        
        initViews();
        setupToolbar();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etPhone = findViewById(R.id.et_phone);
        etCollateralType = findViewById(R.id.et_collateral_type);
        etDescription = findViewById(R.id.et_description);
        etAmount = findViewById(R.id.et_amount);
        btnCreate = findViewById(R.id.btn_create);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupButton() {
        btnCreate.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String collateral = etCollateralType.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            
            if (phone.isEmpty() || collateral.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Toast.makeText(this, "Đã tạo khoản vay thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}


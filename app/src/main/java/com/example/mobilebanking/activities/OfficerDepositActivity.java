package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.google.android.material.appbar.MaterialToolbar;

public class OfficerDepositActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private EditText etAccountNumber, etAmount, etNote;
    private Button btnDeposit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_deposit);
        
        initViews();
        setupToolbar();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etAccountNumber = findViewById(R.id.et_account_number);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        btnDeposit = findViewById(R.id.btn_deposit);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupButton() {
        btnDeposit.setOnClickListener(v -> {
            String accountNumber = etAccountNumber.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();
            
            if (accountNumber.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận nạp tiền")
                .setMessage("Nạp " + amount + " đ vào tài khoản " + accountNumber + "?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    Toast.makeText(this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
}


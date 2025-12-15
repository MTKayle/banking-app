package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;

/**
 * Mobile Top-up Activity - Prepaid mobile recharge
 */
public class MobileTopUpActivity extends AppCompatActivity {
    private Spinner spinnerProvider;
    private EditText etPhoneNumber, etAmount;
    private Button btnTopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_topup);

        setupToolbar();
        initializeViews();
        setupProviders();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mobile Top-up");
        }
    }

    private void initializeViews() {
        spinnerProvider = findViewById(R.id.spinner_provider);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etAmount = findViewById(R.id.et_amount);
        btnTopUp = findViewById(R.id.btn_topup);
    }

    private void setupProviders() {
        String[] providers = {"Viettel", "Vinaphone", "Mobifone", "Vietnamobile"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, providers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvider.setAdapter(adapter);
    }

    private void setupListeners() {
        btnTopUp.setOnClickListener(v -> handleTopUp());
    }

    private void handleTopUp() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (phoneNumber.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Top-up successful!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


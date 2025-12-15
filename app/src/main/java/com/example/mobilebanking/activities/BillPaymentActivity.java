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
 * Bill Payment Activity - Pay utility bills
 */
public class BillPaymentActivity extends AppCompatActivity {
    private Spinner spinnerBillType;
    private EditText etCustomerCode, etAmount;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment);

        setupToolbar();
        initializeViews();
        setupBillTypes();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh Toán Hóa Đơn");
        }
    }

    private void initializeViews() {
        spinnerBillType = findViewById(R.id.spinner_bill_type);
        etCustomerCode = findViewById(R.id.et_customer_code);
        etAmount = findViewById(R.id.et_amount);
        btnPay = findViewById(R.id.btn_pay);
    }

    private void setupBillTypes() {
        String[] billTypes = {"Điện", "Nước", "Internet", "Gas", "Điện Thoại"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, billTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBillType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> handlePayment());
    }

    private void handlePayment() {
        String customerCode = etCustomerCode.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (customerCode.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Thanh toán hóa đơn thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


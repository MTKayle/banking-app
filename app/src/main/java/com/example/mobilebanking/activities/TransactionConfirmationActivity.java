package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Transaction Confirmation Activity - Confirms transaction details before processing
 */
public class TransactionConfirmationActivity extends AppCompatActivity {
    private TextView tvTransactionType, tvFromAccount, tvToAccount, tvAmount, tvNote, tvFee, tvTotal;
    private Button btnConfirm, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_confirmation);

        setupToolbar();
        initializeViews();
        loadTransactionDetails();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Xác Nhận Giao Dịch");
        }
    }

    private void initializeViews() {
        tvTransactionType = findViewById(R.id.tv_transaction_type);
        tvFromAccount = findViewById(R.id.tv_from_account);
        tvToAccount = findViewById(R.id.tv_to_account);
        tvAmount = findViewById(R.id.tv_amount);
        tvNote = findViewById(R.id.tv_note);
        tvFee = findViewById(R.id.tv_fee);
        tvTotal = findViewById(R.id.tv_total);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void loadTransactionDetails() {
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String fromAccount = intent.getStringExtra("from_account");
        String toAccount = intent.getStringExtra("to_account");
        double amount = intent.getDoubleExtra("amount", 0);
        String note = intent.getStringExtra("note");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        tvTransactionType.setText(type);
        tvFromAccount.setText(fromAccount);
        tvToAccount.setText(toAccount);
        tvAmount.setText(formatter.format(amount));
        tvNote.setText(note.isEmpty() ? "Không có ghi chú" : note);

        // Calculate fee (mock calculation)
        double fee = amount * 0.001; // 0.1% fee
        if (fee < 1000) fee = 1000; // Minimum fee
        if (fee > 50000) fee = 50000; // Maximum fee

        tvFee.setText(formatter.format(fee));
        tvTotal.setText(formatter.format(amount + fee));
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> {
            // Navigate to OTP verification
            Intent intent = new Intent(TransactionConfirmationActivity.this, OtpVerificationActivity.class);
            intent.putExtra("phone", "0901234567");
            intent.putExtra("from", "transaction");
            startActivityForResult(intent, 100);
        });

        btnCancel.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // OTP verified successfully
            Toast.makeText(this, "Giao dịch hoàn tất thành công!", Toast.LENGTH_LONG).show();

            // Return to dashboard
            Intent intent = new Intent(this, CustomerDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

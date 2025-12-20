package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.ui_home.UiHomeActivity;

import java.text.DecimalFormat;
import java.util.Locale;

public class SavingSuccessActivity extends AppCompatActivity {

    private TextView tvAmount, tvSavingAccount, tvTerm, tvInterestRate, tvReference;
    private LinearLayout btnShare, btnSaveImage, btnInvite;
    private Button btnDone;

    private String savingBookNumber;
    private double amount;
    private int termMonths;
    private double interestRate;
    private Long savingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_success);

        // Get data from intent
        savingBookNumber = getIntent().getStringExtra("savingBookNumber");
        amount = getIntent().getDoubleExtra("amount", 0.0);
        termMonths = getIntent().getIntExtra("termMonths", 1);
        interestRate = getIntent().getDoubleExtra("interestRate", 0.0);
        savingId = getIntent().getLongExtra("savingId", 0);

        initViews();
        displayInfo();
        setupListeners();
    }

    private void initViews() {
        tvAmount = findViewById(R.id.tv_amount);
        tvSavingAccount = findViewById(R.id.tv_saving_account);
        tvTerm = findViewById(R.id.tv_term);
        tvInterestRate = findViewById(R.id.tv_interest_rate);
        tvReference = findViewById(R.id.tv_reference);
        btnShare = findViewById(R.id.btn_share);
        btnSaveImage = findViewById(R.id.btn_save_image);
        btnInvite = findViewById(R.id.btn_invite);
        btnDone = findViewById(R.id.btn_done);
    }

    private void displayInfo() {
        tvAmount.setText(formatCurrency(amount) + " VND");
        tvSavingAccount.setText(savingBookNumber != null ? savingBookNumber : "N/A");
        tvTerm.setText(termMonths + " Tháng");
        tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%/năm", interestRate));
        tvReference.setText(String.valueOf(savingId));
    }

    private void setupListeners() {
        btnShare.setOnClickListener(v -> {
            shareTransaction();
        });

        btnSaveImage.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng lưu ảnh đang phát triển", Toast.LENGTH_SHORT).show();
        });

        btnInvite.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng giới thiệu đang phát triển", Toast.LENGTH_SHORT).show();
        });

        btnDone.setOnClickListener(v -> {
            navigateToHome();
        });
    }

    private void shareTransaction() {
        String shareText = "✅ Giao dịch thành công!\n\n" +
                "Sản phẩm: Tiết kiệm trả lãi cuối kỳ\n" +
                "Số tiền: " + formatCurrency(amount) + " VND\n" +
                "Tài khoản tiết kiệm: " + savingBookNumber + "\n" +
                "Kỳ hạn: " + termMonths + " tháng\n" +
                "Lãi suất: " + String.format(Locale.getDefault(), "%.1f%%/năm", interestRate) + "\n\n" +
                "HAT Banking - Ngân hàng số tiện lợi";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ"));
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, UiHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }

    @Override
    public void onBackPressed() {
        // Navigate to home instead of going back
        navigateToHome();
    }
}


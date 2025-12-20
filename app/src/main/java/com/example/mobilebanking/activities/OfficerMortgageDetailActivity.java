package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.Locale;

public class OfficerMortgageDetailActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TextView tvAccountNumber, tvStatus, tvCustomerName, tvCustomerPhone;
    private TextView tvPrincipalAmount, tvInterestRate, tvTermMonths, tvMonthlyPayment;
    private TextView tvCreatedDate, tvCollateral;
    private LinearLayout layoutActions;
    private Button btnReject, btnApprove;
    
    private String status;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_detail);
        
        initViews();
        setupToolbar();
        loadData();
        setupButtons();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvStatus = findViewById(R.id.tv_status);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvPrincipalAmount = findViewById(R.id.tv_principal_amount);
        tvInterestRate = findViewById(R.id.tv_interest_rate);
        tvTermMonths = findViewById(R.id.tv_term_months);
        tvMonthlyPayment = findViewById(R.id.tv_monthly_payment);
        tvCreatedDate = findViewById(R.id.tv_created_date);
        tvCollateral = findViewById(R.id.tv_collateral);
        layoutActions = findViewById(R.id.layout_actions);
        btnReject = findViewById(R.id.btn_reject);
        btnApprove = findViewById(R.id.btn_approve);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadData() {
        String accountNumber = getIntent().getStringExtra("account_number");
        String customerName = getIntent().getStringExtra("customer_name");
        String customerPhone = getIntent().getStringExtra("customer_phone");
        double principalAmount = getIntent().getDoubleExtra("principal_amount", 0);
        double interestRate = getIntent().getDoubleExtra("interest_rate", 0);
        int termMonths = getIntent().getIntExtra("term_months", 0);
        double monthlyPayment = getIntent().getDoubleExtra("monthly_payment", 0);
        String createdDate = getIntent().getStringExtra("created_date");
        String collateralType = getIntent().getStringExtra("collateral_type");
        String collateralDesc = getIntent().getStringExtra("collateral_description");
        status = getIntent().getStringExtra("status");
        
        tvAccountNumber.setText(accountNumber);
        tvCustomerName.setText(customerName);
        tvCustomerPhone.setText(customerPhone);
        tvCreatedDate.setText(createdDate);
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvPrincipalAmount.setText(formatter.format(principalAmount) + " đ");
        tvInterestRate.setText(interestRate + "% /năm");
        tvTermMonths.setText(termMonths + " tháng (" + (termMonths/12) + " năm)");
        
        if (monthlyPayment > 0) {
            tvMonthlyPayment.setText(formatter.format(monthlyPayment) + " đ");
        } else {
            tvMonthlyPayment.setText("Chưa có");
        }
        
        String collateralText = collateralType;
        if (collateralDesc != null && !collateralDesc.isEmpty()) {
            collateralText += " - " + collateralDesc;
        }
        tvCollateral.setText(collateralText);
        
        // Status
        if ("PENDING_APPRAISAL".equals(status)) {
            tvStatus.setText("CHỜ DUYỆT");
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_orange);
            layoutActions.setVisibility(View.VISIBLE);
        } else if ("ACTIVE".equals(status)) {
            tvStatus.setText("ĐANG VAY");
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_primary);
            layoutActions.setVisibility(View.GONE);
        } else if ("COMPLETED".equals(status)) {
            tvStatus.setText("HOÀN THÀNH");
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_blue);
            layoutActions.setVisibility(View.GONE);
        } else if ("REJECTED".equals(status)) {
            tvStatus.setText("ĐÃ TỪ CHỐI");
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_red);
            layoutActions.setVisibility(View.GONE);
        }
    }
    
    private void setupButtons() {
        btnApprove.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Phê duyệt khoản vay")
                .setMessage("Bạn có chắc chắn muốn phê duyệt khoản vay này?")
                .setPositiveButton("Phê duyệt", (dialog, which) -> {
                    Toast.makeText(this, "Đã phê duyệt khoản vay thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
        
        btnReject.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Từ chối khoản vay")
                .setMessage("Nhập lý do từ chối:")
                .setView(new android.widget.EditText(this))
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    Toast.makeText(this, "Đã từ chối khoản vay!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
}


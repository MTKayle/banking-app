package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.OfficerMockData;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.Locale;

public class OfficerReportsActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TextView tvTotalCustomers, tvTotalMortgages, tvPendingMortgages;
    private TextView tvTodayTransactions, tvTodayValue, tvActiveMortgages;
    private Button btnExport;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_reports);
        
        initViews();
        setupToolbar();
        loadStatistics();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTotalCustomers = findViewById(R.id.tv_total_customers);
        tvTotalMortgages = findViewById(R.id.tv_total_mortgages);
        tvPendingMortgages = findViewById(R.id.tv_pending_mortgages);
        tvTodayTransactions = findViewById(R.id.tv_today_transactions);
        tvTodayValue = findViewById(R.id.tv_today_value);
        tvActiveMortgages = findViewById(R.id.tv_active_mortgages);
        btnExport = findViewById(R.id.btn_export);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadStatistics() {
        OfficerMockData.ReportStatistics stats = OfficerMockData.getInstance().getReportStatistics();
        
        tvTotalCustomers.setText(String.valueOf(stats.totalCustomers));
        tvTotalMortgages.setText(String.valueOf(stats.totalMortgages));
        tvPendingMortgages.setText(String.valueOf(stats.pendingMortgages));
        tvTodayTransactions.setText(String.valueOf(stats.todayTransactions));
        tvActiveMortgages.setText(String.valueOf(stats.activeMortgages));
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTodayValue.setText(formatter.format(stats.todayTransactionValue) + " đ");
    }
    
    private void setupButton() {
        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "Xuất báo cáo thành công!", Toast.LENGTH_SHORT).show();
        });
    }
}


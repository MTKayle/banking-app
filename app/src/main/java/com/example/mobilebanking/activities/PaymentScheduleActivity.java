package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.PaymentScheduleAdapter;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;
import com.example.mobilebanking.api.dto.PaymentScheduleDTO;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Payment Schedule Activity
 * Màn hình lịch thanh toán chi tiết
 */
public class PaymentScheduleActivity extends AppCompatActivity {
    
    private TextView tvTotalRemaining, tvOverduePeriods;
    private TextView tvPaidCount, tvOverdueCount, tvRemainingCount;
    private RecyclerView rvPaymentSchedules;
    private Button btnPayCurrent, btnPayOff;
    
    private Long mortgageId;
    private PaymentScheduleAdapter adapter;
    private List<PaymentScheduleDTO> schedules = new ArrayList<>();
    private NumberFormat currencyFormatter;
    private String mortgageStatus; // Store mortgage status
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_schedule);
        
        // Get mortgageId from intent
        mortgageId = getIntent().getLongExtra("MORTGAGE_ID", 0);
        
        if (mortgageId == 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khoản vay", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        fetchPaymentSchedules();
    }
    
    private void initViews() {
        tvTotalRemaining = findViewById(R.id.tv_total_remaining);
        tvOverduePeriods = findViewById(R.id.tv_overdue_periods);
        tvPaidCount = findViewById(R.id.tv_paid_count);
        tvOverdueCount = findViewById(R.id.tv_overdue_count);
        tvRemainingCount = findViewById(R.id.tv_remaining_count);
        rvPaymentSchedules = findViewById(R.id.rv_payment_schedules);
        btnPayCurrent = findViewById(R.id.btn_pay_current);
        btnPayOff = findViewById(R.id.btn_pay_off);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new PaymentScheduleAdapter(this, schedules);
        rvPaymentSchedules.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentSchedules.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        btnPayCurrent.setOnClickListener(v -> {
            // Tìm kỳ hiện tại hoặc kỳ quá hạn để thanh toán
            PaymentScheduleDTO currentOrOverdue = null;
            double totalPayment = 0;
            
            for (PaymentScheduleDTO schedule : schedules) {
                if (schedule.getOverdue() != null && schedule.getOverdue()) {
                    // Ưu tiên kỳ quá hạn
                    currentOrOverdue = schedule;
                    totalPayment = schedule.getTotalAmount() != null ? schedule.getTotalAmount() : 0;
                    break;
                } else if (schedule.getCurrentPeriod() != null && schedule.getCurrentPeriod()) {
                    currentOrOverdue = schedule;
                    totalPayment = schedule.getTotalAmount() != null ? schedule.getTotalAmount() : 0;
                }
            }
            
            if (currentOrOverdue == null) {
                Toast.makeText(this, "Không có kỳ nào cần thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Chuyển sang màn hình xác nhận
            Intent intent = new Intent(this, MortgagePaymentConfirmActivity.class);
            intent.putExtra("MORTGAGE_ID", mortgageId);
            intent.putExtra("MORTGAGE_ACCOUNT", getIntent().getStringExtra("MORTGAGE_ACCOUNT"));
            intent.putExtra("PAYMENT_AMOUNT", totalPayment);
            intent.putExtra("PERIOD_NUMBER", currentOrOverdue.getPeriodNumber());
            startActivity(intent);
        });
        
        btnPayOff.setOnClickListener(v -> {
            // Tính tổng số tiền cần tất toán (tổng totalAmount của các kỳ chưa thanh toán)
            double settlementAmount = 0;
            int unpaidCount = 0;
            
            for (PaymentScheduleDTO schedule : schedules) {
                if (!"PAID".equals(schedule.getStatus())) {
                    if (schedule.getTotalAmount() != null) {
                        settlementAmount += schedule.getTotalAmount();
                        unpaidCount++;
                    }
                }
            }
            
            if (unpaidCount == 0) {
                Toast.makeText(this, "Khoản vay đã được thanh toán đầy đủ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Kiểm tra số dư tài khoản thanh toán
            com.example.mobilebanking.utils.DataManager dataManager = 
                    com.example.mobilebanking.utils.DataManager.getInstance(this);
            Long userId = dataManager.getUserId();
            
            AccountApiService service = ApiClient.getAccountApiService();
            final double finalSettlementAmount = settlementAmount;
            
            service.getCheckingAccountInfo(userId).enqueue(new Callback<com.example.mobilebanking.api.dto.CheckingAccountInfoResponse>() {
                @Override
                public void onResponse(Call<com.example.mobilebanking.api.dto.CheckingAccountInfoResponse> call, 
                        Response<com.example.mobilebanking.api.dto.CheckingAccountInfoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        com.example.mobilebanking.api.dto.CheckingAccountInfoResponse account = response.body();
                        Double currentBalance = account.getBalance() != null ? account.getBalance().doubleValue() : 0.0;
                        
                        // Kiểm tra số dư có đủ không
                        if (currentBalance < finalSettlementAmount) {
                            Toast.makeText(PaymentScheduleActivity.this, 
                                    "Số dư tài khoản không đủ để tất toán khoản vay.\nSố dư hiện tại: " + 
                                    formatCurrency(currentBalance) + "\nSố tiền cần thanh toán: " + 
                                    formatCurrency(finalSettlementAmount), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // Chuyển sang màn hình xác nhận tất toán
                        Intent intent = new Intent(PaymentScheduleActivity.this, MortgageSettlementConfirmActivity.class);
                        intent.putExtra("MORTGAGE_ID", mortgageId);
                        intent.putExtra("MORTGAGE_ACCOUNT", getIntent().getStringExtra("MORTGAGE_ACCOUNT"));
                        intent.putExtra("SETTLEMENT_AMOUNT", finalSettlementAmount);
                        intent.putExtra("PAYMENT_ACCOUNT", account.getAccountNumber());
                        intent.putExtra("CURRENT_BALANCE", currentBalance);
                        startActivity(intent);
                    } else {
                        Toast.makeText(PaymentScheduleActivity.this, 
                                "Không thể tải thông tin tài khoản thanh toán", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<com.example.mobilebanking.api.dto.CheckingAccountInfoResponse> call, Throwable t) {
                    Toast.makeText(PaymentScheduleActivity.this, 
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    
    private void fetchPaymentSchedules() {
        AccountApiService service = ApiClient.getAccountApiService();
        Call<MortgageAccountDTO> call = service.getMortgageDetail(mortgageId);
        
        call.enqueue(new Callback<MortgageAccountDTO>() {
            @Override
            public void onResponse(Call<MortgageAccountDTO> call, Response<MortgageAccountDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MortgageAccountDTO mortgage = response.body();
                    mortgageStatus = mortgage.getStatus(); // Store status
                    if (mortgage.getPaymentSchedules() != null && !mortgage.getPaymentSchedules().isEmpty()) {
                        schedules.clear();
                        schedules.addAll(mortgage.getPaymentSchedules());
                        adapter.notifyDataSetChanged();
                        displaySummary();
                    } else {
                        Toast.makeText(PaymentScheduleActivity.this, 
                                "Không có lịch thanh toán", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(PaymentScheduleActivity.this, 
                            "Không thể tải lịch thanh toán", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<MortgageAccountDTO> call, Throwable t) {
                Toast.makeText(PaymentScheduleActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void displaySummary() {
        int paidCount = 0;
        int overdueCount = 0;
        int remainingCount = 0;
        double totalRemaining = 0;
        List<String> overduePeriodsList = new ArrayList<>();
        PaymentScheduleDTO currentPeriod = null;
        
        for (PaymentScheduleDTO schedule : schedules) {
            if ("PAID".equals(schedule.getStatus())) {
                paidCount++;
            } else if (schedule.getOverdue() != null && schedule.getOverdue()) {
                // Kỳ quá hạn
                overdueCount++;
                overduePeriodsList.add("Kỳ " + schedule.getPeriodNumber() + " (Quá hạn)");
                
                // Cộng totalAmount (đã bao gồm gốc + lãi + penalty)
                if (schedule.getTotalAmount() != null) {
                    totalRemaining += schedule.getTotalAmount();
                }
            } else {
                remainingCount++;
                if (schedule.getCurrentPeriod() != null && schedule.getCurrentPeriod()) {
                    // Kỳ hiện tại
                    currentPeriod = schedule;
                    overduePeriodsList.add("Kỳ " + schedule.getPeriodNumber());
                    
                    // Cộng totalAmount (gốc + lãi)
                    if (schedule.getTotalAmount() != null) {
                        totalRemaining += schedule.getTotalAmount();
                    }
                }
            }
        }
        
        // Update counts
        tvPaidCount.setText(String.valueOf(paidCount));
        tvOverdueCount.setText(String.valueOf(overdueCount));
        tvRemainingCount.setText(String.valueOf(remainingCount));
        
        // Update total remaining (sum of totalAmount for overdue + current period)
        tvTotalRemaining.setText(formatCurrency(totalRemaining));
        
        // Update overdue periods text
        if (!overduePeriodsList.isEmpty()) {
            tvOverduePeriods.setText(String.join(", ", overduePeriodsList));
        } else {
            tvOverduePeriods.setText("Không có kỳ cần thanh toán");
        }
        
        // Hide buttons if mortgage is COMPLETED
        if ("COMPLETED".equals(mortgageStatus)) {
            btnPayCurrent.setVisibility(View.GONE);
            btnPayOff.setVisibility(View.GONE);
        } else {
            // Show/hide pay current button based on status
            if (currentPeriod != null || overdueCount > 0) {
                btnPayCurrent.setVisibility(View.VISIBLE);
            } else {
                btnPayCurrent.setVisibility(View.GONE);
            }
            // Always show pay off button for active loans
            btnPayOff.setVisibility(View.VISIBLE);
        }
    }
    
    private String formatCurrency(Double amount) {
        return currencyFormatter.format(amount) + " đ";
    }
}

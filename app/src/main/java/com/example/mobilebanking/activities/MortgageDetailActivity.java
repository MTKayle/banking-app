package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;
import com.example.mobilebanking.api.dto.PaymentScheduleDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mortgage Detail Activity
 * Màn hình chi tiết khoản vay
 */
public class MortgageDetailActivity extends AppCompatActivity {
    
    private TextView tvAccountNumber, tvStatusBadge, tvPrincipalAmount;
    private TextView tvCustomerName, tvCustomerPhone;
    private TextView tvInterestRate, tvTerm, tvCreatedDate;
    private TextView tvCollateralType, tvCollateralDescription;
    private TextView tvScheduleCount, tvPaidCount, tvOverdueCount, tvRemainingCount;
    private TextView tvRemainingBalance;
    private Button btnViewSchedule;
    private CardView cardPaymentSchedule, cardRemainingBalance;
    
    private Long mortgageId;
    private MortgageAccountDTO mortgageDetail;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mortgage_detail);
        
        // Get mortgageId from intent
        mortgageId = getIntent().getLongExtra("MORTGAGE_ID", 0);
        
        if (mortgageId == 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin khoản vay", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        initViews();
        setupToolbar();
        fetchMortgageDetail();
    }
    
    private void initViews() {
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvStatusBadge = findViewById(R.id.tv_status_badge);
        tvPrincipalAmount = findViewById(R.id.tv_principal_amount);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvInterestRate = findViewById(R.id.tv_interest_rate);
        tvTerm = findViewById(R.id.tv_term);
        tvCreatedDate = findViewById(R.id.tv_created_date);
        tvCollateralType = findViewById(R.id.tv_collateral_type);
        tvCollateralDescription = findViewById(R.id.tv_collateral_description);
        tvScheduleCount = findViewById(R.id.tv_schedule_count);
        tvPaidCount = findViewById(R.id.tv_paid_count);
        tvOverdueCount = findViewById(R.id.tv_overdue_count);
        tvRemainingCount = findViewById(R.id.tv_remaining_count);
        tvRemainingBalance = findViewById(R.id.tv_remaining_balance);
        btnViewSchedule = findViewById(R.id.btn_view_schedule);
        cardPaymentSchedule = findViewById(R.id.card_payment_schedule);
        cardRemainingBalance = findViewById(R.id.card_remaining_balance);
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
    
    private void fetchMortgageDetail() {
        AccountApiService service = ApiClient.getAccountApiService();
        Call<MortgageAccountDTO> call = service.getMortgageDetail(mortgageId);
        
        call.enqueue(new Callback<MortgageAccountDTO>() {
            @Override
            public void onResponse(Call<MortgageAccountDTO> call, Response<MortgageAccountDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mortgageDetail = response.body();
                    displayMortgageDetail();
                } else {
                    Toast.makeText(MortgageDetailActivity.this, 
                            "Không thể tải thông tin khoản vay", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Call<MortgageAccountDTO> call, Throwable t) {
                Toast.makeText(MortgageDetailActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void displayMortgageDetail() {
        // Account Number
        tvAccountNumber.setText(mortgageDetail.getAccountNumber());
        
        // Status Badge
        String status = mortgageDetail.getStatus();
        tvStatusBadge.setText(formatStatus(status));
        tvStatusBadge.setBackgroundResource(getStatusBackground(status));
        
        // Principal Amount
        if ("REJECTED".equals(status)) {
            tvPrincipalAmount.setText("Từ chối");
            tvPrincipalAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (mortgageDetail.getPrincipalAmount() != null && mortgageDetail.getPrincipalAmount() > 0) {
            tvPrincipalAmount.setText(formatCurrency(mortgageDetail.getPrincipalAmount()));
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.bidv_primary));
        } else {
            tvPrincipalAmount.setText("Chờ thỏa thuận");
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.bidv_primary));
        }
        
        // Customer Info
        tvCustomerName.setText(mortgageDetail.getCustomerName());
        tvCustomerPhone.setText(mortgageDetail.getCustomerPhone());
        
        // Loan Details
        if (mortgageDetail.getInterestRate() != null) {
            tvInterestRate.setText(String.format("%.4f%% /tháng", mortgageDetail.getInterestRate()));
        }
        
        if (mortgageDetail.getTermMonths() != null) {
            int years = mortgageDetail.getTermMonths() / 12;
            int months = mortgageDetail.getTermMonths() % 12;
            if (years > 0 && months > 0) {
                tvTerm.setText(String.format("%d tháng (%d năm %d tháng)", 
                        mortgageDetail.getTermMonths(), years, months));
            } else if (years > 0) {
                tvTerm.setText(String.format("%d tháng (%d năm)", 
                        mortgageDetail.getTermMonths(), years));
            } else {
                tvTerm.setText(String.format("%d tháng", mortgageDetail.getTermMonths()));
            }
        }
        
        tvCreatedDate.setText(formatDate(mortgageDetail.getCreatedDate()));
        
        // Collateral
        tvCollateralType.setText(formatCollateralType(mortgageDetail.getCollateralType()));
        tvCollateralDescription.setText(mortgageDetail.getCollateralDescription());
        
        // Payment Schedule
        List<PaymentScheduleDTO> schedules = mortgageDetail.getPaymentSchedules();
        if (schedules != null && !schedules.isEmpty()) {
            cardPaymentSchedule.setVisibility(View.VISIBLE);
            tvScheduleCount.setText(schedules.size() + " kỳ");
            
            int paidCount = 0;
            int overdueCount = 0;
            int remainingCount = 0;
            
            for (PaymentScheduleDTO schedule : schedules) {
                if ("PAID".equals(schedule.getStatus())) {
                    paidCount++;
                } else if (schedule.getOverdue() != null && schedule.getOverdue()) {
                    overdueCount++;
                } else {
                    remainingCount++;
                }
            }
            
            tvPaidCount.setText(String.valueOf(paidCount));
            tvOverdueCount.setText(String.valueOf(overdueCount));
            tvRemainingCount.setText(String.valueOf(remainingCount));
            
            btnViewSchedule.setOnClickListener(v -> {
                // Navigate to payment schedule detail screen
                Intent intent = new Intent(MortgageDetailActivity.this, PaymentScheduleActivity.class);
                intent.putExtra("MORTGAGE_ID", mortgageId);
                intent.putExtra("MORTGAGE_ACCOUNT", mortgageDetail.getAccountNumber());
                startActivity(intent);
            });
        } else {
            cardPaymentSchedule.setVisibility(View.GONE);
        }
        
        // Remaining Balance (only for ACTIVE status)
        if ("ACTIVE".equals(status) && mortgageDetail.getRemainingBalance() != null) {
            cardRemainingBalance.setVisibility(View.VISIBLE);
            tvRemainingBalance.setText(formatCurrency(mortgageDetail.getRemainingBalance()));
        } else {
            cardRemainingBalance.setVisibility(View.GONE);
        }
    }
    
    private String formatCurrency(Double amount) {
        return currencyFormatter.format(amount) + " đ";
    }
    
    private String formatCollateralType(String type) {
        if (type == null) return "";
        switch (type) {
            case "HOUSE":
                return "Nhà ở";
            case "CAR":
                return "Xe";
            case "LAND":
                return "Đất";
            default:
                return type;
        }
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING_APPRAISAL":
                return "CHỜ DUYỆT";
            case "ACTIVE":
                return "ĐANG VAY";
            case "REJECTED":
                return "TỪ CHỐI";
            case "COMPLETED":
                return "HOÀN THÀNH";
            default:
                return status;
        }
    }
    
    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        switch (status) {
            case "PENDING_APPRAISAL":
                return R.drawable.bg_status_pending;
            case "ACTIVE":
                return R.drawable.bg_status_active;
            case "REJECTED":
                return R.drawable.bg_status_rejected;
            case "COMPLETED":
                return R.drawable.bg_status_completed;
            default:
                return R.drawable.bg_status_pending;
        }
    }
    
    private String formatDate(String isoDate) {
        if (isoDate == null) return "";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormatter.format(isoFormat.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }
}

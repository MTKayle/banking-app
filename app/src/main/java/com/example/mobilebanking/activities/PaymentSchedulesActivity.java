package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.PaymentScheduleAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MortgageApiService;
import com.example.mobilebanking.api.dto.MortgageAccountResponse;
import com.example.mobilebanking.api.dto.PaymentScheduleResponse;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentSchedulesActivity extends BaseActivity {

    private static final String TAG = "PaymentSchedules";

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private CardView cardPaymentSummary;
    private RecyclerView rvSchedules;

    private TextView tvTotalDue, tvPeriodsInfo;
    private TextView tvPaidCount, tvOverdueCount, tvRemainingCount;

    private PaymentScheduleAdapter adapter;
    private MortgageApiService mortgageApiService;

    private Long mortgageId;
    private String accountNumber;
    private List<PaymentScheduleResponse> paymentSchedules;
    private MortgageAccountResponse currentMortgage;

    private NumberFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_schedules);

        mortgageApiService = ApiClient.getMortgageApiService();
        currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Get data from intent
        mortgageId = getIntent().getLongExtra("mortgage_id", -1);
        accountNumber = getIntent().getStringExtra("account_number");

        initViews();
        setupToolbar();
        setupRecyclerView();

        if (mortgageId > 0) {
            loadMortgageData();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin khoản vay", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        cardPaymentSummary = findViewById(R.id.card_payment_summary);
        rvSchedules = findViewById(R.id.rv_schedules);

        tvTotalDue = findViewById(R.id.tv_total_due);
        tvPeriodsInfo = findViewById(R.id.tv_periods_info);
        tvPaidCount = findViewById(R.id.tv_paid_count);
        tvOverdueCount = findViewById(R.id.tv_overdue_count);
        tvRemainingCount = findViewById(R.id.tv_remaining_count);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (accountNumber != null) {
                getSupportActionBar().setTitle("Lịch thanh toán - " + accountNumber);
            }
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new PaymentScheduleAdapter(this);
        rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        rvSchedules.setAdapter(adapter);
    }

    private void loadMortgageData() {
        showLoading(true);

        mortgageApiService.getMortgageDetail(mortgageId).enqueue(new Callback<MortgageAccountResponse>() {
            @Override
            public void onResponse(Call<MortgageAccountResponse> call, Response<MortgageAccountResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentMortgage = response.body();
                    paymentSchedules = currentMortgage.getPaymentSchedules();

                    if (paymentSchedules != null && !paymentSchedules.isEmpty()) {
                        processSchedules();
                        displayData();
                    } else {
                        Toast.makeText(PaymentSchedulesActivity.this,
                                "Không có lịch thanh toán", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentSchedulesActivity.this,
                            "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MortgageAccountResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(PaymentSchedulesActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processSchedules() {
        // Backend đã trả về currentPeriod và overdue, không cần xử lý thêm
        // Chỉ log để debug
        if (paymentSchedules != null) {
            for (PaymentScheduleResponse schedule : paymentSchedules) {
                Log.d(TAG, "Period " + schedule.getPeriodNumber() + 
                    ": currentPeriod=" + schedule.getCurrentPeriod() + 
                    ", overdue=" + schedule.getOverdue() +
                    ", status=" + schedule.getStatus() +
                    ", totalAmount=" + schedule.getTotalAmount() +
                    ", penaltyAmount=" + schedule.getPenaltyAmount());
            }
        }
    }

    private void displayData() {
        if (paymentSchedules == null) return;

        // Calculate counts and total due
        int paidCount = 0;
        int overdueCount = 0;
        double totalDue = 0;
        List<PaymentScheduleResponse> duePeriods = new ArrayList<>();

        for (PaymentScheduleResponse schedule : paymentSchedules) {
            if (schedule.getIsPaid()) {
                paidCount++;
            } else {
                if (schedule.getIsOverdue()) {
                    overdueCount++;
                    // Tổng = totalAmount + penaltyAmount (nếu có)
                    double amount = schedule.getTotalAmount() != null ? schedule.getTotalAmount() : 0;
                    double penalty = schedule.getPenaltyAmount() != null ? schedule.getPenaltyAmount() : 0;
                    totalDue += amount + penalty;
                    duePeriods.add(schedule);
                } else if (schedule.getIsCurrentPeriod()) {
                    // Kỳ hiện tại: chỉ tính totalAmount
                    double amount = schedule.getTotalAmount() != null ? schedule.getTotalAmount() : 0;
                    totalDue += amount;
                    duePeriods.add(schedule);
                }
            }
        }

        int remainingCount = paymentSchedules.size() - paidCount;

        // Update UI
        tvPaidCount.setText(String.valueOf(paidCount));
        tvOverdueCount.setText(String.valueOf(overdueCount));
        tvRemainingCount.setText(String.valueOf(remainingCount));

        tvTotalDue.setText(currencyFormat.format(totalDue) + " đ");

        // Build periods info text
        if (duePeriods.isEmpty()) {
            tvPeriodsInfo.setText("Không có kỳ nào cần thanh toán");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < duePeriods.size(); i++) {
                PaymentScheduleResponse p = duePeriods.get(i);
                if (i > 0) sb.append(", ");
                sb.append("Kỳ ").append(p.getPeriodNumber());
                if (p.getIsOverdue()) {
                    sb.append(" (Quá hạn)");
                }
            }
            tvPeriodsInfo.setText(sb.toString());
        }

        // Update adapter
        adapter.setSchedules(paymentSchedules);
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

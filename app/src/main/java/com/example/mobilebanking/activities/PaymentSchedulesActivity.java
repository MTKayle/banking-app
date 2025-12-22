package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentSchedulesActivity extends BaseActivity {

    private static final String TAG = "PaymentSchedules";

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private CardView cardPaymentSummary;
    private RecyclerView rvSchedules;
    private LinearLayout layoutActions;
    private MaterialButton btnPayCurrent, btnSettle;

    private TextView tvTotalDue, tvPeriodsInfo;
    private TextView tvPaidCount, tvOverdueCount, tvRemainingCount;

    private PaymentScheduleAdapter adapter;
    private MortgageApiService mortgageApiService;

    private Long mortgageId;
    private String accountNumber;
    private String paymentAccountNumber;
    private List<PaymentScheduleResponse> paymentSchedules;
    private MortgageAccountResponse currentMortgage;

    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_schedules);

        mortgageApiService = ApiClient.getMortgageApiService();
        currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Get data from intent
        mortgageId = getIntent().getLongExtra("mortgage_id", -1);
        accountNumber = getIntent().getStringExtra("account_number");
        paymentAccountNumber = getIntent().getStringExtra("payment_account_number");

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupButtons();

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
        layoutActions = findViewById(R.id.layout_actions);
        btnPayCurrent = findViewById(R.id.btn_pay_current);
        btnSettle = findViewById(R.id.btn_settle);

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

    private void setupButtons() {
        btnPayCurrent.setOnClickListener(v -> showPayCurrentDialog());
        btnSettle.setOnClickListener(v -> showSettleDialog());
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

                    if (paymentAccountNumber == null || paymentAccountNumber.isEmpty()) {
                        paymentAccountNumber = currentMortgage.getAccountNumber();
                    }

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
        if (paymentSchedules == null) return;

        Date today = new Date();
        boolean foundCurrent = false;

        for (PaymentScheduleResponse schedule : paymentSchedules) {
            schedule.setIsCurrentPeriod(false);

            // Check overdue
            if (schedule.getDueDate() != null && !Boolean.TRUE.equals(schedule.getIsPaid())) {
                try {
                    Date dueDate = dateFormat.parse(schedule.getDueDate());
                    if (dueDate != null && dueDate.before(today)) {
                        schedule.setIsOverdue(true);
                    }
                } catch (ParseException e) {
                    // Ignore
                }
            }

            // Mark first unpaid as current
            if (!foundCurrent && !Boolean.TRUE.equals(schedule.getIsPaid())) {
                schedule.setIsCurrentPeriod(true);
                foundCurrent = true;
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
            if (Boolean.TRUE.equals(schedule.getIsPaid())) {
                paidCount++;
            } else {
                if (Boolean.TRUE.equals(schedule.getIsOverdue())) {
                    overdueCount++;
                    // getTotalPayment() đã bao gồm gốc + lãi + phạt
                    double amount = schedule.getTotalPayment() != null ? schedule.getTotalPayment() : 0;
                    totalDue += amount;
                    duePeriods.add(schedule);
                } else if (Boolean.TRUE.equals(schedule.getIsCurrentPeriod())) {
                    // Add current period amount (gốc + lãi)
                    double principal = schedule.getPrincipalAmount() != null ? schedule.getPrincipalAmount() : 0;
                    double interest = schedule.getInterestAmount() != null ? schedule.getInterestAmount() : 0;
                    totalDue += principal + interest;
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
            btnPayCurrent.setEnabled(false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < duePeriods.size(); i++) {
                PaymentScheduleResponse p = duePeriods.get(i);
                if (i > 0) sb.append(", ");
                sb.append("Kỳ ").append(p.getPeriodNumber());
                if (Boolean.TRUE.equals(p.getIsOverdue())) {
                    sb.append(" (Quá hạn)");
                }
            }
            tvPeriodsInfo.setText(sb.toString());
        }

        // Update adapter
        adapter.setSchedules(paymentSchedules);

        // Show/hide actions based on status
        if ("COMPLETED".equals(currentMortgage.getStatus())) {
            layoutActions.setVisibility(View.GONE);
        }
    }

    private void showPayCurrentDialog() {
        if (paymentSchedules == null) return;

        // Calculate total and get periods to pay
        double totalAmount = 0;
        double totalPenalty = 0;
        List<PaymentScheduleResponse> periodsToPay = new ArrayList<>();

        for (PaymentScheduleResponse schedule : paymentSchedules) {
            if (!Boolean.TRUE.equals(schedule.getIsPaid())) {
                if (Boolean.TRUE.equals(schedule.getIsOverdue()) || Boolean.TRUE.equals(schedule.getIsCurrentPeriod())) {
                    double principal = schedule.getPrincipalAmount() != null ? schedule.getPrincipalAmount() : 0;
                    double interest = schedule.getInterestAmount() != null ? schedule.getInterestAmount() : 0;
                    double penalty = schedule.getPenaltyAmount() != null ? schedule.getPenaltyAmount() : 0;
                    
                    totalAmount += principal + interest + penalty;
                    totalPenalty += penalty;
                    periodsToPay.add(schedule);
                }
            }
        }

        if (periodsToPay.isEmpty()) {
            Toast.makeText(this, "Không có kỳ nào cần thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_payment, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvAmount = dialogView.findViewById(R.id.tv_amount);
        TextView tvPeriodsLabel = dialogView.findViewById(R.id.tv_periods_label);
        TextView tvPeriodsDetail = dialogView.findViewById(R.id.tv_periods_detail);
        TextView tvAccountNumber = dialogView.findViewById(R.id.tv_account_number);
        LinearLayout layoutPenaltyWarning = dialogView.findViewById(R.id.layout_penalty_warning);
        TextView tvPenaltyInfo = dialogView.findViewById(R.id.tv_penalty_info);

        tvTitle.setText("Xác nhận thanh toán");
        tvAmount.setText(currencyFormat.format(totalAmount) + " đ");
        tvAccountNumber.setText(paymentAccountNumber != null ? paymentAccountNumber : "N/A");

        // Build periods detail
        StringBuilder sb = new StringBuilder();
        for (PaymentScheduleResponse p : periodsToPay) {
            double principal = p.getPrincipalAmount() != null ? p.getPrincipalAmount() : 0;
            double interest = p.getInterestAmount() != null ? p.getInterestAmount() : 0;
            double penalty = p.getPenaltyAmount() != null ? p.getPenaltyAmount() : 0;
            double baseAmount = principal + interest;

            sb.append("• Kỳ ").append(p.getPeriodNumber());
            if (Boolean.TRUE.equals(p.getIsOverdue())) {
                sb.append(" (Quá hạn)");
            } else {
                sb.append(" (Hiện tại)");
            }
            sb.append(": ").append(currencyFormat.format(baseAmount));
            if (penalty > 0) {
                sb.append(" + phạt ").append(currencyFormat.format(penalty));
            }
            sb.append(" đ\n");
        }
        tvPeriodsDetail.setText(sb.toString().trim());

        // Show penalty warning if any
        if (totalPenalty > 0) {
            layoutPenaltyWarning.setVisibility(View.VISIBLE);
            tvPenaltyInfo.setText("Bao gồm tiền phạt quá hạn: " + currencyFormat.format(totalPenalty) + " đ");
        }

        double finalTotalAmount = totalAmount;
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Xác nhận thanh toán", (dialog, which) -> {
                    makeCurrentPeriodPayment(finalTotalAmount);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void makeCurrentPeriodPayment(double amount) {
        showLoading(true);
        btnPayCurrent.setEnabled(false);

        Map<String, Object> request = new HashMap<>();
        request.put("mortgageId", mortgageId);
        request.put("paymentAmount", amount);
        request.put("paymentAccountNumber", paymentAccountNumber);

        Log.d(TAG, "Payment request: " + request);

        mortgageApiService.makeCurrentPeriodPayment(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                btnPayCurrent.setEnabled(true);

                if (response.isSuccessful()) {
                    new AlertDialog.Builder(PaymentSchedulesActivity.this)
                            .setTitle("Thành công")
                            .setMessage("Thanh toán thành công " + currencyFormat.format(amount) + " đ")
                            .setPositiveButton("OK", (dialog, which) -> {
                                setResult(RESULT_OK);
                                loadMortgageData(); // Reload data
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    String errorMsg = "Thanh toán thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    Toast.makeText(PaymentSchedulesActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                btnPayCurrent.setEnabled(true);
                Toast.makeText(PaymentSchedulesActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSettleDialog() {
        if (currentMortgage == null) return;

        Double remainingBalance = currentMortgage.getRemainingBalance();
        if (remainingBalance == null || remainingBalance <= 0) {
            Toast.makeText(this, "Không có dư nợ cần tất toán", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_payment, null);

        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvAmount = dialogView.findViewById(R.id.tv_amount);
        TextView tvPeriodsLabel = dialogView.findViewById(R.id.tv_periods_label);
        TextView tvPeriodsDetail = dialogView.findViewById(R.id.tv_periods_detail);
        TextView tvAccountNumber = dialogView.findViewById(R.id.tv_account_number);
        LinearLayout layoutPenaltyWarning = dialogView.findViewById(R.id.layout_penalty_warning);

        tvTitle.setText("Xác nhận tất toán");
        tvAmount.setText(currencyFormat.format(remainingBalance) + " đ");
        tvAccountNumber.setText(paymentAccountNumber != null ? paymentAccountNumber : "N/A");
        tvPeriodsLabel.setText("Tất toán khoản vay");
        tvPeriodsDetail.setText("Thanh toán toàn bộ dư nợ còn lại để hoàn tất khoản vay trước hạn.");
        layoutPenaltyWarning.setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Xác nhận tất toán", (dialog, which) -> {
                    makeFullSettlement(remainingBalance);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void makeFullSettlement(double amount) {
        showLoading(true);
        btnSettle.setEnabled(false);

        Map<String, Object> request = new HashMap<>();
        request.put("mortgageId", mortgageId);
        request.put("paymentAmount", amount);
        request.put("paymentAccountNumber", paymentAccountNumber);

        Log.d(TAG, "Settlement request: " + request);

        mortgageApiService.makeMortgagePayment(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                btnSettle.setEnabled(true);

                if (response.isSuccessful()) {
                    new AlertDialog.Builder(PaymentSchedulesActivity.this)
                            .setTitle("Thành công")
                            .setMessage("Tất toán khoản vay thành công!\nSố tiền: " + currencyFormat.format(amount) + " đ")
                            .setPositiveButton("OK", (dialog, which) -> {
                                setResult(RESULT_OK);
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    String errorMsg = "Tất toán thất bại";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    Toast.makeText(PaymentSchedulesActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                btnSettle.setEnabled(true);
                Toast.makeText(PaymentSchedulesActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

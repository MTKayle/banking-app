package com.example.mobilebanking.activities;

import android.content.Intent;
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
import com.example.mobilebanking.api.dto.MortgageApproveRequest;
import com.example.mobilebanking.api.dto.MortgageRejectRequest;
import com.example.mobilebanking.api.dto.PaymentScheduleResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficerMortgageDetailActivity extends BaseActivity {
    
    private static final String TAG = "OfficerMortgageDetail";
    
    private MaterialToolbar toolbar;
    private TextView tvAccountNumber, tvStatus, tvCustomerName, tvCustomerPhone;
    private TextView tvPrincipalAmount, tvInterestRate, tvTermMonths;
    private TextView tvCreatedDate, tvCollateral, tvRejectionReason;
    private CardView cardStatus, cardRejection, cardPaymentSchedule, cardRemainingBalance;
    private LinearLayout layoutActions;
    private MaterialButton btnReject, btnApprove, btnViewSchedules;
    private ProgressBar progressBar;
    
    // Payment schedule summary views
    private TextView tvScheduleCount, tvPaidCount, tvOverdueCount, tvRemainingCount;
    private TextView tvRemainingBalance;
    
    private String status;
    private Long mortgageId;
    private MortgageApiService mortgageApiService;
    private MortgageAccountResponse currentMortgage;
    private List<PaymentScheduleResponse> paymentSchedules;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_detail);
        
        mortgageApiService = ApiClient.getMortgageApiService();
        
        initViews();
        setupToolbar();
        
        // Get mortgageId from intent
        mortgageId = getIntent().getLongExtra("mortgage_id", -1);
        
        if (mortgageId > 0) {
            loadMortgageFromApi();
        } else {
            loadDataFromIntent();
        }
        
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
        tvCreatedDate = findViewById(R.id.tv_created_date);
        tvCollateral = findViewById(R.id.tv_collateral);
        tvRejectionReason = findViewById(R.id.tv_rejection_reason);
        cardStatus = findViewById(R.id.card_status);
        cardRejection = findViewById(R.id.card_rejection);
        cardPaymentSchedule = findViewById(R.id.card_payment_schedule);
        cardRemainingBalance = findViewById(R.id.card_remaining_balance);
        layoutActions = findViewById(R.id.layout_actions);
        btnReject = findViewById(R.id.btn_reject);
        btnApprove = findViewById(R.id.btn_approve);
        btnViewSchedules = findViewById(R.id.btn_view_schedules);
        progressBar = findViewById(R.id.progress_bar);
        
        // Payment schedule summary views
        tvScheduleCount = findViewById(R.id.tv_schedule_count);
        tvPaidCount = findViewById(R.id.tv_paid_count);
        tvOverdueCount = findViewById(R.id.tv_overdue_count);
        tvRemainingCount = findViewById(R.id.tv_remaining_count);
        tvRemainingBalance = findViewById(R.id.tv_remaining_balance);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    /**
     * Load mortgage detail from API
     */
    private void loadMortgageFromApi() {
        showLoading(true);
        
        Call<MortgageAccountResponse> call = mortgageApiService.getMortgageDetail(mortgageId);
        call.enqueue(new Callback<MortgageAccountResponse>() {
            @Override
            public void onResponse(Call<MortgageAccountResponse> call, Response<MortgageAccountResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentMortgage = response.body();
                    displayMortgageData(currentMortgage);
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    Toast.makeText(OfficerMortgageDetailActivity.this, 
                        "Lỗi tải thông tin khoản vay", Toast.LENGTH_SHORT).show();
                    loadDataFromIntent();
                }
            }
            
            @Override
            public void onFailure(Call<MortgageAccountResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                Toast.makeText(OfficerMortgageDetailActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                loadDataFromIntent();
            }
        });
    }
    
    /**
     * Display mortgage data from API response
     */
    private void displayMortgageData(MortgageAccountResponse mortgage) {
        tvAccountNumber.setText(mortgage.getAccountNumber() != null ? mortgage.getAccountNumber() : "N/A");
        tvCustomerName.setText(mortgage.getCustomerName() != null ? mortgage.getCustomerName() : "N/A");
        tvCustomerPhone.setText(mortgage.getCustomerPhone() != null ? mortgage.getCustomerPhone() : "N/A");
        tvCreatedDate.setText(mortgage.getCreatedDate() != null ? mortgage.getCreatedDate() : "N/A");
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        
        Double principalAmount = mortgage.getPrincipalAmount();
        Double interestRate = mortgage.getInterestRate();
        Integer termMonths = mortgage.getTermMonths();
        
        status = mortgage.getStatus();
        boolean isRejected = "REJECTED".equals(status);
        boolean isPending = "PENDING_APPRAISAL".equals(status);
        boolean isActive = "ACTIVE".equals(status);
        boolean isCompleted = "COMPLETED".equals(status);
        
        // Display amount
        if (isRejected) {
            tvPrincipalAmount.setText("Từ chối");
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.red));
        } else if (isPending || principalAmount == null || principalAmount == 0) {
            tvPrincipalAmount.setText("Chờ thỏa thuận");
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            tvPrincipalAmount.setText(formatter.format(principalAmount) + " đ");
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.primary));
        }
        
        // Interest rate (per month)
        if (isRejected) {
            tvInterestRate.setText("Từ chối");
            tvInterestRate.setTextColor(getResources().getColor(R.color.red));
        } else if (interestRate != null && interestRate > 0) {
            tvInterestRate.setText(interestRate + "% /tháng");
        } else {
            tvInterestRate.setText("Chờ xác định");
        }
        
        // Term months
        if (isRejected) {
            tvTermMonths.setText("Từ chối");
            tvTermMonths.setTextColor(getResources().getColor(R.color.red));
        } else if (termMonths != null && termMonths > 0) {
            int years = termMonths / 12;
            int months = termMonths % 12;
            if (years > 0 && months > 0) {
                tvTermMonths.setText(termMonths + " tháng (" + years + " năm " + months + " tháng)");
            } else if (years > 0) {
                tvTermMonths.setText(termMonths + " tháng (" + years + " năm)");
            } else {
                tvTermMonths.setText(termMonths + " tháng");
            }
        } else {
            tvTermMonths.setText("Chờ xác định");
        }
        
        // Collateral info
        String collateralText = getCollateralDisplayName(mortgage.getCollateralType());
        if (mortgage.getCollateralDescription() != null && !mortgage.getCollateralDescription().isEmpty()) {
            collateralText += "\n" + mortgage.getCollateralDescription();
        }
        tvCollateral.setText(collateralText.isEmpty() ? "N/A" : collateralText);
        
        // Update status UI
        updateStatusUI(status, mortgage.getRejectionReason());
        
        // Handle payment schedules for ACTIVE or COMPLETED status
        paymentSchedules = mortgage.getPaymentSchedules();
        if ((isActive || isCompleted) && paymentSchedules != null && !paymentSchedules.isEmpty()) {
            displayPaymentScheduleSummary();
        }
        
        // Show remaining balance for ACTIVE status
        if (isActive && mortgage.getRemainingBalance() != null && cardRemainingBalance != null) {
            cardRemainingBalance.setVisibility(View.VISIBLE);
            tvRemainingBalance.setText(formatter.format(mortgage.getRemainingBalance()) + " đ");
        }
    }
    
    /**
     * Display payment schedule summary
     */
    private void displayPaymentScheduleSummary() {
        if (cardPaymentSchedule == null || paymentSchedules == null) return;
        
        cardPaymentSchedule.setVisibility(View.VISIBLE);
        
        // Mark current period first
        markCurrentPeriod();
        
        // Calculate counts
        int totalCount = paymentSchedules.size();
        int paidCount = 0;
        int overdueCount = 0;
        
        for (PaymentScheduleResponse schedule : paymentSchedules) {
            if (Boolean.TRUE.equals(schedule.getIsPaid())) {
                paidCount++;
            } else if (Boolean.TRUE.equals(schedule.getIsOverdue())) {
                overdueCount++;
            }
        }
        
        int remainingCount = totalCount - paidCount;
        
        // Update UI
        if (tvScheduleCount != null) tvScheduleCount.setText(totalCount + " kỳ");
        if (tvPaidCount != null) tvPaidCount.setText(String.valueOf(paidCount));
        if (tvOverdueCount != null) tvOverdueCount.setText(String.valueOf(overdueCount));
        if (tvRemainingCount != null) tvRemainingCount.setText(String.valueOf(remainingCount));
    }
    
    private String getCollateralDisplayName(String type) {
        if (type == null) return "Không xác định";
        switch (type) {
            case "HOUSE": return "Nhà ở";
            case "LAND": return "Đất";
            case "VEHICLE": return "Xe";
            case "CAR": return "Xe";
            case "OTHER": return "Khác";
            default: return type;
        }
    }
    
    /**
     * Load data from intent (backward compatibility)
     */
    private void loadDataFromIntent() {
        String accountNumber = getIntent().getStringExtra("account_number");
        String customerName = getIntent().getStringExtra("customer_name");
        String customerPhone = getIntent().getStringExtra("customer_phone");
        double principalAmount = getIntent().getDoubleExtra("principal_amount", 0);
        double interestRate = getIntent().getDoubleExtra("interest_rate", 0);
        int termMonths = getIntent().getIntExtra("term_months", 0);
        String createdDate = getIntent().getStringExtra("created_date");
        String collateralType = getIntent().getStringExtra("collateral_type");
        String collateralDesc = getIntent().getStringExtra("collateral_description");
        String rejectionReason = getIntent().getStringExtra("rejection_reason");
        status = getIntent().getStringExtra("status");
        
        tvAccountNumber.setText(accountNumber != null ? accountNumber : "N/A");
        tvCustomerName.setText(customerName != null ? customerName : "N/A");
        tvCustomerPhone.setText(customerPhone != null ? customerPhone : "N/A");
        tvCreatedDate.setText(createdDate != null ? createdDate : "N/A");
        
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        
        boolean isRejected = "REJECTED".equals(status);
        boolean isPending = "PENDING_APPRAISAL".equals(status);
        
        // Display amount
        if (isRejected) {
            tvPrincipalAmount.setText("Từ chối");
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.red));
        } else if (isPending || principalAmount == 0) {
            tvPrincipalAmount.setText("Chờ thỏa thuận");
            tvPrincipalAmount.setTextColor(getResources().getColor(R.color.text_secondary));
        } else {
            tvPrincipalAmount.setText(formatter.format(principalAmount) + " đ");
        }
        
        // Interest rate (per month)
        if (isRejected) {
            tvInterestRate.setText("Từ chối");
            tvInterestRate.setTextColor(getResources().getColor(R.color.red));
        } else if (interestRate > 0) {
            tvInterestRate.setText(interestRate + "% /tháng");
        } else {
            tvInterestRate.setText("Chờ xác định");
        }
        
        // Term months
        if (isRejected) {
            tvTermMonths.setText("Từ chối");
            tvTermMonths.setTextColor(getResources().getColor(R.color.red));
        } else if (termMonths > 0) {
            tvTermMonths.setText(termMonths + " tháng (" + (termMonths/12) + " năm)");
        } else {
            tvTermMonths.setText("Chờ xác định");
        }
        
        String collateralText = getCollateralDisplayName(collateralType);
        if (collateralDesc != null && !collateralDesc.isEmpty()) {
            collateralText += "\n" + collateralDesc;
        }
        tvCollateral.setText(collateralText);
        
        updateStatusUI(status, rejectionReason);
    }
    
    /**
     * Update status UI based on status string
     */
    private void updateStatusUI(String status, String rejectionReason) {
        // Hide rejection card by default
        if (cardRejection != null) {
            cardRejection.setVisibility(View.GONE);
        }
        
        if ("PENDING_APPRAISAL".equals(status)) {
            tvStatus.setText("CHỜ DUYỆT");
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_orange);
            layoutActions.setVisibility(View.VISIBLE);
        } else if ("APPROVED".equals(status)) {
            tvStatus.setText("ĐÃ DUYỆT");
            tvStatus.setBackgroundResource(R.drawable.bg_rounded_primary);
            layoutActions.setVisibility(View.GONE);
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
            
            // Show rejection reason
            if (cardRejection != null && rejectionReason != null && !rejectionReason.isEmpty()) {
                cardRejection.setVisibility(View.VISIBLE);
                tvRejectionReason.setText(rejectionReason);
            }
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void setupButtons() {
        btnApprove.setOnClickListener(v -> showApproveDialog());
        btnReject.setOnClickListener(v -> showRejectDialog());
        
        if (btnViewSchedules != null) {
            btnViewSchedules.setOnClickListener(v -> openPaymentSchedulesActivity());
        }
    }
    
    /**
     * Open PaymentSchedulesActivity
     */
    private void openPaymentSchedulesActivity() {
        if (mortgageId == null || mortgageId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin khoản vay", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, PaymentSchedulesActivity.class);
        intent.putExtra("mortgage_id", mortgageId);
        intent.putExtra("account_number", currentMortgage != null ? currentMortgage.getAccountNumber() : "");
        intent.putExtra("payment_account_number", currentMortgage != null ? currentMortgage.getAccountNumber() : "");
        startActivity(intent);
    }
    
    /**
     * Mark current period in payment schedules
     */
    private void markCurrentPeriod() {
        if (paymentSchedules == null) return;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        
        boolean foundCurrent = false;
        for (PaymentScheduleResponse schedule : paymentSchedules) {
            // Reset current period flag
            schedule.setIsCurrentPeriod(false);
            
            // Find first unpaid period as current
            if (!foundCurrent && !Boolean.TRUE.equals(schedule.getIsPaid())) {
                schedule.setIsCurrentPeriod(true);
                foundCurrent = true;
            }
            
            // Check if overdue (due date < today and not paid)
            if (schedule.getDueDate() != null && !Boolean.TRUE.equals(schedule.getIsPaid())) {
                try {
                    Date dueDate = sdf.parse(schedule.getDueDate());
                    if (dueDate != null && dueDate.before(today)) {
                        schedule.setIsOverdue(true);
                    }
                } catch (ParseException e) {
                    // Ignore parse errors
                }
            }
        }
    }
    
    /**
     * Show approve dialog with editable fields
     */
    private void showApproveDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_approve_mortgage, null);
        
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_amount);
        TextInputEditText etTerm = dialogView.findViewById(R.id.et_term);
        
        // Pre-fill with current values if available
        if (currentMortgage != null) {
            if (currentMortgage.getPrincipalAmount() != null && currentMortgage.getPrincipalAmount() > 0) {
                etAmount.setText(String.valueOf(currentMortgage.getPrincipalAmount().longValue()));
            }
            if (currentMortgage.getTermMonths() != null && currentMortgage.getTermMonths() > 0) {
                etTerm.setText(String.valueOf(currentMortgage.getTermMonths()));
            }
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Phê duyệt khoản vay")
            .setView(dialogView)
            .setPositiveButton("Phê duyệt", (dialog, which) -> {
                String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
                String termStr = etTerm.getText() != null ? etTerm.getText().toString().trim() : "";
                
                if (amountStr.isEmpty() || termStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    Long amount = Long.parseLong(amountStr);
                    Integer term = Integer.parseInt(termStr);
                    
                    if (amount <= 0) {
                        Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (term <= 0) {
                        Toast.makeText(this, "Kỳ hạn phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    approveMortgage(amount, term);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Call approve mortgage API
     * Endpoint: POST /mortgage/approve
     * Body: { mortgageId, principalAmount, termMonths }
     */
    private void approveMortgage(Long amount, Integer term) {
        if (mortgageId == null || mortgageId <= 0) {
            Toast.makeText(this, "Không tìm thấy ID khoản vay", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);
        
        // Create request with exact format: { mortgageId, principalAmount, termMonths }
        MortgageApproveRequest request = new MortgageApproveRequest(mortgageId, amount, term);
        
        Log.d(TAG, "Approve request: mortgageId=" + mortgageId + ", principalAmount=" + amount + ", termMonths=" + term);
        
        Call<MortgageAccountResponse> call = mortgageApiService.approveMortgage(request);
        call.enqueue(new Callback<MortgageAccountResponse>() {
            @Override
            public void onResponse(Call<MortgageAccountResponse> call, Response<MortgageAccountResponse> response) {
                showLoading(false);
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
                
                if (response.isSuccessful()) {
                    new AlertDialog.Builder(OfficerMortgageDetailActivity.this)
                        .setTitle("Thành công")
                        .setMessage("Đã phê duyệt khoản vay thành công!")
                        .setPositiveButton("OK", (dialog, which) -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } else {
                    Log.e(TAG, "Approve Error: " + response.code());
                    String errorMsg = parseErrorMessage(response);
                    showErrorDialog("Lỗi phê duyệt", errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<MortgageAccountResponse> call, Throwable t) {
                showLoading(false);
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
                Log.e(TAG, "Approve Failed: " + t.getMessage(), t);
                showErrorDialog("Lỗi kết nối", "Không thể kết nối server. Vui lòng thử lại.");
            }
        });
    }
    
    /**
     * Show reject dialog with reason input
     */
    private void showRejectDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reject_mortgage, null);
        TextInputEditText etReason = dialogView.findViewById(R.id.et_reason);
        
        new AlertDialog.Builder(this)
            .setTitle("Từ chối khoản vay")
            .setView(dialogView)
            .setPositiveButton("Từ chối", (dialog, which) -> {
                String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";
                if (reason.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show();
                    return;
                }
                rejectMortgage(reason);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Call reject mortgage API
     * Endpoint: POST /mortgage/reject
     */
    private void rejectMortgage(String reason) {
        if (mortgageId == null || mortgageId <= 0) {
            Toast.makeText(this, "Không tìm thấy ID khoản vay", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);
        
        MortgageRejectRequest request = new MortgageRejectRequest(mortgageId, reason);
        
        Call<MortgageAccountResponse> call = mortgageApiService.rejectMortgage(request);
        call.enqueue(new Callback<MortgageAccountResponse>() {
            @Override
            public void onResponse(Call<MortgageAccountResponse> call, Response<MortgageAccountResponse> response) {
                showLoading(false);
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
                
                if (response.isSuccessful()) {
                    new AlertDialog.Builder(OfficerMortgageDetailActivity.this)
                        .setTitle("Đã từ chối")
                        .setMessage("Khoản vay đã bị từ chối.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } else {
                    Log.e(TAG, "Reject Error: " + response.code());
                    String errorMsg = parseErrorMessage(response);
                    showErrorDialog("Lỗi từ chối", errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<MortgageAccountResponse> call, Throwable t) {
                showLoading(false);
                btnApprove.setEnabled(true);
                btnReject.setEnabled(true);
                Log.e(TAG, "Reject Failed: " + t.getMessage(), t);
                showErrorDialog("Lỗi kết nối", "Không thể kết nối server. Vui lòng thử lại.");
            }
        });
    }
    
    /**
     * Parse error message from response
     */
    private String parseErrorMessage(Response<?> response) {
        String errorMsg = "Đã xảy ra lỗi";
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Error body: " + errorBody);
                
                try {
                    JsonObject jsonError = JsonParser.parseString(errorBody).getAsJsonObject();
                    if (jsonError.has("message")) {
                        errorMsg = jsonError.get("message").getAsString();
                    } else if (jsonError.has("error")) {
                        errorMsg = jsonError.get("error").getAsString();
                    }
                } catch (Exception e) {
                    if (!errorBody.isEmpty() && errorBody.length() < 200) {
                        errorMsg = errorBody;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading error body", e);
        }
        return errorMsg;
    }
    
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}

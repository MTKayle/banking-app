package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MortgageApiService;
import com.example.mobilebanking.api.dto.InterestRateResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for managing mortgage interest rates
 * Officer can view and update interest rates for different loan terms
 */
public class OfficerMortgageInterestRateActivity extends BaseActivity {
    
    private static final String TAG = "MortgageInterestRate";
    
    private MaterialToolbar toolbar;
    private RecyclerView rvRates;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    
    private MortgageApiService mortgageApiService;
    private List<InterestRateResponse> interestRates = new ArrayList<>();
    private MortgageInterestRateAdapter adapter;
    private Gson gson = new Gson();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_interest_rate);
        
        mortgageApiService = ApiClient.getMortgageApiService();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadInterestRates();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvRates = findViewById(R.id.rv_rates);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý lãi suất vay");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        rvRates.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MortgageInterestRateAdapter(interestRates, this::showUpdateRateDialog);
        rvRates.setAdapter(adapter);
    }

    /**
     * Load mortgage interest rates from API
     * Endpoint: GET /mortgage/interest-rates
     * Response: {"data": [...], "success": true}
     */
    private void loadInterestRates() {
        showLoading(true);
        
        // Use Map to handle wrapped response {data: [...], success: true}
        Call<Map<String, Object>> call = mortgageApiService.getInterestRates();
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                
                Log.d(TAG, "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Log.d(TAG, "Response body: " + body);
                    
                    // Check success flag
                    Boolean success = (Boolean) body.get("success");
                    if (success != null && success) {
                        // Parse data array
                        Object dataObj = body.get("data");
                        if (dataObj != null) {
                            try {
                                String dataJson = gson.toJson(dataObj);
                                Type listType = new TypeToken<List<InterestRateResponse>>(){}.getType();
                                List<InterestRateResponse> rates = gson.fromJson(dataJson, listType);
                                
                                interestRates.clear();
                                if (rates != null) {
                                    // Sort by minMonths ascending (smallest to largest)
                                    Collections.sort(rates, (r1, r2) -> {
                                        Integer min1 = r1.getMinMonths() != null ? r1.getMinMonths() : 0;
                                        Integer min2 = r2.getMinMonths() != null ? r2.getMinMonths() : 0;
                                        return min1.compareTo(min2);
                                    });
                                    interestRates.addAll(rates);
                                }
                                adapter.notifyDataSetChanged();
                                
                                Log.d(TAG, "Loaded " + interestRates.size() + " interest rates");
                                
                                if (interestRates.isEmpty()) {
                                    showEmptyState();
                                } else {
                                    rvRates.setVisibility(View.VISIBLE);
                                    if (emptyState != null) emptyState.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing data: " + e.getMessage(), e);
                                Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                                    "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                                showEmptyState();
                            }
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                            "Lỗi tải danh sách lãi suất", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Load interest rates error: " + response.code());
                    Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                        "Lỗi tải danh sách lãi suất", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Load interest rates failed: " + t.getMessage(), t);
                Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    /**
     * Show dialog to update interest rate
     */
    private void showUpdateRateDialog(InterestRateResponse rate) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);
        
        TextView tvInfo = new TextView(this);
        String termText = rate.getTermDisplay();
        String currentRate = rate.getInterestRate() != null ? rate.getInterestRate() + "%" : "N/A";
        tvInfo.setText("Kỳ hạn: " + termText + "\nLãi suất hiện tại: " + currentRate);
        tvInfo.setTextSize(14);
        tvInfo.setPadding(0, 0, 0, 20);
        layout.addView(tvInfo);
        
        EditText etNewRate = new EditText(this);
        etNewRate.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etNewRate.setHint("Nhập lãi suất mới (%)");
        if (rate.getInterestRate() != null) {
            etNewRate.setText(String.valueOf(rate.getInterestRate()));
        }
        layout.addView(etNewRate);
        
        new AlertDialog.Builder(this)
            .setTitle("Cập nhật lãi suất vay")
            .setView(layout)
            .setPositiveButton("Cập nhật", (dialog, which) -> {
                String rateStr = etNewRate.getText().toString().trim();
                if (rateStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập lãi suất", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    double newRate = Double.parseDouble(rateStr);
                    if (newRate <= 0 || newRate > 100) {
                        Toast.makeText(this, "Lãi suất phải từ 0 đến 100%", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateInterestRate(rate.getRateId(), newRate);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Lãi suất không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    /**
     * Update interest rate via API
     * Endpoint: PUT /mortgage/interest-rates/update
     * Input: {"rateId": 1, "interestRate": 7.5}
     * Response: {"data": {...}, "success": true, "message": "..."}
     */
    private void updateInterestRate(Long rateId, double newRate) {
        showLoading(true);
        
        Map<String, Object> request = new HashMap<>();
        request.put("rateId", rateId);
        request.put("interestRate", newRate);
        
        Log.d(TAG, "Updating rate - rateId: " + rateId + ", newRate: " + newRate);
        
        Call<Map<String, Object>> call = mortgageApiService.updateMortgageInterestRate(request);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                
                Log.d(TAG, "Update response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    Boolean success = (Boolean) body.get("success");
                    String message = (String) body.get("message");
                    
                    if (success != null && success) {
                        Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                            message != null ? message : "Cập nhật lãi suất thành công!", 
                            Toast.LENGTH_SHORT).show();
                        // Reload data
                        loadInterestRates();
                    } else {
                        Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                            message != null ? message : "Lỗi cập nhật lãi suất", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Update rate error: " + response.code());
                    String errorMsg = "Lỗi cập nhật lãi suất";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(OfficerMortgageInterestRateActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Update rate failed: " + t.getMessage(), t);
                Toast.makeText(OfficerMortgageInterestRateActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showEmptyState() {
        rvRates.setVisibility(View.GONE);
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Inner adapter class for displaying interest rates
     */
    private static class MortgageInterestRateAdapter extends RecyclerView.Adapter<MortgageInterestRateAdapter.ViewHolder> {
        
        private final List<InterestRateResponse> rates;
        private final OnRateClickListener listener;
        
        interface OnRateClickListener {
            void onRateClick(InterestRateResponse rate);
        }
        
        MortgageInterestRateAdapter(List<InterestRateResponse> rates, OnRateClickListener listener) {
            this.rates = rates;
            this.listener = listener;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mortgage_interest_rate, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            InterestRateResponse rate = rates.get(position);
            holder.bind(rate, listener);
        }
        
        @Override
        public int getItemCount() {
            return rates.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTermMonths, tvInterestRate, tvDescription;
            
            ViewHolder(View itemView) {
                super(itemView);
                tvTermMonths = itemView.findViewById(R.id.tv_term_months);
                tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);
                tvDescription = itemView.findViewById(R.id.tv_description);
            }
            
            void bind(InterestRateResponse rate, OnRateClickListener listener) {
                // Use getTermDisplay() for better display
                tvTermMonths.setText(rate.getTermDisplay());
                
                String rateText = rate.getInterestRate() != null ? rate.getInterestRate() + "%" : "N/A";
                tvInterestRate.setText(rateText);
                
                if (tvDescription != null) {
                    // Show min-max months range
                    String rangeText = "";
                    if (rate.getMinMonths() != null) {
                        if (rate.getMaxMonths() != null) {
                            rangeText = "Từ " + rate.getMinMonths() + " đến " + rate.getMaxMonths() + " tháng";
                        } else {
                            rangeText = "Trên " + rate.getMinMonths() + " tháng";
                        }
                    }
                    tvDescription.setText(rangeText);
                }
                
                itemView.setOnClickListener(v -> listener.onRateClick(rate));
            }
        }
    }
}

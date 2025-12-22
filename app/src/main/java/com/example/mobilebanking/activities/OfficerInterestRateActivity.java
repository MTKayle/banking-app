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
import com.example.mobilebanking.adapters.SavingTermAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.dto.SavingRateUpdateRequest;
import com.example.mobilebanking.api.dto.SavingTermDTO;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficerInterestRateActivity extends BaseActivity {
    
    private static final String TAG = "OfficerInterestRate";
    
    private MaterialToolbar toolbar;
    private RecyclerView rvTerms;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    
    private AccountApiService accountApiService;
    private List<SavingTermDTO> savingTerms = new ArrayList<>();
    private SavingTermAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_interest_rate);
        
        accountApiService = ApiClient.getAccountApiService();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadSavingTerms();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTerms = findViewById(R.id.rv_terms);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        rvTerms.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup adapter with click listener for editing (using DTO interface)
        adapter = new SavingTermAdapter(savingTerms, (SavingTermAdapter.OnTermDTOClickListener) term -> {
            showUpdateRateDialog(term);
        });
        rvTerms.setAdapter(adapter);
    }
    
    /**
     * Load saving terms from API
     * Endpoint: GET /saving/terms
     */
    private void loadSavingTerms() {
        showLoading(true);
        
        Call<List<SavingTermDTO>> call = accountApiService.getSavingTerms();
        call.enqueue(new Callback<List<SavingTermDTO>>() {
            @Override
            public void onResponse(Call<List<SavingTermDTO>> call, Response<List<SavingTermDTO>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    savingTerms.clear();
                    savingTerms.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    if (savingTerms.isEmpty()) {
                        showEmptyState();
                    } else {
                        rvTerms.setVisibility(View.VISIBLE);
                        if (emptyState != null) emptyState.setVisibility(View.GONE);
                    }
                    
                    Log.d(TAG, "Loaded " + savingTerms.size() + " saving terms");
                } else {
                    Log.e(TAG, "Load saving terms error: " + response.code());
                    Toast.makeText(OfficerInterestRateActivity.this, 
                        "Lỗi tải danh sách kỳ hạn", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<SavingTermDTO>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Load saving terms failed: " + t.getMessage(), t);
                Toast.makeText(OfficerInterestRateActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    /**
     * Show dialog to update interest rate
     */
    private void showUpdateRateDialog(SavingTermDTO term) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);
        
        TextView tvInfo = new TextView(this);
        tvInfo.setText("Kỳ hạn: " + term.getDisplayName() + "\nLãi suất hiện tại: " + term.getInterestRate() + "%");
        layout.addView(tvInfo);
        
        EditText etNewRate = new EditText(this);
        etNewRate.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etNewRate.setHint("Nhập lãi suất mới (%)");
        etNewRate.setText(String.valueOf(term.getInterestRate()));
        layout.addView(etNewRate);
        
        new AlertDialog.Builder(this)
            .setTitle("Cập nhật lãi suất")
            .setView(layout)
            .setPositiveButton("Cập nhật", (dialog, which) -> {
                String rateStr = etNewRate.getText().toString().trim();
                if (rateStr.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập lãi suất", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    double newRate = Double.parseDouble(rateStr);
                    updateSavingRate(term.getTermType(), newRate);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Lãi suất không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Update saving term rate via API
     * Endpoint: PUT /saving/terms/update-rate
     */
    private void updateSavingRate(String termType, double newRate) {
        showLoading(true);
        
        SavingRateUpdateRequest request = new SavingRateUpdateRequest(termType, newRate);
        
        Call<Map<String, Object>> call = accountApiService.updateSavingTermRate(request);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    Toast.makeText(OfficerInterestRateActivity.this, 
                        "Cập nhật lãi suất thành công!", Toast.LENGTH_SHORT).show();
                    // Reload data
                    loadSavingTerms();
                } else {
                    Log.e(TAG, "Update rate error: " + response.code());
                    Toast.makeText(OfficerInterestRateActivity.this, 
                        "Lỗi cập nhật lãi suất", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Update rate failed: " + t.getMessage(), t);
                Toast.makeText(OfficerInterestRateActivity.this, 
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
        rvTerms.setVisibility(View.GONE);
        if (emptyState != null) {
            emptyState.setVisibility(View.VISIBLE);
        }
    }
}


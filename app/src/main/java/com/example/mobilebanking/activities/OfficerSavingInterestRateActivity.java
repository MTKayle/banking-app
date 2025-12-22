package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.example.mobilebanking.api.SavingApiService;
import com.example.mobilebanking.api.dto.SavingTermListResponse;
import com.example.mobilebanking.api.dto.SavingTermResponse;
import com.example.mobilebanking.api.dto.SavingTermUpdateRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OfficerSavingInterestRateActivity - Quản lý lãi suất tiết kiệm
 */
public class OfficerSavingInterestRateActivity extends BaseActivity {

    private static final String TAG = "OfficerSavingRate";

    private MaterialToolbar toolbar;
    private RecyclerView rvTerms;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    private SavingTermAdapter adapter;
    private List<SavingTermResponse> termList = new ArrayList<>();

    private SavingApiService savingApiService;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_saving_interest_rate);

        savingApiService = ApiClient.getSavingApiService();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadSavingTerms();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTerms = findViewById(R.id.rv_terms);
        emptyState = findViewById(R.id.empty_state);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new SavingTermAdapter(term -> {
            showEditDialog(term);
        });
        rvTerms.setLayoutManager(new LinearLayoutManager(this));
        rvTerms.setAdapter(adapter);
    }

    private void loadSavingTerms() {
        if (isLoading) return;

        isLoading = true;
        showLoading(true);
        termList.clear();

        Call<SavingTermListResponse> call = savingApiService.getSavingTerms();
        call.enqueue(new Callback<SavingTermListResponse>() {
            @Override
            public void onResponse(Call<SavingTermListResponse> call, 
                                   Response<SavingTermListResponse> response) {
                isLoading = false;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    SavingTermListResponse result = response.body();
                    if (result.getSuccess() && result.getData() != null) {
                        termList.addAll(result.getData());
                        // Sort by months ascending
                        Collections.sort(termList, (t1, t2) -> {
                            int m1 = t1.getMonths() != null ? t1.getMonths() : 0;
                            int m2 = t2.getMonths() != null ? t2.getMonths() : 0;
                            return Integer.compare(m1, m2);
                        });
                        adapter.setTermList(termList);
                        Log.d(TAG, "Loaded " + termList.size() + " terms");
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    Toast.makeText(OfficerSavingInterestRateActivity.this,
                            "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
                updateEmptyState();
            }

            @Override
            public void onFailure(Call<SavingTermListResponse> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Log.e(TAG, "API Failed: " + t.getMessage(), t);
                Toast.makeText(OfficerSavingInterestRateActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void showEditDialog(SavingTermResponse term) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_edit_saving_rate, null);

        TextView tvTermName = dialogView.findViewById(R.id.tv_term_name);
        EditText etInterestRate = dialogView.findViewById(R.id.et_interest_rate);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        tvTermName.setText("Kỳ hạn: " + term.getDisplayName());
        etInterestRate.setText(String.valueOf(term.getInterestRate()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String rateStr = etInterestRate.getText().toString().trim();
            if (rateStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lãi suất", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newRate = Double.parseDouble(rateStr);
                if (newRate < 0 || newRate > 100) {
                    Toast.makeText(this, "Lãi suất phải từ 0 đến 100", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updateInterestRate(term.getTermType(), newRate);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Lãi suất không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateInterestRate(String termType, double newRate) {
        showLoading(true);

        SavingTermUpdateRequest request = new SavingTermUpdateRequest(termType, newRate);
        Call<SavingTermResponse> call = savingApiService.updateSavingTermRate(request);

        call.enqueue(new Callback<SavingTermResponse>() {
            @Override
            public void onResponse(Call<SavingTermResponse> call, 
                                   Response<SavingTermResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    SavingTermResponse updatedTerm = response.body();
                    Log.d(TAG, "Updated term: " + updatedTerm.getTermType() + 
                            " - Rate: " + updatedTerm.getInterestRate());
                    
                    Toast.makeText(OfficerSavingInterestRateActivity.this,
                            "Cập nhật lãi suất thành công", Toast.LENGTH_SHORT).show();
                    
                    // Reload data to show updated values
                    isLoading = false;
                    loadSavingTerms();
                } else {
                    Log.e(TAG, "Update Error: " + response.code());
                    Toast.makeText(OfficerSavingInterestRateActivity.this,
                            "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SavingTermResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Update Failed: " + t.getMessage(), t);
                Toast.makeText(OfficerSavingInterestRateActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvTerms.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (termList.isEmpty()) {
            rvTerms.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvTerms.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}

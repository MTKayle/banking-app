package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.OfficerMortgageAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MortgageApiService;
import com.example.mobilebanking.api.dto.MortgageAccountResponse;
import com.example.mobilebanking.models.MortgageModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OfficerMortgageListActivity - Danh sách khoản vay cho Officer
 * Features:
 * - RecyclerView hiển thị mortgages (card view)
 * - 4 Tabs: Chờ duyệt, Đang vay, Từ chối, Hoàn thành
 * - Search bar
 * - Click vào mortgage → OfficerMortgageDetailActivity
 * - FAB: "Tạo khoản vay mới" → OfficerMortgageCreateActivity
 */
public class OfficerMortgageListActivity extends BaseActivity {
    
    private static final String TAG = "OfficerMortgageList";
    
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView rvMortgages;
    private LinearLayout emptyState;
    private FloatingActionButton fabCreate;
    private ProgressBar progressBar;
    
    private OfficerMortgageAdapter adapter;
    private List<MortgageModel> mortgageList = new ArrayList<>();
    
    private String currentStatus = "PENDING_APPRAISAL"; // Default tab
    private String searchQuery = "";
    
    private MortgageApiService mortgageApiService;
    
    // Flag để tránh gọi API nhiều lần
    private boolean isLoading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_list);
        
        // Initialize API service
        mortgageApiService = ApiClient.getMortgageApiService();
        
        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSearch();
        setupFAB();
        
        // Load data for first tab
        loadMortgagesByStatus(currentStatus);
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.et_search);
        rvMortgages = findViewById(R.id.rv_mortgages);
        emptyState = findViewById(R.id.empty_state);
        fabCreate = findViewById(R.id.fab_create);
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
    
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Chờ duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang vay"));
        tabLayout.addTab(tabLayout.newTab().setText("Từ chối"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatus = "PENDING_APPRAISAL";
                        break;
                    case 1:
                        currentStatus = "ACTIVE";
                        break;
                    case 2:
                        currentStatus = "REJECTED";
                        break;
                    case 3:
                        currentStatus = "COMPLETED";
                        break;
                }
                Log.d(TAG, "Tab selected: " + tab.getPosition() + ", status: " + currentStatus);
                // Luôn gọi API khi chuyển tab
                loadMortgagesByStatus(currentStatus);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Khi bấm lại tab đang chọn, cũng refresh data
                Log.d(TAG, "Tab reselected: " + tab.getPosition() + ", status: " + currentStatus);
                loadMortgagesByStatus(currentStatus);
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new OfficerMortgageAdapter(mortgage -> {
            // Click vào mortgage → mở detail
            Intent intent = new Intent(OfficerMortgageListActivity.this, OfficerMortgageDetailActivity.class);
            intent.putExtra("mortgage_id", mortgage.getMortgageId());
            intent.putExtra("account_number", mortgage.getAccountNumber());
            intent.putExtra("customer_name", mortgage.getCustomerName());
            intent.putExtra("customer_phone", mortgage.getCustomerPhone());
            intent.putExtra("principal_amount", mortgage.getPrincipalAmount());
            intent.putExtra("interest_rate", mortgage.getInterestRate());
            intent.putExtra("term_months", mortgage.getTermMonths());
            intent.putExtra("status", mortgage.getStatus());
            intent.putExtra("created_date", mortgage.getCreatedDate());
            intent.putExtra("collateral_type", mortgage.getCollateralType());
            intent.putExtra("collateral_description", mortgage.getCollateralDescription());
            intent.putExtra("monthly_payment", mortgage.getMonthlyPayment());
            intent.putExtra("rejection_reason", mortgage.getRejectionReason());
            startActivity(intent);
        });
        
        rvMortgages.setLayoutManager(new LinearLayoutManager(this));
        rvMortgages.setAdapter(adapter);
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase(Locale.getDefault());
                filterBySearch();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupFAB() {
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(OfficerMortgageListActivity.this, OfficerMortgageCreateActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Load mortgages by status from API
     */
    private void loadMortgagesByStatus(String status) {
        // Tránh gọi API nhiều lần khi đang loading
        if (isLoading) {
            Log.d(TAG, "Already loading, skip...");
            return;
        }
        
        isLoading = true;
        showLoading(true);
        
        // Clear list trước khi load mới
        mortgageList.clear();
        adapter.setMortgageList(mortgageList);
        
        Log.d(TAG, "Loading mortgages with status: " + status);
        
        Call<List<MortgageAccountResponse>> call = mortgageApiService.getMortgagesByStatus(status);
        
        call.enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                isLoading = false;
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Loaded " + response.body().size() + " mortgages");
                    for (MortgageAccountResponse resp : response.body()) {
                        mortgageList.add(convertToModel(resp));
                    }
                    filterBySearch();
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    Toast.makeText(OfficerMortgageListActivity.this, 
                            "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Log.e(TAG, "API Failed: " + t.getMessage(), t);
                Toast.makeText(OfficerMortgageListActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }
    
    private MortgageModel convertToModel(MortgageAccountResponse resp) {
        return new MortgageModel(
                resp.getMortgageId(),
                resp.getAccountNumber() != null ? resp.getAccountNumber() : "",
                resp.getCustomerName() != null ? resp.getCustomerName() : "",
                resp.getCustomerPhone(),
                resp.getPrincipalAmount() != null ? resp.getPrincipalAmount() : 0.0,
                resp.getInterestRate() != null ? resp.getInterestRate() : 0.0,
                resp.getTermMonths() != null ? resp.getTermMonths() : 0,
                resp.getStatus() != null ? resp.getStatus() : "",
                resp.getCreatedDate() != null ? resp.getCreatedDate() : "",
                resp.getCollateralType() != null ? resp.getCollateralType() : "",
                resp.getCollateralDescription(),
                0.0, // monthlyPayment - calculate if needed
                resp.getRejectionReason() // rejection reason
        );
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvMortgages.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void updateEmptyState() {
        if (mortgageList.isEmpty()) {
            rvMortgages.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvMortgages.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    /**
     * Filter by search query (local filter)
     */
    private void filterBySearch() {
        if (searchQuery.isEmpty()) {
            adapter.setMortgageList(mortgageList);
        } else {
            List<MortgageModel> filtered = new ArrayList<>();
            for (MortgageModel mortgage : mortgageList) {
                String customerName = mortgage.getCustomerName().toLowerCase(Locale.getDefault());
                String accountNumber = mortgage.getAccountNumber().toLowerCase(Locale.getDefault());
                String phone = mortgage.getCustomerPhone() != null ? 
                              mortgage.getCustomerPhone().toLowerCase(Locale.getDefault()) : "";
                
                if (customerName.contains(searchQuery) || 
                    accountNumber.contains(searchQuery) || 
                    phone.contains(searchQuery)) {
                    filtered.add(mortgage);
                }
            }
            adapter.setMortgageList(filtered);
        }
        
        updateEmptyState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại từ màn hình khác
        Log.d(TAG, "onResume - refreshing data for status: " + currentStatus);
        isLoading = false; // Reset flag để cho phép load lại
        loadMortgagesByStatus(currentStatus);
    }
}

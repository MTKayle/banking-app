package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
 * - 4 Tabs: Tất cả, Chờ duyệt, Đang vay, Hoàn thành
 * - Search bar
 * - Click vào mortgage → OfficerMortgageDetailActivity
 * - FAB: "Tạo khoản vay mới" → OfficerMortgageCreateActivity
 */
public class OfficerMortgageListActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView rvMortgages;
    private LinearLayout emptyState;
    private FloatingActionButton fabCreate;
    private ProgressBar progressBar;
    
    private OfficerMortgageAdapter adapter;
    private List<MortgageModel> allMortgages = new ArrayList<>();
    private List<MortgageModel> filteredMortgages = new ArrayList<>();
    
    private String currentFilter = "ALL"; // ALL, PENDING, ACTIVE, COMPLETED
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
        loadMortgages();
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
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Chờ duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang vay"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "ALL";
                        break;
                    case 1:
                        currentFilter = "PENDING";
                        break;
                    case 2:
                        currentFilter = "ACTIVE";
                        break;
                    case 3:
                        currentFilter = "COMPLETED";
                        break;
                }
                filterMortgages();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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
                filterMortgages();
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
    
    private void loadMortgages() {
        // Tránh gọi API nhiều lần khi đang loading
        if (isLoading) {
            return;
        }
        isLoading = true;
        showLoading(true);
        
        // Clear list trước khi load mới
        allMortgages.clear();
        
        // Determine which API to call based on current filter
        Call<List<MortgageAccountResponse>> call;
        
        if (!searchQuery.isEmpty()) {
            // Search by phone
            String status = getApiStatus();
            if (status != null) {
                call = mortgageApiService.searchMortgages(status, searchQuery);
            } else {
                // Search in all - call pending first, then we'll handle locally
                call = mortgageApiService.getPendingMortgages();
            }
        } else {
            switch (currentFilter) {
                case "PENDING":
                    call = mortgageApiService.getMortgagesByStatus("PENDING_APPRAISAL");
                    break;
                case "ACTIVE":
                    call = mortgageApiService.getMortgagesByStatus("ACTIVE");
                    break;
                case "COMPLETED":
                    // Get both COMPLETED and REJECTED
                    loadCompletedMortgages();
                    return;
                case "ALL":
                default:
                    // Load all by getting pending first
                    loadAllMortgages();
                    return;
            }
        }
        
        call.enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                isLoading = false;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    for (MortgageAccountResponse resp : response.body()) {
                        allMortgages.add(convertToModel(resp));
                    }
                    filterMortgages();
                } else {
                    Toast.makeText(OfficerMortgageListActivity.this, 
                            "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Toast.makeText(OfficerMortgageListActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }
    
    private void loadAllMortgages() {
        // Load PENDING_APPRAISAL
        mortgageApiService.getMortgagesByStatus("PENDING_APPRAISAL").enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MortgageAccountResponse resp : response.body()) {
                        allMortgages.add(convertToModel(resp));
                    }
                }
                // Load ACTIVE
                loadActiveMortgages();
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                loadActiveMortgages();
            }
        });
    }
    
    private void loadActiveMortgages() {
        mortgageApiService.getMortgagesByStatus("ACTIVE").enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MortgageAccountResponse resp : response.body()) {
                        allMortgages.add(convertToModel(resp));
                    }
                }
                // Load COMPLETED
                loadCompletedAndRejectedMortgages();
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                loadCompletedAndRejectedMortgages();
            }
        });
    }
    
    private void loadCompletedAndRejectedMortgages() {
        mortgageApiService.getMortgagesByStatus("COMPLETED").enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MortgageAccountResponse resp : response.body()) {
                        allMortgages.add(convertToModel(resp));
                    }
                }
                // Load REJECTED
                loadRejectedMortgages();
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                loadRejectedMortgages();
            }
        });
    }
    
    private void loadRejectedMortgages() {
        mortgageApiService.getMortgagesByStatus("REJECTED").enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                isLoading = false;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    for (MortgageAccountResponse resp : response.body()) {
                        allMortgages.add(convertToModel(resp));
                    }
                }
                filterMortgages();
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                filterMortgages();
            }
        });
    }
    
    private void loadCompletedMortgages() {
        mortgageApiService.getMortgagesByStatus("COMPLETED").enqueue(new Callback<List<MortgageAccountResponse>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                   Response<List<MortgageAccountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MortgageAccountResponse resp : response.body()) {
                        allMortgages.add(convertToModel(resp));
                    }
                }
                // Also load REJECTED
                mortgageApiService.getMortgagesByStatus("REJECTED").enqueue(new Callback<List<MortgageAccountResponse>>() {
                    @Override
                    public void onResponse(Call<List<MortgageAccountResponse>> call, 
                                           Response<List<MortgageAccountResponse>> response) {
                        isLoading = false;
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            for (MortgageAccountResponse resp : response.body()) {
                                allMortgages.add(convertToModel(resp));
                            }
                        }
                        filterMortgages();
                    }
                    
                    @Override
                    public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                        isLoading = false;
                        showLoading(false);
                        filterMortgages();
                    }
                });
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountResponse>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Toast.makeText(OfficerMortgageListActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }
    
    private String getApiStatus() {
        switch (currentFilter) {
            case "PENDING":
                return "PENDING_APPRAISAL";
            case "ACTIVE":
                return "ACTIVE";
            case "COMPLETED":
                return "COMPLETED";
            default:
                return null;
        }
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
        if (allMortgages.isEmpty()) {
            rvMortgages.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvMortgages.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    private void filterMortgages() {
        filteredMortgages.clear();
        
        for (MortgageModel mortgage : allMortgages) {
            // Filter by tab
            boolean matchesTab = false;
            if (currentFilter.equals("ALL")) {
                matchesTab = true;
            } else if (currentFilter.equals("PENDING") && "PENDING_APPRAISAL".equals(mortgage.getStatus())) {
                matchesTab = true;
            } else if (currentFilter.equals("ACTIVE") && "ACTIVE".equals(mortgage.getStatus())) {
                matchesTab = true;
            } else if (currentFilter.equals("COMPLETED") && 
                      ("COMPLETED".equals(mortgage.getStatus()) || "REJECTED".equals(mortgage.getStatus()))) {
                matchesTab = true;
            }
            
            if (!matchesTab) continue;
            
            // Filter by search query
            if (!searchQuery.isEmpty()) {
                String customerName = mortgage.getCustomerName().toLowerCase(Locale.getDefault());
                String accountNumber = mortgage.getAccountNumber().toLowerCase(Locale.getDefault());
                String phone = mortgage.getCustomerPhone() != null ? 
                              mortgage.getCustomerPhone().toLowerCase(Locale.getDefault()) : "";
                
                if (!customerName.contains(searchQuery) && 
                    !accountNumber.contains(searchQuery) && 
                    !phone.contains(searchQuery)) {
                    continue;
                }
            }
            
            filteredMortgages.add(mortgage);
        }
        
        // Update UI
        adapter.setMortgageList(filteredMortgages);
        
        if (filteredMortgages.isEmpty()) {
            rvMortgages.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvMortgages.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại
        loadMortgages();
    }
}


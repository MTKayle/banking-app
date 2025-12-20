package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.OfficerMortgageAdapter;
import com.example.mobilebanking.models.MortgageModel;
import com.example.mobilebanking.utils.OfficerMockData;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    
    private OfficerMortgageAdapter adapter;
    private List<MortgageModel> allMortgages = new ArrayList<>();
    private List<MortgageModel> filteredMortgages = new ArrayList<>();
    
    private String currentFilter = "ALL"; // ALL, PENDING, ACTIVE, COMPLETED
    private String searchQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_list);
        
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
        // Load mock data
        allMortgages = OfficerMockData.getInstance().getMockMortgages();
        filterMortgages();
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


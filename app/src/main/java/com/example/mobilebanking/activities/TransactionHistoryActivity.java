package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.TransactionAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.TransactionApiService;
import com.example.mobilebanking.api.dto.TransactionDTO;
import com.example.mobilebanking.api.dto.TransactionResponse;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Transaction History Activity - BIDV Style
 * Hiển thị lịch sử giao dịch với 3 tabs filter
 */
public class TransactionHistoryActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvTransactions;
    private LinearLayout emptyState;
    private EditText etSearch;
    private ImageView ivFilter;
    
    private TransactionAdapter adapter;
    private List<TransactionDTO> allTransactions = new ArrayList<>();
    private List<TransactionDTO> filteredTransactions = new ArrayList<>();
    
    private String currentFilter = "ALL"; // ALL, IN, OUT
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        
        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSearch();
        fetchTransactions();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        rvTransactions = findViewById(R.id.rv_transactions);
        emptyState = findViewById(R.id.empty_state);
        etSearch = findViewById(R.id.et_search);
        ivFilter = findViewById(R.id.iv_filter);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        ivFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Filter: Chọn khoảng thời gian", Toast.LENGTH_SHORT).show();
            // TODO: Show date filter bottom sheet
        });
    }
    
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Tiền vào"));
        tabLayout.addTab(tabLayout.newTab().setText("Tiền ra"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "ALL";
                        break;
                    case 1:
                        currentFilter = "IN";
                        break;
                    case 2:
                        currentFilter = "OUT";
                        break;
                }
                filterTransactions();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, filteredTransactions, transaction -> {
            // Show transaction detail bottom sheet
            com.example.mobilebanking.fragments.TransactionDetailBottomSheet bottomSheet = 
                    com.example.mobilebanking.fragments.TransactionDetailBottomSheet.newInstance(transaction);
            bottomSheet.show(getSupportFragmentManager(), "TransactionDetail");
        });
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterTransactions();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void fetchTransactions() {
        TransactionApiService service = ApiClient.getTransactionApiService();
        service.getMyTransactions().enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<TransactionResponse> call, @NonNull Response<TransactionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTransactions.clear();
                    allTransactions.addAll(response.body().getData());
                    filterTransactions();
                } else {
                    Toast.makeText(TransactionHistoryActivity.this, 
                            "Không thể tải lịch sử giao dịch", 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<TransactionResponse> call, @NonNull Throwable t) {
                Toast.makeText(TransactionHistoryActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void filterTransactions() {
        filteredTransactions.clear();
        
        DataManager dm = DataManager.getInstance(this);
        String myAccountNumber = dm.getAccountNumber();
        
        for (TransactionDTO tx : allTransactions) {
            // Filter by tab (ALL/IN/OUT)
            boolean matchesTab = false;
                switch (currentFilter) {
                case "ALL":
                    matchesTab = true;
                        break;
                case "IN":
                    // Tiền vào: receiverAccountNumber == myAccountNumber
                    matchesTab = myAccountNumber != null && 
                                 myAccountNumber.equals(tx.getReceiverAccountNumber());
                        break;
                case "OUT":
                    // Tiền ra: senderAccountNumber == myAccountNumber
                    matchesTab = myAccountNumber != null && 
                                 myAccountNumber.equals(tx.getSenderAccountNumber());
                        break;
                }
                
            if (!matchesTab) continue;
            
            // Filter by search query
            if (!searchQuery.isEmpty()) {
                String description = tx.getDescription() != null ? tx.getDescription().toLowerCase() : "";
                String code = tx.getCode() != null ? tx.getCode().toLowerCase() : "";
                String receiverName = tx.getReceiverAccountName() != null ? 
                                     tx.getReceiverAccountName().toLowerCase() : "";
                String senderName = tx.getSenderAccountName() != null ? 
                                   tx.getSenderAccountName().toLowerCase() : "";
                
                if (!description.contains(searchQuery) && 
                    !code.contains(searchQuery) &&
                    !receiverName.contains(searchQuery) &&
                    !senderName.contains(searchQuery)) {
                    continue;
                }
            }
            
            filteredTransactions.add(tx);
        }
        
        adapter.notifyDataSetChanged();
        
        // Show/hide empty state
        if (filteredTransactions.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvTransactions.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvTransactions.setVisibility(View.VISIBLE);
    }
}

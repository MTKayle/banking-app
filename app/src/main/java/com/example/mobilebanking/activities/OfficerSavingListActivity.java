package com.example.mobilebanking.activities;

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
import com.example.mobilebanking.adapters.OfficerSavingAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.SavingApiService;
import com.example.mobilebanking.api.dto.SavingAccountResponse;
import com.example.mobilebanking.models.SavingModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OfficerSavingListActivity - Danh sách sổ tiết kiệm cho Officer
 * Features:
 * - RecyclerView hiển thị savings (card view)
 * - 2 Tabs: Đang hoạt động (ACTIVE), Đã đóng (CLOSED)
 * - Search bar theo tên khách hàng
 */
public class OfficerSavingListActivity extends BaseActivity {

    private static final String TAG = "OfficerSavingList";

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView rvSavings;
    private LinearLayout emptyState;
    private ProgressBar progressBar;

    private OfficerSavingAdapter adapter;
    private List<SavingModel> allSavingList = new ArrayList<>();
    private List<SavingModel> filteredByStatusList = new ArrayList<>();

    private String currentStatus = "ACTIVE"; // Default tab
    private String searchQuery = "";

    private SavingApiService savingApiService;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_saving_list);

        // Initialize API service
        savingApiService = ApiClient.getSavingApiService();

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSearch();

        // Load all data
        loadAllSavings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.et_search);
        rvSavings = findViewById(R.id.rv_savings);
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

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Đang hoạt động"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã đóng"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentStatus = "ACTIVE";
                        break;
                    case 1:
                        currentStatus = "CLOSED";
                        break;
                }
                Log.d(TAG, "Tab selected: " + tab.getPosition() + ", status: " + currentStatus);
                filterByStatus();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab reselected: " + tab.getPosition() + ", status: " + currentStatus);
                filterByStatus();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new OfficerSavingAdapter(saving -> {
            // Click vào saving - có thể mở detail nếu cần
            Toast.makeText(this, "Sổ: " + saving.getSavingBookNumber(), Toast.LENGTH_SHORT).show();
        });

        rvSavings.setLayoutManager(new LinearLayoutManager(this));
        rvSavings.setAdapter(adapter);
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

    /**
     * Load all savings from API
     */
    private void loadAllSavings() {
        if (isLoading) {
            Log.d(TAG, "Already loading, skip...");
            return;
        }

        isLoading = true;
        showLoading(true);

        allSavingList.clear();
        filteredByStatusList.clear();
        adapter.setSavingList(new ArrayList<>());

        Log.d(TAG, "Loading all savings...");

        Call<List<SavingAccountResponse>> call = savingApiService.getAllSavingAccounts();

        call.enqueue(new Callback<List<SavingAccountResponse>>() {
            @Override
            public void onResponse(Call<List<SavingAccountResponse>> call,
                                   Response<List<SavingAccountResponse>> response) {
                isLoading = false;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Loaded " + response.body().size() + " savings");
                    for (SavingAccountResponse resp : response.body()) {
                        allSavingList.add(convertToModel(resp));
                    }
                    filterByStatus();
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    Toast.makeText(OfficerSavingListActivity.this,
                            "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<SavingAccountResponse>> call, Throwable t) {
                isLoading = false;
                showLoading(false);
                Log.e(TAG, "API Failed: " + t.getMessage(), t);
                Toast.makeText(OfficerSavingListActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private SavingModel convertToModel(SavingAccountResponse resp) {
        return new SavingModel(
                resp.getSavingId(),
                resp.getSavingBookNumber(),
                resp.getAccountNumber(),
                resp.getBalance(),
                resp.getTerm(),
                resp.getTermMonths(),
                resp.getInterestRate(),
                resp.getOpenedDate(),
                resp.getMaturityDate(),
                resp.getStatus(),
                resp.getUserId(),
                resp.getUserFullName()
        );
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        rvSavings.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        List<SavingModel> currentList = adapter.getItemCount() > 0 ? 
                filteredByStatusList : new ArrayList<>();
        
        if (currentList.isEmpty()) {
            rvSavings.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvSavings.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    /**
     * Filter by status (ACTIVE/CLOSED)
     */
    private void filterByStatus() {
        filteredByStatusList.clear();
        for (SavingModel saving : allSavingList) {
            if (currentStatus.equals(saving.getStatus())) {
                filteredByStatusList.add(saving);
            }
        }
        filterBySearch();
    }

    /**
     * Filter by search query (customer name)
     */
    private void filterBySearch() {
        if (searchQuery.isEmpty()) {
            adapter.setSavingList(filteredByStatusList);
        } else {
            List<SavingModel> filtered = new ArrayList<>();
            for (SavingModel saving : filteredByStatusList) {
                String customerName = saving.getUserFullName() != null ?
                        saving.getUserFullName().toLowerCase(Locale.getDefault()) : "";

                if (customerName.contains(searchQuery)) {
                    filtered.add(saving);
                }
            }
            adapter.setSavingList(filtered);
        }

        updateEmptyState();
    }

    // Không cần override onResume để tránh duplicate data
    // Data đã được load trong onCreate
}

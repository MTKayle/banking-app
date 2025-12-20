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
import com.example.mobilebanking.adapters.OfficerUserAdapter;
import com.example.mobilebanking.models.UserModel;
import com.example.mobilebanking.utils.OfficerMockData;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OfficerUserListActivity - Danh sách người dùng cho Officer
 * Features:
 * - RecyclerView hiển thị users
 * - Search bar
 * - 3 Tabs: Tất cả, Khách hàng, Nhân viên
 * - Click vào user → OfficerUserDetailActivity
 */
public class OfficerUserListActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView rvUsers;
    private LinearLayout emptyState;
    
    private OfficerUserAdapter adapter;
    private List<UserModel> allUsers = new ArrayList<>();
    private List<UserModel> filteredUsers = new ArrayList<>();
    
    private String currentFilter = "ALL"; // ALL, CUSTOMER, OFFICER
    private String searchQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_user_list);
        
        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.et_search);
        rvUsers = findViewById(R.id.rv_users);
        emptyState = findViewById(R.id.empty_state);
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
        tabLayout.addTab(tabLayout.newTab().setText("Khách hàng"));
        tabLayout.addTab(tabLayout.newTab().setText("Nhân viên"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "ALL";
                        break;
                    case 1:
                        currentFilter = "CUSTOMER";
                        break;
                    case 2:
                        currentFilter = "OFFICER";
                        break;
                }
                filterUsers();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupRecyclerView() {
        adapter = new OfficerUserAdapter(user -> {
            // Click vào user → mở detail
            Intent intent = new Intent(OfficerUserListActivity.this, OfficerUserDetailActivity.class);
            intent.putExtra("user_id", user.getUserId());
            intent.putExtra("user_name", user.getFullName());
            intent.putExtra("user_phone", user.getPhone());
            intent.putExtra("user_email", user.getEmail());
            intent.putExtra("user_role", user.getRole());
            intent.putExtra("user_locked", user.isLocked());
            intent.putExtra("user_cccd", user.getCccdNumber());
            intent.putExtra("user_dob", user.getDateOfBirth());
            startActivity(intent);
        });
        
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase(Locale.getDefault());
                filterUsers();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadUsers() {
        // Load mock data
        allUsers = OfficerMockData.getInstance().getMockUsers();
        filterUsers();
    }
    
    private void filterUsers() {
        filteredUsers.clear();
        
        for (UserModel user : allUsers) {
            // Filter by tab
            boolean matchesTab = false;
            if (currentFilter.equals("ALL")) {
                matchesTab = true;
            } else if (currentFilter.equals("CUSTOMER") && "customer".equalsIgnoreCase(user.getRole())) {
                matchesTab = true;
            } else if (currentFilter.equals("OFFICER") && "officer".equalsIgnoreCase(user.getRole())) {
                matchesTab = true;
            }
            
            if (!matchesTab) continue;
            
            // Filter by search query
            if (!searchQuery.isEmpty()) {
                String fullName = user.getFullName().toLowerCase(Locale.getDefault());
                String phone = user.getPhone().toLowerCase(Locale.getDefault());
                String email = user.getEmail().toLowerCase(Locale.getDefault());
                
                if (!fullName.contains(searchQuery) && 
                    !phone.contains(searchQuery) && 
                    !email.contains(searchQuery)) {
                    continue;
                }
            }
            
            filteredUsers.add(user);
        }
        
        // Update UI
        adapter.setUserList(filteredUsers);
        
        if (filteredUsers.isEmpty()) {
            rvUsers.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvUsers.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại (có thể đã update user)
        loadUsers();
    }
}


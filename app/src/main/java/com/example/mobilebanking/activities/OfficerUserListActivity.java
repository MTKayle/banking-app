package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.OfficerUserAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.UserApiService;
import com.example.mobilebanking.api.dto.UserResponse;
import com.example.mobilebanking.models.UserModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OfficerUserListActivity - Danh sách khách hàng cho Officer
 * Features:
 * - RecyclerView hiển thị khách hàng (chỉ CUSTOMER)
 * - Search bar
 * - Click vào user → OfficerUserDetailActivity
 * - Gọi API thật từ backend: GET /api/users
 */
public class OfficerUserListActivity extends BaseActivity {
    
    private static final String TAG = "OfficerUserListActivity";
    
    private MaterialToolbar toolbar;
    private EditText etSearch;
    private RecyclerView rvUsers;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private Button btnSearchApi;
    
    private OfficerUserAdapter adapter;
    private List<UserModel> allCustomers = new ArrayList<>();
    private List<UserModel> filteredCustomers = new ArrayList<>();
    
    private String searchQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_user_list);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadCustomersFromApi();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        rvUsers = findViewById(R.id.rv_users);
        emptyState = findViewById(R.id.empty_state);
        progressBar = findViewById(R.id.progress_bar);
        btnSearchApi = findViewById(R.id.btn_search_api);
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
                filterCustomers();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Add search button for API search
        if (btnSearchApi != null) {
            btnSearchApi.setOnClickListener(v -> {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchUserByApi(query);
                }
            });
        }
    }
    
    /**
     * Search user by phone or CCCD via API
     * Tries phone first, then CCCD if phone fails
     */
    private void searchUserByApi(String query) {
        showLoading(true);
        
        // Check if query looks like phone number (starts with 0 and has 10-11 digits)
        if (query.matches("^0\\d{9,10}$")) {
            searchByPhone(query);
        } else if (query.matches("^\\d{12}$")) {
            // CCCD has 12 digits
            searchByCccd(query);
        } else {
            // Try phone first
            searchByPhone(query);
        }
    }
    
    /**
     * Search user by phone number
     * Endpoint: GET /users/by-phone/{phone}
     */
    private void searchByPhone(String phone) {
        UserApiService userApiService = ApiClient.getUserApiService();
        Call<UserResponse> call = userApiService.getUserByPhone(phone);
        
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showLoading(false);
                    UserResponse userResponse = response.body();
                    
                    // Only show if CUSTOMER
                    if ("customer".equalsIgnoreCase(userResponse.getRole())) {
                        displaySingleUser(userResponse);
                    } else {
                        Toast.makeText(OfficerUserListActivity.this, 
                            "Không tìm thấy khách hàng với SĐT này", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    // Try CCCD search if phone fails
                    searchByCccd(phone);
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Try CCCD search if phone fails
                searchByCccd(phone);
            }
        });
    }
    
    /**
     * Search user by CCCD number
     * Endpoint: GET /users/by-cccd/{cccdNumber}
     */
    private void searchByCccd(String cccd) {
        UserApiService userApiService = ApiClient.getUserApiService();
        Call<UserResponse> call = userApiService.getUserByCccd(cccd);
        
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    
                    // Only show if CUSTOMER
                    if ("customer".equalsIgnoreCase(userResponse.getRole())) {
                        displaySingleUser(userResponse);
                    } else {
                        Toast.makeText(OfficerUserListActivity.this, 
                            "Không tìm thấy khách hàng với CCCD này", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    Toast.makeText(OfficerUserListActivity.this, 
                        "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Search by CCCD failed: " + t.getMessage(), t);
                Toast.makeText(OfficerUserListActivity.this, 
                    "Không tìm thấy khách hàng", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    /**
     * Display single user from API search result
     */
    private void displaySingleUser(UserResponse userResponse) {
        filteredCustomers.clear();
        
        UserModel userModel = new UserModel(
            userResponse.getUserId(),
            userResponse.getPhone() != null ? userResponse.getPhone() : "",
            userResponse.getFullName() != null ? userResponse.getFullName() : "",
            userResponse.getEmail() != null ? userResponse.getEmail() : "",
            userResponse.getRole() != null ? userResponse.getRole().toLowerCase() : "",
            userResponse.getIsLocked() != null ? userResponse.getIsLocked() : false,
            userResponse.getCreatedAt() != null ? userResponse.getCreatedAt() : "",
            userResponse.getCccdNumber() != null ? userResponse.getCccdNumber() : "",
            userResponse.getDateOfBirth() != null ? userResponse.getDateOfBirth() : ""
        );
        
        filteredCustomers.add(userModel);
        adapter.setUserList(filteredCustomers);
        
        rvUsers.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        
        Toast.makeText(this, "Tìm thấy 1 khách hàng", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Gọi API lấy danh sách users từ backend
     * Endpoint: GET /api/users
     * Chỉ lọc và hiển thị CUSTOMER
     */
    private void loadCustomersFromApi() {
        showLoading(true);
        
        UserApiService userApiService = ApiClient.getUserApiService();
        Call<List<UserResponse>> call = userApiService.getAllUsers();
        
        call.enqueue(new Callback<List<UserResponse>>() {
            @Override
            public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<UserResponse> userResponses = response.body();
                    allCustomers.clear();
                    
                    // Convert UserResponse to UserModel - CHỈ LẤY CUSTOMER
                    for (UserResponse userResponse : userResponses) {
                        String role = userResponse.getRole() != null ? userResponse.getRole().toLowerCase() : "";
                        
                        // Chỉ thêm nếu là CUSTOMER
                        if ("customer".equalsIgnoreCase(role)) {
                            UserModel userModel = new UserModel(
                                userResponse.getUserId(),
                                userResponse.getPhone() != null ? userResponse.getPhone() : "",
                                userResponse.getFullName() != null ? userResponse.getFullName() : "",
                                userResponse.getEmail() != null ? userResponse.getEmail() : "",
                                role,
                                userResponse.getIsLocked() != null ? userResponse.getIsLocked() : false,
                                userResponse.getCreatedAt() != null ? userResponse.getCreatedAt() : "",
                                userResponse.getCccdNumber() != null ? userResponse.getCccdNumber() : "",
                                userResponse.getDateOfBirth() != null ? userResponse.getDateOfBirth() : ""
                            );
                            allCustomers.add(userModel);
                        }
                    }
                    
                    Log.d(TAG, "Loaded " + allCustomers.size() + " customers from API");
                    filterCustomers();
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    Toast.makeText(OfficerUserListActivity.this, 
                        "Lỗi tải danh sách: " + response.message(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                Toast.makeText(OfficerUserListActivity.this, 
                    "Không thể kết nối server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void filterCustomers() {
        filteredCustomers.clear();
        
        for (UserModel user : allCustomers) {
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
            
            filteredCustomers.add(user);
        }
        
        // Update UI
        adapter.setUserList(filteredCustomers);
        
        if (filteredCustomers.isEmpty()) {
            showEmptyState();
        } else {
            rvUsers.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (show) {
            rvUsers.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }
    
    private void showEmptyState() {
        rvUsers.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại (có thể đã update user)
        loadCustomersFromApi();
    }
}


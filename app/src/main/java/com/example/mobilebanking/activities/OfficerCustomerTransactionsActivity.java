package com.example.mobilebanking.activities;

import android.os.Bundle;
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
import com.example.mobilebanking.adapters.TransactionAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.TransactionApiService;
import com.example.mobilebanking.api.UserApiService;
import com.example.mobilebanking.api.dto.TransactionDTO;
import com.example.mobilebanking.api.dto.TransactionResponse;
import com.example.mobilebanking.api.dto.UserResponse;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficerCustomerTransactionsActivity extends BaseActivity {
    
    private static final String TAG = "OfficerCustTransactions";
    
    private MaterialToolbar toolbar;
    private EditText etCustomerPhone;
    private Button btnSearch;
    private RecyclerView rvTransactions;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    
    private TransactionAdapter adapter;
    private List<TransactionDTO> transactions = new ArrayList<>();
    
    private TransactionApiService transactionApiService;
    private UserApiService userApiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_customer_transactions);
        
        transactionApiService = ApiClient.getTransactionApiService();
        userApiService = ApiClient.getUserApiService();
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etCustomerPhone = findViewById(R.id.et_customer_phone);
        btnSearch = findViewById(R.id.btn_search);
        rvTransactions = findViewById(R.id.rv_transactions);
        emptyState = findViewById(R.id.empty_state);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, transactions, transaction -> {
            Toast.makeText(this, "Chi tiết: " + transaction.getTransactionId(), Toast.LENGTH_SHORT).show();
        });
        
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }
    
    private void setupButton() {
        btnSearch.setOnClickListener(v -> {
            String phone = etCustomerPhone.getText().toString().trim();
            
            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // First, get userId by phone, then get transactions
            searchUserAndLoadTransactions(phone);
        });
    }
    
    /**
     * Search user by phone and load their transactions
     */
    private void searchUserAndLoadTransactions(String phone) {
        showLoading(true);
        
        Call<UserResponse> call = userApiService.getUserByPhone(phone);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    Long userId = user.getUserId();
                    
                    if (userId != null && userId > 0) {
                        loadTransactionsByUserId(userId);
                    } else {
                        showLoading(false);
                        Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                            "Không tìm thấy user ID", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                } else {
                    showLoading(false);
                    Log.e(TAG, "Search user error: " + response.code());
                    Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                        "Không tìm thấy khách hàng với SĐT này", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Search user failed: " + t.getMessage(), t);
                Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    /**
     * Load transactions by user ID
     * Endpoint: GET /transactions/user/{userId}
     */
    private void loadTransactionsByUserId(Long userId) {
        Call<TransactionResponse> call = transactionApiService.getTransactionsByUser(userId);
        call.enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    TransactionResponse transactionResponse = response.body();
                    
                    if (transactionResponse.isSuccess() && transactionResponse.getData() != null) {
                        transactions.clear();
                        transactions.addAll(transactionResponse.getData());
                        adapter.notifyDataSetChanged();
                        
                        if (transactions.isEmpty()) {
                            showEmptyState();
                            Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                                "Không có giao dịch nào", Toast.LENGTH_SHORT).show();
                        } else {
                            rvTransactions.setVisibility(View.VISIBLE);
                            emptyState.setVisibility(View.GONE);
                            Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                                "Tìm thấy " + transactions.size() + " giao dịch", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showEmptyState();
                        Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                            transactionResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Load transactions error: " + response.code());
                    Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                        "Lỗi tải lịch sử giao dịch", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Load transactions failed: " + t.getMessage(), t);
                Toast.makeText(OfficerCustomerTransactionsActivity.this, 
                    "Không thể kết nối server", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnSearch.setEnabled(!show);
    }
    
    private void showEmptyState() {
        rvTransactions.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}


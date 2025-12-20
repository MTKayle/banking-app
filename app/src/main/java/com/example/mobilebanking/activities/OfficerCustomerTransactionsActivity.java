package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.TransactionAdapter;
import com.example.mobilebanking.api.dto.TransactionDTO;
import com.example.mobilebanking.utils.OfficerMockData;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class OfficerCustomerTransactionsActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private EditText etCustomerPhone;
    private Button btnSearch;
    private RecyclerView rvTransactions;
    private LinearLayout emptyState;
    
    private TransactionAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_customer_transactions);
        
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
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, new ArrayList<>(), transaction -> {
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
            
            // Load mock transactions - tạo adapter mới với data
            List<TransactionDTO> transactions = OfficerMockData.getInstance().getMockCustomerTransactions(phone);
            adapter = new TransactionAdapter(this, transactions, transaction -> {
                Toast.makeText(this, "Chi tiết: " + transaction.getTransactionId(), Toast.LENGTH_SHORT).show();
            });
            rvTransactions.setAdapter(adapter);
            
            if (adapter.getItemCount() > 0) {
                rvTransactions.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                Toast.makeText(this, "Tìm thấy " + adapter.getItemCount() + " giao dịch", Toast.LENGTH_SHORT).show();
            } else {
                rvTransactions.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}


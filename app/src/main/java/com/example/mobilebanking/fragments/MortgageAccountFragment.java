package com.example.mobilebanking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.MortgageDetailActivity;
import com.example.mobilebanking.activities.ServicesActivity;
import com.example.mobilebanking.adapters.MortgageAccountAdapter;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mortgage Account Fragment
 * Tab hiển thị danh sách khoản vay với filter theo status
 */
public class MortgageAccountFragment extends Fragment {
    
    private RecyclerView rvMortgageAccounts;
    private MortgageAccountAdapter adapter;
    private List<MortgageAccountDTO> allMortgageAccounts = new ArrayList<>();
    private List<MortgageAccountDTO> filteredMortgageAccounts = new ArrayList<>();
    private LinearLayout emptyState;
    private TabLayout tabLayout;
    private EditText etSearch;
    
    private String currentStatus = "ALL"; // ALL, PENDING_APPRAISAL, ACTIVE, REJECTED, COMPLETED
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mortgage_account, container, false);
        
        initViews(view);
        setupTabLayout();
        setupRecyclerView();
        setupSearchListener();
        fetchMortgageAccounts();
        
        return view;
    }
    
    private void initViews(View view) {
        rvMortgageAccounts = view.findViewById(R.id.rv_mortgage_accounts);
        emptyState = view.findViewById(R.id.empty_state);
        tabLayout = view.findViewById(R.id.tab_layout);
        etSearch = view.findViewById(R.id.et_search);
    }
    
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Chờ duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang vay"));
        tabLayout.addTab(tabLayout.newTab().setText("Từ chối"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentStatus = "ALL";
                        break;
                    case 1:
                        currentStatus = "PENDING_APPRAISAL";
                        break;
                    case 2:
                        currentStatus = "ACTIVE";
                        break;
                    case 3:
                        currentStatus = "REJECTED";
                        break;
                    case 4:
                        currentStatus = "COMPLETED";
                        break;
                }
                filterMortgageAccounts();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupRecyclerView() {
        adapter = new MortgageAccountAdapter(requireContext(), filteredMortgageAccounts);
        adapter.setOnItemClickListener(mortgage -> {
            // Navigate to detail screen
            Intent intent = new Intent(requireContext(), MortgageDetailActivity.class);
            intent.putExtra("MORTGAGE_ID", mortgage.getMortgageId());
            startActivity(intent);
        });
        rvMortgageAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMortgageAccounts.setAdapter(adapter);
    }
    
    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMortgageAccounts();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void fetchMortgageAccounts() {
        DataManager dm = DataManager.getInstance(requireContext());
        Long userId = dm.getUserId();
        
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call API: http://localhost:8089/api/mortgage/user/{userId}
        AccountApiService service = ApiClient.getAccountApiService();
        Call<List<MortgageAccountDTO>> call = service.getMortgagesByUserId(userId);
        
        call.enqueue(new Callback<List<MortgageAccountDTO>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountDTO>> call, Response<List<MortgageAccountDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allMortgageAccounts.clear();
                    allMortgageAccounts.addAll(response.body());
                    filterMortgageAccounts();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách khoản vay", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void filterMortgageAccounts() {
        filteredMortgageAccounts.clear();
        
        String searchQuery = etSearch.getText().toString().toLowerCase().trim();
        
        for (MortgageAccountDTO account : allMortgageAccounts) {
            // Filter by status
            boolean matchStatus = currentStatus.equals("ALL") || currentStatus.equals(account.getStatus());
            
            // Filter by search query
            boolean matchSearch = searchQuery.isEmpty() || 
                    (account.getAccountNumber() != null && account.getAccountNumber().toLowerCase().contains(searchQuery)) ||
                    (account.getCustomerName() != null && account.getCustomerName().toLowerCase().contains(searchQuery)) ||
                    (account.getCustomerPhone() != null && account.getCustomerPhone().contains(searchQuery));
            
            if (matchStatus && matchSearch) {
                filteredMortgageAccounts.add(account);
            }
        }
        
        adapter.updateList(filteredMortgageAccounts);
        
        // Show/hide empty state
        if (filteredMortgageAccounts.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }
    
    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        rvMortgageAccounts.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        emptyState.setVisibility(View.GONE);
        rvMortgageAccounts.setVisibility(View.VISIBLE);
    }
}


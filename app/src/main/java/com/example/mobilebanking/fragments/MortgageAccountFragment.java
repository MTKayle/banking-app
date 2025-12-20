package com.example.mobilebanking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.ServicesActivity;
import com.example.mobilebanking.adapters.MortgageAccountAdapter;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;
import com.example.mobilebanking.utils.DataManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Mortgage Account Fragment
 * Tab hiển thị danh sách khoản vay
 */
public class MortgageAccountFragment extends Fragment {
    
    private RecyclerView rvMortgageAccounts;
    private MortgageAccountAdapter adapter;
    private List<MortgageAccountDTO> mortgageAccounts = new ArrayList<>();
    private LinearLayout emptyState;
    private Button btnCreateMortgage;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mortgage_account, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        fetchMortgageAccounts();
        
        return view;
    }
    
    private void initViews(View view) {
        rvMortgageAccounts = view.findViewById(R.id.rv_mortgage_accounts);
        emptyState = view.findViewById(R.id.empty_state);
        btnCreateMortgage = view.findViewById(R.id.btn_create_mortgage);
    }
    
    private void setupRecyclerView() {
        adapter = new MortgageAccountAdapter(requireContext(), mortgageAccounts);
        rvMortgageAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMortgageAccounts.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        btnCreateMortgage.setOnClickListener(v -> {
            // Navigate to create mortgage account screen
            Intent intent = new Intent(requireContext(), ServicesActivity.class);
            startActivity(intent);
            Toast.makeText(requireContext(), "Vui lòng liên hệ chi nhánh để đăng ký vay", Toast.LENGTH_LONG).show();
        });
    }
    
    private void fetchMortgageAccounts() {
        DataManager dm = DataManager.getInstance(requireContext());
        Long userId = dm.getUserId();
        
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AccountApiService service = ApiClient.getAccountApiService();
        service.getMortgageAccounts(userId).enqueue(new Callback<List<MortgageAccountDTO>>() {
            @Override
            public void onResponse(Call<List<MortgageAccountDTO>> call, Response<List<MortgageAccountDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mortgageAccounts.clear();
                    mortgageAccounts.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    // Show/hide empty state
                    if (mortgageAccounts.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        rvMortgageAccounts.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        rvMortgageAccounts.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách khoản vay", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<MortgageAccountDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyState.setVisibility(View.VISIBLE);
                rvMortgageAccounts.setVisibility(View.GONE);
            }
        });
    }
}


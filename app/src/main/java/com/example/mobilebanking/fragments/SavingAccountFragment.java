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
import com.example.mobilebanking.activities.SavingTermListActivity;
import com.example.mobilebanking.adapters.SavingAccountAdapter;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.SavingAccountDTO;
import com.example.mobilebanking.utils.DataManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Saving Account Fragment
 * Tab hiển thị danh sách sổ tiết kiệm
 */
public class SavingAccountFragment extends Fragment {
    
    private RecyclerView rvSavingAccounts;
    private SavingAccountAdapter adapter;
    private List<SavingAccountDTO> savingAccounts = new ArrayList<>();
    private LinearLayout emptyState;
    private Button btnCreateSaving;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saving_account, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        fetchSavingAccounts();
        
        return view;
    }
    
    private void initViews(View view) {
        rvSavingAccounts = view.findViewById(R.id.rv_saving_accounts);
        emptyState = view.findViewById(R.id.empty_state);
        btnCreateSaving = view.findViewById(R.id.btn_create_saving);
    }
    
    private void setupRecyclerView() {
        adapter = new SavingAccountAdapter(requireContext(), savingAccounts);
        rvSavingAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSavingAccounts.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        btnCreateSaving.setOnClickListener(v -> {
            // Navigate to create saving account screen
            Intent intent = new Intent(requireContext(), SavingTermListActivity.class);
            startActivity(intent);
        });
    }
    
    private void fetchSavingAccounts() {
        DataManager dm = DataManager.getInstance(requireContext());
        Long userId = dm.getUserId();
        
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AccountApiService service = ApiClient.getAccountApiService();
        service.getSavingAccounts(userId).enqueue(new Callback<List<SavingAccountDTO>>() {
            @Override
            public void onResponse(Call<List<SavingAccountDTO>> call, Response<List<SavingAccountDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savingAccounts.clear();
                    savingAccounts.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    // Show/hide empty state
                    if (savingAccounts.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        rvSavingAccounts.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        rvSavingAccounts.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách tiết kiệm", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<SavingAccountDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyState.setVisibility(View.VISIBLE);
                rvSavingAccounts.setVisibility(View.GONE);
            }
        });
    }
}


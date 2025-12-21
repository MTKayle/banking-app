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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.SavingTermListActivity;
import com.example.mobilebanking.adapters.MySavingAccountAdapter;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.MySavingAccountDTO;

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
    private MySavingAccountAdapter adapter;
    private List<MySavingAccountDTO> savingAccounts = new ArrayList<>();
    private LinearLayout emptyState;
    private CardView bannerCreateSaving;
    private Button btnCreateSavingBanner;
    private Button btnCreateSavingBottom;
    
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
        bannerCreateSaving = view.findViewById(R.id.banner_create_saving);
        btnCreateSavingBanner = view.findViewById(R.id.btn_create_saving_banner);
        btnCreateSavingBottom = view.findViewById(R.id.btn_create_saving_bottom);
    }
    
    private void setupRecyclerView() {
        adapter = new MySavingAccountAdapter(requireContext(), savingAccounts);
        rvSavingAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSavingAccounts.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        // Banner button
        btnCreateSavingBanner.setOnClickListener(v -> navigateToCreateSaving());
        
        // Bottom button
        btnCreateSavingBottom.setOnClickListener(v -> navigateToCreateSaving());
    }
    
    private void navigateToCreateSaving() {
        Intent intent = new Intent(requireContext(), SavingTermListActivity.class);
        startActivity(intent);
    }
    
    private void fetchSavingAccounts() {
        AccountApiService service = ApiClient.getAccountApiService();
        service.getMySavingAccounts().enqueue(new Callback<List<MySavingAccountDTO>>() {
            @Override
            public void onResponse(Call<List<MySavingAccountDTO>> call, Response<List<MySavingAccountDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savingAccounts.clear();
                    savingAccounts.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    // Show/hide UI elements based on account list
                    if (savingAccounts.isEmpty()) {
                        // Không có tài khoản: Hiển thị banner và empty state
                        bannerCreateSaving.setVisibility(View.VISIBLE);
                        emptyState.setVisibility(View.VISIBLE);
                        rvSavingAccounts.setVisibility(View.GONE);
                        btnCreateSavingBottom.setVisibility(View.GONE);
                    } else {
                        // Có tài khoản: Ẩn banner, hiển thị danh sách và nút dưới
                        bannerCreateSaving.setVisibility(View.GONE);
                        emptyState.setVisibility(View.GONE);
                        rvSavingAccounts.setVisibility(View.VISIBLE);
                        btnCreateSavingBottom.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách tiết kiệm", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Call<List<MySavingAccountDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void showEmptyState() {
        bannerCreateSaving.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.VISIBLE);
        rvSavingAccounts.setVisibility(View.GONE);
        btnCreateSavingBottom.setVisibility(View.GONE);
    }
}


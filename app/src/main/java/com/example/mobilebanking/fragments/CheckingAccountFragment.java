package com.example.mobilebanking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.AccountDetailActivity;
import com.example.mobilebanking.activities.MyQRActivity;
import com.example.mobilebanking.activities.TransactionHistoryActivity;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Checking Account Fragment
 * Tab hiển thị tài khoản thanh toán theo style BIDV
 */
public class CheckingAccountFragment extends Fragment {
    
    private TextView tvAccountNumber, tvBalance;
    private LinearLayout btnMyQR, btnHistory, btnDetail;
    private CheckingAccountInfoResponse accountInfo;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checking_account, container, false);
        
        initViews(view);
        setupClickListeners();
        fetchAccountInfo();
        
        return view;
    }
    
    private void initViews(View view) {
        tvAccountNumber = view.findViewById(R.id.tv_account_number);
        tvBalance = view.findViewById(R.id.tv_balance);
        btnMyQR = view.findViewById(R.id.btn_my_qr);
        btnHistory = view.findViewById(R.id.btn_history);
        btnDetail = view.findViewById(R.id.btn_detail);
    }
    
    private void fetchAccountInfo() {
        DataManager dm = DataManager.getInstance(requireContext());
        Long userId = dm.getUserId();
        
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AccountApiService service = ApiClient.getAccountApiService();
        service.getCheckingAccountInfo(userId).enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    accountInfo = response.body();
                    updateUI();
                    
                    // Lưu account number vào DataManager để dùng cho các screen khác
                    DataManager.getInstance(requireContext()).saveCheckingAccountInfo(
                            accountInfo.getCheckingId(), 
                            accountInfo.getAccountNumber()
                    );
                } else {
                    Toast.makeText(requireContext(), "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI() {
        if (accountInfo != null) {
            tvAccountNumber.setText(accountInfo.getAccountNumber());
            if (accountInfo.getBalance() != null) {
                tvBalance.setText(formatCurrency(accountInfo.getBalance().doubleValue()));
            }
        }
    }
    
    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
    
    private void setupClickListeners() {
        btnMyQR.setOnClickListener(v -> {
            if (accountInfo != null) {
                Intent intent = new Intent(requireContext(), MyQRActivity.class);
                intent.putExtra("accountNumber", accountInfo.getAccountNumber());
                // Lấy tên từ DataManager thay vì từ accountInfo
                DataManager dm = DataManager.getInstance(requireContext());
                intent.putExtra("accountHolderName", dm.getUserFullName());
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Vui lòng đợi tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), TransactionHistoryActivity.class));
        });
        
        btnDetail.setOnClickListener(v -> {
            if (accountInfo != null) {
                Intent intent = new Intent(requireContext(), AccountDetailActivity.class);
                intent.putExtra("accountNumber", accountInfo.getAccountNumber());
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Vui lòng đợi tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


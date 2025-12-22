package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.SavingTermAdapter;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.SavingTermDTO;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SavingTermListActivity extends AppCompatActivity 
        implements SavingTermAdapter.OnTermDTOClickListener {

    private RecyclerView rvTerms;
    private SavingTermAdapter adapter;
    private List<SavingTermDTO> termList = new ArrayList<>();
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_term_list);

        dataManager = DataManager.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        loadTerms();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvTerms = findViewById(R.id.rv_terms);
        rvTerms.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavingTermAdapter(termList, this);
        rvTerms.setAdapter(adapter);
    }

    private void loadTerms() {
        AccountApiService apiService = ApiClient.getAccountApiService();
        
        apiService.getSavingTerms().enqueue(new Callback<List<SavingTermDTO>>() {
            @Override
            public void onResponse(Call<List<SavingTermDTO>> call, Response<List<SavingTermDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    termList.clear();
                    // Lọc bỏ NON_TERM và sắp xếp theo số tháng
                    for (SavingTermDTO term : response.body()) {
                        if (!"NON_TERM".equals(term.getTermType())) {
                            termList.add(term);
                        }
                    }
                    // Sắp xếp theo số tháng tăng dần
                    Collections.sort(termList, (t1, t2) -> 
                            Integer.compare(t1.getTermMonths(), t2.getTermMonths()));
                    adapter.updateData(termList);
                } else {
                    Toast.makeText(SavingTermListActivity.this, 
                            "Không thể tải danh sách kỳ hạn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SavingTermDTO>> call, Throwable t) {
                Toast.makeText(SavingTermListActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTermClick(SavingTermDTO term) {
        Intent intent = new Intent(this, SavingDepositActivity.class);
        intent.putExtra("termType", term.getTermType());
        intent.putExtra("termMonths", term.getTermMonths());
        intent.putExtra("interestRate", term.getInterestRate());
        startActivity(intent);
    }
}


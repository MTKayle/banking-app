package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.SavingTermAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

public class OfficerInterestRateActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private RecyclerView rvTerms;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_interest_rate);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTerms = findViewById(R.id.rv_terms);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupRecyclerView() {
        // Mock data: termMonths, currentRate
        List<TermItem> terms = Arrays.asList(
            new TermItem(1, 2.5),
            new TermItem(3, 3.5),
            new TermItem(6, 4.5),
            new TermItem(12, 5.5),
            new TermItem(24, 6.5)
        );
        
        // TODO: Use actual SavingTermAdapter or create custom adapter
        // For now, simple display
        rvTerms.setLayoutManager(new LinearLayoutManager(this));
        Toast.makeText(this, "Hiển thị " + terms.size() + " kỳ hạn", Toast.LENGTH_SHORT).show();
    }
    
    static class TermItem {
        int months;
        double rate;
        
        TermItem(int months, double rate) {
            this.months = months;
            this.rate = rate;
        }
    }
}


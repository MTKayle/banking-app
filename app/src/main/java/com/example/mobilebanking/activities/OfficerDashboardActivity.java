package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.DataManager;

/**
 * Officer Dashboard Activity - Main screen for bank officers
 */
public class OfficerDashboardActivity extends BaseActivity {
    private TextView tvWelcome;
    private CardView cvCustomerManagement, cvAccountManagement, cvTransactionMonitoring, cvReports;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_dashboard);

        dataManager = DataManager.getInstance(this);

        setupToolbar();
        initializeViews();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Officer Dashboard");
        }
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        cvCustomerManagement = findViewById(R.id.cv_customer_management);
        cvAccountManagement = findViewById(R.id.cv_account_management);
        cvTransactionMonitoring = findViewById(R.id.cv_transaction_monitoring);
        cvReports = findViewById(R.id.cv_reports);

        String username = dataManager.getLoggedInUser();
        tvWelcome.setText("Welcome, Officer " + username);
    }

    private void setupListeners() {
        cvCustomerManagement.setOnClickListener(v -> 
            Toast.makeText(this, "Customer Management Feature", Toast.LENGTH_SHORT).show());
        
        cvAccountManagement.setOnClickListener(v -> 
            Toast.makeText(this, "Account Management Feature", Toast.LENGTH_SHORT).show());
        
        cvTransactionMonitoring.setOnClickListener(v -> 
            Toast.makeText(this, "Transaction Monitoring Feature", Toast.LENGTH_SHORT).show());
        
        cvReports.setOnClickListener(v -> 
            Toast.makeText(this, "Reports Feature", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, OfficerSettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            dataManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


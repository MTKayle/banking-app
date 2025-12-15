package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.AccountAdapter;
import com.example.mobilebanking.adapters.QuickActionAdapter;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.models.QuickAction;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Customer Dashboard Activity - Main screen for customers
 */
public class CustomerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvTotalBalance;
    private RecyclerView rvAccounts, rvQuickActions;
    private CardView cvTransfer, cvBillPay, cvMore;
    private DataManager dataManager;
    private AccountAdapter accountAdapter;
    private QuickActionAdapter quickActionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        dataManager = DataManager.getInstance(this);
        
        setupToolbar();
        initializeViews();
        setupRecyclerViews();
        loadData();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trang Chủ");
        }
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        rvAccounts = findViewById(R.id.rv_accounts);
        rvQuickActions = findViewById(R.id.rv_quick_actions);
        cvTransfer = findViewById(R.id.cv_transfer);
        cvBillPay = findViewById(R.id.cv_bill_pay);
        cvMore = findViewById(R.id.cv_more);

        // Set welcome message
        String username = dataManager.getLoggedInUser();
        tvWelcome.setText("Xin chào, " + username);
    }

    private void setupRecyclerViews() {
        // Accounts RecyclerView
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        accountAdapter = new AccountAdapter(new ArrayList<>(), this::onAccountClick);
        rvAccounts.setAdapter(accountAdapter);

        // Quick Actions RecyclerView
        rvQuickActions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quickActionAdapter = new QuickActionAdapter(getQuickActions(), this::onQuickActionClick);
        rvQuickActions.setAdapter(quickActionAdapter);
    }

    private void loadData() {
        // Load user accounts
        List<Account> accounts = dataManager.getMockAccounts("U001");
        accountAdapter.updateAccounts(accounts);

        // Calculate total balance
        double totalBalance = accounts.stream()
                .filter(account -> account.getType() != Account.AccountType.MORTGAGE)
                .mapToDouble(Account::getBalance)
                .sum();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalBalance.setText(formatter.format(totalBalance));
    }

    private List<QuickAction> getQuickActions() {
        List<QuickAction> actions = new ArrayList<>();
        actions.add(new QuickAction("Chuyển Tiền", R.drawable.ic_transfer, "transfer"));
        actions.add(new QuickAction("Thanh Toán", R.drawable.ic_bill, "bill_pay"));
        actions.add(new QuickAction("Nạp Tiền", R.drawable.ic_phone, "top_up"));
        actions.add(new QuickAction("Đặt Vé", R.drawable.ic_ticket, "tickets"));
        actions.add(new QuickAction("Khách Sạn", R.drawable.ic_hotel, "hotels"));
        actions.add(new QuickAction("ATM/Chi Nhánh", R.drawable.ic_location, "locations"));
        return actions;
    }

    private void setupClickListeners() {
        cvTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransferActivity.class);
            startActivity(intent);
        });

        cvBillPay.setOnClickListener(v -> {
            Intent intent = new Intent(this, BillPaymentActivity.class);
            startActivity(intent);
        });

        cvMore.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServicesActivity.class);
            startActivity(intent);
        });
    }

    private void onAccountClick(Account account) {
        Intent intent = new Intent(this, AccountDetailActivity.class);
        intent.putExtra("account_id", account.getAccountId());
        intent.putExtra("account_type", account.getType().name());
        startActivity(intent);
    }

    private void onQuickActionClick(QuickAction action) {
        switch (action.getActionId()) {
            case "transfer":
                startActivity(new Intent(this, TransferActivity.class));
                break;
            case "bill_pay":
                startActivity(new Intent(this, BillPaymentActivity.class));
                break;
            case "top_up":
                startActivity(new Intent(this, MobileTopUpActivity.class));
                break;
            case "tickets":
                startActivity(new Intent(this, TicketBookingActivity.class));
                break;
            case "hotels":
                startActivity(new Intent(this, HotelBookingActivity.class));
                break;
            case "locations":
                startActivity(new Intent(this, BranchLocatorActivity.class));
                break;
        }
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
            startActivity(new Intent(this, ProfileActivity.class));
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

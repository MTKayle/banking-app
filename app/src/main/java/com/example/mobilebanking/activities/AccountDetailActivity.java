package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.TransactionAdapter;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.models.Transaction;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Account Detail Activity - Shows detailed information about a specific account
 */
public class AccountDetailActivity extends AppCompatActivity {
    private TextView tvAccountType, tvAccountNumber, tvBalance, tvInterestRate, tvMonthlyProfit;
    private TextView tvLoanAmount, tvMonthlyPayment, tvRemainingMonths;
    private RecyclerView rvTransactions;
    private DataManager dataManager;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        dataManager = DataManager.getInstance(this);

        setupToolbar();
        initializeViews();
        loadAccountData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Account Details");
        }
    }

    private void initializeViews() {
        tvAccountType = findViewById(R.id.tv_account_type);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvBalance = findViewById(R.id.tv_balance);
        tvInterestRate = findViewById(R.id.tv_interest_rate);
        tvMonthlyProfit = findViewById(R.id.tv_monthly_profit);
        tvLoanAmount = findViewById(R.id.tv_loan_amount);
        tvMonthlyPayment = findViewById(R.id.tv_monthly_payment);
        tvRemainingMonths = findViewById(R.id.tv_remaining_months);
        rvTransactions = findViewById(R.id.rv_transactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAccountData() {
        String accountType = getIntent().getStringExtra("account_type");
        
        // Get mock account data
        List<Account> accounts = dataManager.getMockAccounts("U001");
        Account account = null;
        
        for (Account acc : accounts) {
            if (acc.getType().name().equals(accountType)) {
                account = acc;
                break;
            }
        }

        if (account != null) {
            displayAccountInfo(account);
            loadTransactions(account.getAccountNumber());
        }
    }

    private void displayAccountInfo(Account account) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        tvAccountType.setText(getAccountTypeName(account.getType()));
        tvAccountNumber.setText(account.getAccountNumber());
        tvBalance.setText(formatter.format(Math.abs(account.getBalance())));

        // Show specific fields based on account type
        if (account.getType() == Account.AccountType.SAVINGS) {
            tvInterestRate.setText(account.getInterestRate() + "% per year");
            tvMonthlyProfit.setText(formatter.format(account.getMonthlyProfit()));
        } else if (account.getType() == Account.AccountType.MORTGAGE) {
            tvLoanAmount.setText(formatter.format(account.getLoanAmount()));
            tvMonthlyPayment.setText(formatter.format(account.getMonthlyPayment()));
            tvRemainingMonths.setText(account.getRemainingMonths() + " months");
        }
    }

    private void loadTransactions(String accountNumber) {
        List<Transaction> transactions = dataManager.getMockTransactions(accountNumber);
        transactionAdapter = new TransactionAdapter(transactions);
        rvTransactions.setAdapter(transactionAdapter);
    }

    private String getAccountTypeName(Account.AccountType type) {
        switch (type) {
            case CHECKING:
                return "Checking Account";
            case SAVINGS:
                return "Savings Account";
            case MORTGAGE:
                return "Mortgage Account";
            default:
                return "Account";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.utils.DataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfer Activity - Money transfer functionality
 */
public class TransferActivity extends AppCompatActivity {
    private Spinner spinnerFromAccount, spinnerToBank;
    private EditText etRecipientAccount, etAmount, etNote;
    private Button btnContinue;
    private DataManager dataManager;
    private List<Account> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        dataManager = DataManager.getInstance(this);

        setupToolbar();
        initializeViews();
        loadAccounts();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chuyển Tiền");
        }
    }

    private void initializeViews() {
        spinnerFromAccount = findViewById(R.id.spinner_from_account);
        spinnerToBank = findViewById(R.id.spinner_to_bank);
        etRecipientAccount = findViewById(R.id.et_recipient_account);
        etAmount = findViewById(R.id.et_amount);
        etNote = findViewById(R.id.et_note);
        btnContinue = findViewById(R.id.btn_continue);
    }

    private void loadAccounts() {
        accounts = dataManager.getMockAccounts("U001");
        
        // Filter out mortgage accounts for transfer
        List<String> accountNumbers = new ArrayList<>();
        for (Account account : accounts) {
            if (account.getType() != Account.AccountType.MORTGAGE) {
                accountNumbers.add(account.getAccountNumber() + " - " + account.getType().name());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accountNumbers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromAccount.setAdapter(adapter);

        // Setup bank spinner
        String[] banks = {"Cùng Ngân Hàng", "VietcomBank", "BIDV", "Techcombank", "VietinBank"};
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, banks);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToBank.setAdapter(bankAdapter);
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> handleTransfer());
    }

    private void handleTransfer() {
        String recipientAccount = etRecipientAccount.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        // Validation
        if (recipientAccount.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản người nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to confirmation screen
        Intent intent = new Intent(this, TransactionConfirmationActivity.class);
        intent.putExtra("type", "TRANSFER");
        intent.putExtra("from_account", spinnerFromAccount.getSelectedItem().toString());
        intent.putExtra("to_account", recipientAccount);
        intent.putExtra("amount", amount);
        intent.putExtra("note", note);
        intent.putExtra("bank", spinnerToBank.getSelectedItem().toString());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


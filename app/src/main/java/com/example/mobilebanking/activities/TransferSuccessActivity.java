package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Transfer Success Activity - Display success screen after completing transfer
 */
public class TransferSuccessActivity extends AppCompatActivity {
    private TextView tvSuccessAmount, tvSuccessDateTime, tvRecipientName;
    private TextView tvRecipientAccount, tvTransferNote, tvFromAccountName, tvTransactionCode, tvBankName;
    private ImageView ivHome;
    private MaterialCardView cardTransactionDetails;
    private LinearLayout btnShare, btnSaveImage, btnSaveTemplate;
    private MaterialButton btnContinue;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_success);

        dataManager = DataManager.getInstance(this);

        initializeViews();
        loadTransactionData();
        setupListeners();

        // Check if we need to clear the transaction stack
        boolean clearStack = getIntent().getBooleanExtra("clear_transaction_stack", false);
        if (clearStack) {
            // Send broadcast to finish TransactionConfirmation and Transfer activities
            Intent finishIntent = new Intent("com.example.mobilebanking.FINISH_TRANSACTION_FLOW");
            sendBroadcast(finishIntent);
        }
    }

    private void initializeViews() {
        tvSuccessAmount = findViewById(R.id.tv_success_amount);
        tvSuccessDateTime = findViewById(R.id.tv_success_datetime);
        tvRecipientName = findViewById(R.id.tv_recipient_name);
        tvRecipientAccount = findViewById(R.id.tv_recipient_account);
        tvBankName = findViewById(R.id.bank_name);
        tvTransferNote = findViewById(R.id.tv_transfer_note);
        tvFromAccountName = findViewById(R.id.tv_from_account_name);
        tvTransactionCode = findViewById(R.id.tv_transaction_code);

        ivHome = findViewById(R.id.iv_home);
        cardTransactionDetails = findViewById(R.id.card_transaction_details);

        btnShare = findViewById(R.id.btn_share);
        btnSaveImage = findViewById(R.id.btn_save_image);
        btnSaveTemplate = findViewById(R.id.btn_save_template);
        btnContinue = findViewById(R.id.btn_continue);
    }

    private void loadTransactionData() {
        Intent intent = getIntent();
        double amount = intent.getDoubleExtra("amount", 0);
        String toAccount = intent.getStringExtra("to_account");
        String toName = intent.getStringExtra("to_name");
        String note = intent.getStringExtra("note");
        String bank = intent.getStringExtra("bank");
        String transactionCode = intent.getStringExtra("transaction_code");

        // Format amount
        String formattedAmount = formatWithDots(String.valueOf((long)amount)) + " VNĐ";
        tvSuccessAmount.setText(formattedAmount);

        // Set current date time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        tvSuccessDateTime.setText(currentDateTime);

        // Set recipient info - use toName from intent if available
        if (toName != null && !toName.isEmpty()) {
            tvRecipientName.setText(toName.toUpperCase());
        } else {
            // Fallback to finding by account
            String recipientName = findNameByAccount(toAccount);
            if (recipientName != null && !recipientName.isEmpty()) {
                tvRecipientName.setText(recipientName.toUpperCase());
            } else {
                tvRecipientName.setText("NGƯỜI NHẬN");
            }
        }
        
        // Set recipient account - only account number
        tvRecipientAccount.setText(toAccount);
        
        // Set bank name separately
        if (bank != null && !bank.isEmpty()) {
            String bankFullName = getBankFullName(bank);
            tvBankName.setText(bankFullName);
        } else {
            tvBankName.setText("Ngân hàng");
        }

        // Set transfer note
        if (note != null && !note.isEmpty()) {
            tvTransferNote.setText(note);
        } else {
            tvTransferNote.setText("Chuyển tiền");
        }

        // Set from account name
        String fullName = dataManager.getLastFullName();
        if (fullName != null && !fullName.isEmpty()) {
            tvFromAccountName.setText(fullName.toUpperCase());
        } else {
            tvFromAccountName.setText("NGƯỜI DÙNG");
        }

        // Use transaction code from API if available, otherwise generate
        if (transactionCode != null && !transactionCode.isEmpty()) {
            tvTransactionCode.setText(transactionCode);
        } else {
            String generatedCode = generateTransactionCode();
            tvTransactionCode.setText(generatedCode);
        }
    }
    
    private String getBankFullName(String bankCode) {
        if (bankCode == null) return "";
        
        // Map bank codes to full names
        switch (bankCode) {
            case "HATBANK":
                return "Ngân hàng công nghệ HAT";
            case "AGRIBANK":
                return "Ngân hàng Nông nghiệp và Phát triển Nông thôn Việt Nam";
            case "VIETCOMBANK":
                return "Ngân hàng TMCP Ngoại thương Việt Nam";
            case "BIDV":
                return "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam";
            case "TECHCOMBANK":
                return "Ngân hàng TMCP Kỹ thương Việt Nam";
            case "VIETINBANK":
                return "Ngân hàng TMCP Công thương Việt Nam";
            case "ACB":
                return "Ngân hàng TMCP Á Châu";
            case "MB":
                return "Ngân hàng TMCP Quân đội";
            case "VPB":
                return "Ngân hàng TMCP Việt Nam Thịnh Vượng";
            case "TPB":
                return "Ngân hàng TMCP Tiên Phong";
            case "SACOMBANK":
                return "Ngân hàng TMCP Sài Gòn Thương Tín";
            default:
                return bankCode;
        }
    }

    private void setupListeners() {
        // Home button - Go to UiHomeActivity (home screen with ui_home_fragment)
        ivHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });


        // Share button
        btnShare.setOnClickListener(v -> {
            Toast.makeText(this, "Chia sẻ giao dịch", Toast.LENGTH_SHORT).show();
            // TODO: Implement share functionality
        });

        // Save image button
        btnSaveImage.setOnClickListener(v -> {
            Toast.makeText(this, "Lưu ảnh thành công", Toast.LENGTH_SHORT).show();
            // TODO: Implement screenshot and save functionality
        });

        // Save template button
        btnSaveTemplate.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu mẫu chuyển tiền", Toast.LENGTH_SHORT).show();
            // TODO: Implement save template functionality
        });

        // Continue button - Go to TransferActivity for new transaction
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransferActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }


    private String findNameByAccount(String accountNumber) {
        List<com.example.mobilebanking.models.User> users = dataManager.getMockUsers();
        if (users == null) return null;
        for (com.example.mobilebanking.models.User user : users) {
            if (user == null) continue;
            List<com.example.mobilebanking.models.Account> userAccounts =
                dataManager.getMockAccounts(user.getUserId());
            if (userAccounts == null) continue;
            for (com.example.mobilebanking.models.Account a : userAccounts) {
                if (a != null && accountNumber.equals(a.getAccountNumber())) {
                    return user.getFullName();
                }
            }
        }
        return null;
    }

    private String formatWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "0";
        if (!digits.equals("0")) {
            digits = digits.replaceFirst("^0+", "");
            if (digits.isEmpty()) digits = "0";
        }

        StringBuilder sb = new StringBuilder(digits).reverse();
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < sb.length(); i++) {
            if (i > 0 && i % 3 == 0) grouped.append('.');
            grouped.append(sb.charAt(i));
        }
        return grouped.reverse().toString();
    }

    private String generateTransactionCode() {
        // Generate transaction code like: FT25354277026949
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(100);
        return "FT" + timestamp + String.format(Locale.getDefault(), "%02d", randomNum);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Prevent back button - must use home or continue button
        Toast.makeText(this, "Vui lòng sử dụng nút Trang chủ hoặc Tiếp tục", Toast.LENGTH_SHORT).show();
    }
}


package com.example.mobilebanking.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.utils.DataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfer Activity - Money transfer functionality
 */
public class TransferActivity extends BaseActivity {
    private EditText etRecipientAccount, etAmount, etNote;
    private Button btnContinue;
    private DataManager dataManager;
    private List<Account> accounts;
    private Account selectedAccount;
    private TextView tvAccountType, tvAccountBalance;
    private LinearLayout cvToBank;
    private TextView tvBankName;
    private LinearLayout llAmountContainer;
    private TextView tvAmountInWords;
    private TextView tvRecipientName;
    private View vAccountNameSeparator;
    private String selectedBank = "Ngân hàng";
    private final String[] banks = {"Cùng Ngân Hàng", "VietcomBank", "BIDV", "Techcombank", "VietinBank"};

    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        dataManager = DataManager.getInstance(this);

        // Register broadcast receiver to finish this activity
        IntentFilter filter = new IntentFilter("com.example.mobilebanking.FINISH_TRANSACTION_FLOW");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(finishReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(finishReceiver, filter);
        }

        setupToolbar();
        initializeViews();
        loadAccounts();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(finishReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
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
        etRecipientAccount = findViewById(R.id.et_recipient_account);
        tvRecipientName = findViewById(R.id.tv_recipient_name);
        vAccountNameSeparator = findViewById(R.id.v_account_name_separator);
        etAmount = findViewById(R.id.et_amount);
        tvAmountInWords = findViewById(R.id.tv_amount_in_words);
        etNote = findViewById(R.id.et_note);
        btnContinue = findViewById(R.id.btn_continue);
        tvAccountType = findViewById(R.id.tv_account_type);
        tvAccountBalance = findViewById(R.id.tv_account_balance);
        cvToBank = findViewById(R.id.cv_to_bank);
        tvBankName = findViewById(R.id.tv_bank_name);
        llAmountContainer = findViewById(R.id.ll_amount_container);

        // Set default note with user's name
        setDefaultNote();

        // Hide recipient name initially
        if (tvRecipientName != null) tvRecipientName.setVisibility(View.GONE);
        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
    }

    private void loadAccounts() {
        accounts = dataManager.getMockAccounts("U001");
        
        // Filter out mortgage accounts for transfer and get first available account
        for (Account account : accounts) {
            if (account.getType() != Account.AccountType.MORTGAGE) {
                selectedAccount = account;
                break;
            }
        }

        // Display account information
        if (selectedAccount != null) {
            String accountTypeText = getAccountTypeText(selectedAccount.getType());
            tvAccountType.setText(accountTypeText + " - " + selectedAccount.getAccountNumber());

            // Lấy số dư
            double balance = selectedAccount.getBalance();

            // Làm tròn về số nguyên (nếu balance là double)
            long roundedBalance = Math.round(balance);

            // Format bằng dấu chấm
            String formattedBalance = formatWithDots(String.valueOf(roundedBalance));

            // Gán lên TextView
            tvAccountBalance.setText(formattedBalance + " VNĐ");

        }

        // Setup bank dropdown
        setupBankDropdown();
    }

    private void setupBankDropdown() {
        // Setup click listener for bank card to show bottom sheet
        cvToBank.setOnClickListener(v -> showBankBottomSheet());
    }

    private void showBankBottomSheet() {
        // Create bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(
            R.layout.bottom_sheet_bank_selection, null);

        LinearLayout llBankListBottomSheet = bottomSheetView.findViewById(R.id.ll_bank_list_bottom_sheet);

        // Add bank items to bottom sheet
        for (String bank : banks) {
            View bankItem = createBankItemForBottomSheet(bank, bottomSheetDialog);
            llBankListBottomSheet.addView(bankItem);
        }

        bottomSheetDialog.setContentView(bottomSheetView);

        // Set bottom sheet to occupy 2/3 of screen height
        bottomSheetDialog.setOnShowListener(dialog -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d =
                (com.google.android.material.bottomsheet.BottomSheetDialog) dialog;

            android.widget.FrameLayout bottomSheet = d.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);

                // Get screen height
                android.view.Display display = getWindowManager().getDefaultDisplay();
                android.graphics.Point size = new android.graphics.Point();
                display.getSize(size);
                int screenHeight = size.y;

                // Set peek height to 2/3 of screen height
                int peekHeight = (int) (screenHeight * 2.0 / 3.0);
                behavior.setPeekHeight(peekHeight);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);

                // Set max height
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = peekHeight;
                bottomSheet.setLayoutParams(layoutParams);
            }
        });

        // Round top corners
        if (bottomSheetView.getParent() != null) {
            ((View) bottomSheetView.getParent()).setBackgroundResource(android.R.color.transparent);
        }

        bottomSheetDialog.show();
    }

    private View createBankItemForBottomSheet(String bankName, BottomSheetDialog dialog) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_bank_dropdown, null, false);

        TextView tvBankItem = itemView.findViewById(R.id.tv_bank_item);
        tvBankItem.setText(bankName);

        itemView.setOnClickListener(v -> {
            selectedBank = bankName;
            tvBankName.setText(bankName);
            dialog.dismiss();
        });

        return itemView;
    }


    private String getAccountTypeText(Account.AccountType type) {
        switch (type) {
            case CHECKING:
                return "TÀI KHOẢN THANH TOÁN";
            case SAVINGS:
                return "TÀI KHOẢN TIẾT KIỆM";
            default:
                return "TÀI KHOẢN";
        }
    }

    private void setDefaultNote() {
        // Get current user's full name and set default note
        String fullName = dataManager.getLastFullName();
        
        // If no saved full name, try to get from current logged in user
        if (fullName == null || fullName.isEmpty()) {
            String loggedInUser = dataManager.getLoggedInUser();
            if (loggedInUser != null) {
                // Try to find user in mock data
                List<com.example.mobilebanking.models.User> users = dataManager.getMockUsers();
                for (com.example.mobilebanking.models.User user : users) {
                    if (user.getUsername().equals(loggedInUser)) {
                        fullName = user.getFullName();
                        break;
                    }
                }
            }
        }
        
        if (fullName != null && !fullName.isEmpty()) {
            // Convert to uppercase and add "chuyen tien"
            String defaultNote = fullName.toUpperCase() + " chuyen tien";
            etNote.setText(defaultNote);
        }
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> handleTransfer());

        // Ensure the amount EditText shows cursor at the end and handle leading zero behavior
        // Place cursor at end on initial display
        etAmount.post(() -> etAmount.setSelection(etAmount.getText() != null ? etAmount.getText().length() : 0));

        // Helper to show keyboard
        final android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);

        // Clicking the whole amount container should focus the EditText, move cursor to the end and open keyboard
        if (llAmountContainer != null) {
            llAmountContainer.setOnClickListener(v -> {
                // Do not clear the "0" on click; just place the cursor to the right of the current text
                etAmount.requestFocus();
                etAmount.post(() -> etAmount.setSelection(etAmount.getText() != null ? etAmount.getText().length() : 0));
                if (imm != null) imm.showSoftInput(etAmount, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            });
        }

        // Also ensure clicking the EditText itself moves cursor to the end and opens keyboard
        etAmount.setOnClickListener(v -> {
            // Keep the "0" visible; just place the cursor after it so user sees where to type
            etAmount.post(() -> etAmount.setSelection(etAmount.getText() != null ? etAmount.getText().length() : 0));
            etAmount.requestFocus();
            if (imm != null) imm.showSoftInput(etAmount, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });

        // When EditText loses focus, if empty show "0"; when gains focus and is "0" clear so user can type
        etAmount.setOnFocusChangeListener((v, hasFocus) -> {
            String cur = etAmount.getText() == null ? "" : etAmount.getText().toString();
            if (hasFocus) {
                // Place cursor at end (to the right of the 0) instead of clearing it immediately
                etAmount.post(() -> etAmount.setSelection(etAmount.getText() != null ? etAmount.getText().length() : 0));
                // hide words while editing
                if (tvAmountInWords != null) tvAmountInWords.setVisibility(View.GONE);
            } else {
                if (cur.isEmpty()) {
                    etAmount.setText("0");
                    etAmount.setSelection(1);
                }
                // on focus lost, show amount in words if > 0
                if (tvAmountInWords != null) {
                    showAmountInWordsIfNeeded();
                }
            }
        });

        // Simplified TextWatcher: treat input as integer only, dots are grouping separators (no decimals)
        final boolean[] isEditing = {false};
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing[0]) return;
                isEditing[0] = true;

                String raw = s == null ? "" : s.toString();
                // keep only digits
                raw = raw.replaceAll("[^0-9]", "");

                if (raw.isEmpty()) {
                    etAmount.setText("0");
                    etAmount.setSelection(1);
                    isEditing[0] = false;
                    return;
                }

                // remove leading zeros
                if (raw.length() > 1 && raw.startsWith("0")) {
                    raw = raw.replaceFirst("^0+", "");
                    if (raw.isEmpty()) raw = "0";
                }

                String formatted = formatWithDots(raw);
                etAmount.setText(formatted);
                etAmount.setSelection(formatted.length());

                isEditing[0] = false;
            }
        });

        // Recipient account: hide name while typing, show on focus loss if known
        if (etRecipientAccount != null) {
            etRecipientAccount.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updateRecipientNameFromField();
                } else {
                    if (tvRecipientName != null) tvRecipientName.setVisibility(View.GONE);
                    if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                }
            });

            etRecipientAccount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s == null || s.toString().trim().isEmpty()) {
                        if (tvRecipientName != null) tvRecipientName.setVisibility(View.GONE);
                        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    // Update tvRecipientName based on current value of etRecipientAccount (lookup mock data)
    private void updateRecipientNameFromField() {
        if (etRecipientAccount == null || tvRecipientName == null || dataManager == null) return;
        String input = etRecipientAccount.getText() == null ? "" : etRecipientAccount.getText().toString().trim();
        String acc = input.replaceAll("[^0-9]", "");
        if (acc.isEmpty()) {
            tvRecipientName.setVisibility(View.GONE);
            if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
            return;
        }
        String name = findNameByAccount(acc);
        if (name != null && !name.trim().isEmpty()) {
            tvRecipientName.setText(name.toUpperCase());
            tvRecipientName.setVisibility(View.VISIBLE);
            if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.VISIBLE);
        } else {
            tvRecipientName.setVisibility(View.GONE);
            if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
        }
    }

    // Search mock users and their mock accounts for a matching account number
    private String findNameByAccount(String accountNumber) {
        List<com.example.mobilebanking.models.User> users = dataManager.getMockUsers();
        if (users == null) return null;
        for (com.example.mobilebanking.models.User user : users) {
            if (user == null) continue;
            List<Account> userAccounts = dataManager.getMockAccounts(user.getUserId());
            if (userAccounts == null) continue;
            for (Account a : userAccounts) {
                if (a != null && accountNumber.equals(a.getAccountNumber())) {
                    return user.getFullName();
                }
            }
        }
        return null;
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
            // Remove grouping separators (dots/commas) — app uses only integers
            String cleaned = amountStr.replace(".", "").replace(",", "");
            amount = Double.parseDouble(cleaned);
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
        String fromAccountText = selectedAccount != null ? 
            selectedAccount.getAccountNumber() : "N/A";
        intent.putExtra("from_account", fromAccountText);
        intent.putExtra("to_account", recipientAccount);
        intent.putExtra("amount", amount);
        intent.putExtra("note", note);
        intent.putExtra("bank", selectedBank);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Helper: insert dots as thousand separators into a numeric string (no decimals)
    private String formatWithDots(String digits) {
        if (digits == null || digits.isEmpty()) return "0";
        // Remove leading zeros unless the value is exactly "0"
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

    private void showAmountInWordsIfNeeded() {
         String txt = etAmount.getText() == null ? "" : etAmount.getText().toString().trim();
         if (txt.isEmpty() || txt.equals("0")) {
             tvAmountInWords.setVisibility(View.GONE);
             return;
         }

         // Clean formatted amount: remove any non-digit characters (grouping dots) and parse full integer
         String intPartStr = txt.replaceAll("[^0-9]", "");

         if (intPartStr.isEmpty()) {
             tvAmountInWords.setVisibility(View.GONE);
             return;
         }

         try {
             long value = Long.parseLong(intPartStr);
             if (value <= 0) {
                 tvAmountInWords.setVisibility(View.GONE);
                 return;
             }
             String words = numberToVietnameseWords(value);
             tvAmountInWords.setText(words + " đồng");
             tvAmountInWords.setVisibility(View.VISIBLE);
         } catch (NumberFormatException ex) {
             tvAmountInWords.setVisibility(View.GONE);
         }
     }

    // Convert number to Vietnamese words (supports up to billions/trillions reasonably)
    private String numberToVietnameseWords(long num) {
        if (num == 0) return "Không";
        String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
        String[] scales = {""," nghìn"," triệu"," tỷ"," nghìn tỷ"," triệu tỷ"};

        List<Integer> groups = new ArrayList<>();
        while (num > 0) {
            groups.add((int)(num % 1000));
            num /= 1000;
        }

        StringBuilder sb = new StringBuilder();
        boolean hadHigherNonZero = false;
        for (int i = groups.size()-1; i >= 0; i--) {
            int g = groups.get(i);
            if (g == 0) continue;
            String s = readThreeDigits(g, digits, hadHigherNonZero);
            if (sb.length() > 0) sb.append(" ");
            sb.append(s).append(scales[i]);
            hadHigherNonZero = true;
        }

        // capitalize first letter
        if (sb.length() == 0) return "Không";
        String res = sb.toString().trim();
        return Character.toUpperCase(res.charAt(0)) + res.substring(1);
    }

    private String readThreeDigits(int num, String[] digits, boolean hadHigherNonZero) {
        int hundreds = num / 100;
        int tens = (num % 100) / 10;
        int units = num % 10;
        StringBuilder sb = new StringBuilder();
        if (hundreds > 0) {
            sb.append(digits[hundreds]).append(" trăm");
        }
        if (tens == 0) {
            if (units != 0) {
                // if there is a hundreds part, say "lẻ"; if not but there was a higher non-zero group, also say "lẻ"
                if (hundreds > 0) {
                    sb.append(" lẻ");
                } else if (hadHigherNonZero) {
                    sb.append(" lẻ");
                }
                sb.append(" ").append(readUnit(units, tens));
            }
        } else if (tens == 1) {
            sb.append(" mười");
            if (units != 0) sb.append(" ").append(readUnit(units, tens));
        } else {
            sb.append(" ").append(digits[tens]).append(" mươi");
            if (units != 0) sb.append(" ").append(readUnit(units, tens));
        }
        return sb.toString().trim();
    }

    private String readUnit(int unit, int tens) {
        switch (unit) {
            case 1:
                if (tens >= 1) return "mốt"; else return "một";
            case 4:
                if (tens >= 1) return "bốn"; else return "bốn";
            case 5:
                if (tens >= 1) return "lăm"; else return "năm";
            default:
                String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
                return digits[unit];
        }
    }
}

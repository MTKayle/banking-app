package com.example.mobilebanking.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.BankApiService;
import com.example.mobilebanking.api.ExternalAccountApiService;
import com.example.mobilebanking.api.dto.AccountInfoResponse;
import com.example.mobilebanking.api.dto.BankListResponse;
import com.example.mobilebanking.api.dto.BankResponse;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.api.dto.ExternalAccountInfoApiResponse;
import com.example.mobilebanking.api.dto.ExternalAccountInfoResponse;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.utils.DataManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Transfer Activity - Money transfer functionality
 */
public class TransferActivity extends BaseActivity {
    private static final String TAG = "TransferActivity";
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
    private TextView tvBankErrorIndicator;
    private TextView tvAccountErrorIndicator;
    private TextView tvAmountError;
    private com.google.android.material.textfield.TextInputLayout tilRecipientAccount;
    private String selectedBank = "Ngân hàng"; // Store selected bank code for display
    private String selectedBankCode = null; // Store bank code (e.g., "HATBANK")
    private String selectedBankBin = null; // Store bank BIN for API calls
    private List<BankResponse> bankList = new ArrayList<>(); // Store banks from API
    private BigDecimal currentBalance = BigDecimal.ZERO; // Store current account balance
    private static final long MAX_TRANSFER_AMOUNT = 500000000L; // 500 million VND

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
        tilRecipientAccount = findViewById(R.id.til_recipient_account);
        tvRecipientName = findViewById(R.id.tv_recipient_name);
        vAccountNameSeparator = findViewById(R.id.v_account_name_separator);
        etAmount = findViewById(R.id.et_amount);
        tvAmountInWords = findViewById(R.id.tv_amount_in_words);
        tvAmountError = findViewById(R.id.tv_amount_error);
        etNote = findViewById(R.id.et_note);
        btnContinue = findViewById(R.id.btn_continue);
        tvAccountType = findViewById(R.id.tv_account_type);
        tvAccountBalance = findViewById(R.id.tv_account_balance);
        cvToBank = findViewById(R.id.cv_to_bank);
        tvBankName = findViewById(R.id.tv_bank_name);
        tvBankErrorIndicator = findViewById(R.id.tv_bank_error_indicator);
        tvAccountErrorIndicator = findViewById(R.id.tv_account_error_indicator);
        llAmountContainer = findViewById(R.id.ll_amount_container);

        // Set default note with user's name
        setDefaultNote();

        // Hide recipient name initially
        if (tvRecipientName != null) tvRecipientName.setVisibility(View.GONE);
        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
        
        // Setup bank dropdown click listener
        cvToBank.setOnClickListener(v -> showBankBottomSheet());
    }

    private void loadAccounts() {
        // Get userId from DataManager
        Long userId = dataManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Call API to get checking account info
        AccountApiService accountApiService = ApiClient.getAccountApiService();
        Call<CheckingAccountInfoResponse> call = accountApiService.getCheckingAccountInfo(userId);
        
        call.enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse accountInfo = response.body();
                    
                    // Display account information
                    String accountNumber = accountInfo.getAccountNumber();
                    BigDecimal balance = accountInfo.getBalance();
                    
                    if (accountNumber != null && balance != null) {
                        // Store balance for validation
                        currentBalance = balance;
                        
                        tvAccountType.setText("TÀI KHOẢN THANH TOÁN - " + accountNumber);
                        
                        // Format balance - convert BigDecimal to long
                        long roundedBalance = balance.longValue();
                        String formattedBalance = formatWithDots(String.valueOf(roundedBalance));
                        tvAccountBalance.setText(formattedBalance + " VNĐ");
                        
                        Log.d(TAG, "Account loaded: " + accountNumber + ", Balance: " + formattedBalance);
                    } else {
                        Toast.makeText(TransferActivity.this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load account: " + response.code());
                    Toast.makeText(TransferActivity.this, "Không thể tải thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                Log.e(TAG, "Error loading account", t);
                Toast.makeText(TransferActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Load banks from API
        loadBanks();
    }

    private void loadBanks() {
        BankApiService bankApiService = ApiClient.getBankApiService();
        Call<BankListResponse> call = bankApiService.getAllBanks();
        
        call.enqueue(new Callback<BankListResponse>() {
            @Override
            public void onResponse(Call<BankListResponse> call, Response<BankListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BankListResponse bankListResponse = response.body();
                    if (bankListResponse.isSuccess() && bankListResponse.getData() != null) {
                        bankList = bankListResponse.getData();
                        Log.d(TAG, "Loaded " + bankList.size() + " banks");
                    } else {
                        Log.e(TAG, "Failed to load banks: " + bankListResponse.getMessage());
                        Toast.makeText(TransferActivity.this, "Không thể tải danh sách ngân hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load banks: " + response.code());
                    Toast.makeText(TransferActivity.this, "Không thể tải danh sách ngân hàng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<BankListResponse> call, Throwable t) {
                Log.e(TAG, "Error loading banks", t);
                Toast.makeText(TransferActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Lookup account holder name from API
     * - If HAT bank: use internal API /api/accounts/info/{accountNumber}
     * - If other bank: use external API /api/external-accounts/info
     */
    private void lookupAccountName(String bankCode, String bankBin, String accountNumber) {
        // Check if this is HAT bank (internal)
        if ("HATBANK".equals(bankCode)) {
            lookupInternalAccount(accountNumber);
        } else {
            lookupExternalAccount(bankBin, accountNumber);
        }
    }

    /**
     * Lookup internal HAT bank account
     */
    private void lookupInternalAccount(String accountNumber) {
        AccountApiService accountApiService = ApiClient.getAccountApiService();
        Call<AccountInfoResponse> call = accountApiService.getAccountInfo(accountNumber);
        
        call.enqueue(new Callback<AccountInfoResponse>() {
            @Override
            public void onResponse(Call<AccountInfoResponse> call, Response<AccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AccountInfoResponse accountInfo = response.body();
                    String holderName = accountInfo.getAccountHolderName();
                    
                    if (holderName != null && !holderName.trim().isEmpty()) {
                        tvRecipientName.setText(holderName.toUpperCase());
                        tvRecipientName.setVisibility(View.VISIBLE);
                        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.VISIBLE);
                        clearAccountError();
                    } else {
                        showAccountError("Số tài khoản không tồn tại");
                        tvRecipientName.setVisibility(View.GONE);
                        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                    }
                } else {
                    // Account not found
                    showAccountError("Số tài khoản không tồn tại");
                    tvRecipientName.setVisibility(View.GONE);
                    if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onFailure(Call<AccountInfoResponse> call, Throwable t) {
                Log.e(TAG, "Error looking up internal account", t);
                showAccountError("Lỗi kết nối");
                tvRecipientName.setVisibility(View.GONE);
                if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Lookup external bank account
     */
    private void lookupExternalAccount(String bankBin, String accountNumber) {
        ExternalAccountApiService externalAccountApiService = ApiClient.getExternalAccountApiService();
        Call<ExternalAccountInfoApiResponse> call = externalAccountApiService.getAccountInfo(bankBin, accountNumber);
        
        call.enqueue(new Callback<ExternalAccountInfoApiResponse>() {
            @Override
            public void onResponse(Call<ExternalAccountInfoApiResponse> call, Response<ExternalAccountInfoApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ExternalAccountInfoApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        ExternalAccountInfoResponse accountInfo = apiResponse.getData();
                        String fullName = accountInfo.getFullName();
                        
                        if (fullName != null && !fullName.trim().isEmpty()) {
                            tvRecipientName.setText(fullName.toUpperCase());
                            tvRecipientName.setVisibility(View.VISIBLE);
                            if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.VISIBLE);
                            clearAccountError();
                        } else {
                            showAccountError("Số tài khoản không tồn tại");
                            tvRecipientName.setVisibility(View.GONE);
                            if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                        }
                    } else {
                        showAccountError("Số tài khoản không tồn tại");
                        tvRecipientName.setVisibility(View.GONE);
                        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                    }
                } else {
                    showAccountError("Số tài khoản không tồn tại");
                    tvRecipientName.setVisibility(View.GONE);
                    if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onFailure(Call<ExternalAccountInfoApiResponse> call, Throwable t) {
                Log.e(TAG, "Error looking up external account", t);
                showAccountError("Lỗi kết nối");
                tvRecipientName.setVisibility(View.GONE);
                if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Show bank error state
     */
    private void showBankError() {
        if (tvBankErrorIndicator != null) {
            tvBankErrorIndicator.setVisibility(View.VISIBLE);
        }
        if (tvBankName != null) {
            tvBankName.setTextColor(0xFFF44336); // Red color
        }
        Toast.makeText(this, "Vui lòng chọn ngân hàng trước", Toast.LENGTH_SHORT).show();
    }

    /**
     * Clear bank error state
     */
    private void clearBankError() {
        if (tvBankErrorIndicator != null) {
            tvBankErrorIndicator.setVisibility(View.GONE);
        }
        if (tvBankName != null) {
            tvBankName.setTextColor(0xFF000000); // Black color
        }
    }

    /**
     * Show account error
     */
    private void showAccountError(String message) {
        if (tvAccountErrorIndicator != null) {
            tvAccountErrorIndicator.setVisibility(View.VISIBLE);
        }
        if (etRecipientAccount != null) {
            etRecipientAccount.setTextColor(0xFFF44336); // Red color
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clear account error state
     */
    private void clearAccountError() {
        if (tvAccountErrorIndicator != null) {
            tvAccountErrorIndicator.setVisibility(View.GONE);
        }
        if (etRecipientAccount != null) {
            etRecipientAccount.setTextColor(0xFF000000); // Black color
        }
    }

    /**
     * Show amount error
     */
    private void showAmountError(String message) {
        if (tvAmountError != null) {
            tvAmountError.setText(message);
            tvAmountError.setVisibility(View.VISIBLE);
        }
        if (etAmount != null) {
            etAmount.setTextColor(0xFFF44336); // Red color
        }
        if (llAmountContainer != null) {
            llAmountContainer.setBackgroundResource(R.drawable.edittext_border_error);
        }
    }

    /**
     * Clear amount error
     */
    private void clearAmountError() {
        if (tvAmountError != null) {
            tvAmountError.setVisibility(View.GONE);
        }
        if (etAmount != null) {
            etAmount.setTextColor(0xFF000000); // Black color
        }
        if (llAmountContainer != null) {
            llAmountContainer.setBackgroundResource(R.drawable.edittext_border);
        }
    }

    private void showBankBottomSheet() {
        // Check if banks are loaded
        if (bankList == null || bankList.isEmpty()) {
            Toast.makeText(this, "Đang tải danh sách ngân hàng...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(
            R.layout.bottom_sheet_bank_selection, null);

        LinearLayout llBankListBottomSheet = bottomSheetView.findViewById(R.id.ll_bank_list_bottom_sheet);

        // Add bank items to bottom sheet from API data
        for (BankResponse bank : bankList) {
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

    private View createBankItemForBottomSheet(BankResponse bank, BottomSheetDialog dialog) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_bank_dropdown, null, false);

        TextView tvBankCode = itemView.findViewById(R.id.tv_bank_code);
        TextView tvBankNameItem = itemView.findViewById(R.id.tv_bank_name);
        
        tvBankCode.setText(bank.getBankCode());
        tvBankNameItem.setText(bank.getBankName());

        itemView.setOnClickListener(v -> {
            selectedBank = bank.getBankCode(); // Store bank code for display (e.g., "HAT")
            selectedBankCode = bank.getBankCode(); // Store full bank code (e.g., "HATBANK")
            selectedBankBin = bank.getBankBin();
            tvBankName.setText(bank.getBankCode()); // Display bank code in the selection field
            
            // Clear bank error state
            clearBankError();
            
            // Clear recipient account and name when bank changes
            if (etRecipientAccount != null) {
                etRecipientAccount.setText("");
            }
            clearAccountError();
            if (tvRecipientName != null) {
                tvRecipientName.setVisibility(View.GONE);
            }
            if (vAccountNameSeparator != null) {
                vAccountNameSeparator.setVisibility(View.GONE);
            }
            
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
        // Also validate that account number is entered before allowing amount input
        etAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Check if account number is entered
                String accountNumber = etRecipientAccount.getText().toString().trim();
                if (accountNumber.isEmpty()) {
                    showAmountError("Vui lòng nhập số tài khoản trước");
                    etAmount.clearFocus();
                    return;
                }
                // Clear error when user starts typing
                clearAmountError();
                // Place cursor at end (to the right of the 0) instead of clearing it immediately
                etAmount.post(() -> etAmount.setSelection(etAmount.getText() != null ? etAmount.getText().length() : 0));
                // hide words while editing
                if (tvAmountInWords != null) tvAmountInWords.setVisibility(View.GONE);
            } else {
                String cur = etAmount.getText() == null ? "" : etAmount.getText().toString();
                if (cur.isEmpty()) {
                    etAmount.setText("0");
                    etAmount.setSelection(1);
                }
                // Validate amount
                String cleaned = cur.replace(".", "").replace(",", "");
                try {
                    double amount = Double.parseDouble(cleaned);
                    if (amount <= 0) {
                        showAmountError("Số tiền phải lớn hơn 0");
                    } else if (amount > MAX_TRANSFER_AMOUNT) {
                        showAmountError("Số tiền chuyển tối đa 500.000.000 VNĐ");
                    } else if (BigDecimal.valueOf(amount).compareTo(currentBalance) > 0) {
                        showAmountError("Số dư không đủ để thực hiện giao dịch");
                    } else {
                        clearAmountError();
                    }
                } catch (NumberFormatException e) {
                    // Ignore
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user types
                if (s != null && s.length() > 0) {
                    clearAmountError();
                }
            }
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

        // Recipient account: validate bank selection and lookup account name
        if (etRecipientAccount != null) {
            etRecipientAccount.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    // Check if bank is selected when user focuses on account field
                    if (selectedBankCode == null || selectedBankCode.isEmpty()) {
                        showBankError();
                        etRecipientAccount.clearFocus();
                        return;
                    }
                    // Hide name while typing
                    if (tvRecipientName != null) tvRecipientName.setVisibility(View.GONE);
                    if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                } else {
                    // When focus is lost, lookup account name
                    String accountNumber = etRecipientAccount.getText().toString().trim();
                    if (accountNumber.isEmpty()) {
                        // Empty account - show red text and error indicator
                        showAccountError("Vui lòng nhập số tài khoản");
                    } else if (selectedBankCode != null && !selectedBankCode.isEmpty()) {
                        lookupAccountName(selectedBankCode, selectedBankBin, accountNumber);
                    }
                }
            });

            etRecipientAccount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Clear error when user types
                    if (s != null && s.length() > 0) {
                        clearAccountError();
                    }
                    if (s == null || s.toString().trim().isEmpty()) {
                        if (tvRecipientName != null) tvRecipientName.setVisibility(View.GONE);
                        if (vAccountNameSeparator != null) vAccountNameSeparator.setVisibility(View.GONE);
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void handleTransfer() {
        String recipientAccount = etRecipientAccount.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        boolean hasError = false;

        // Validation: Check bank selection
        if (selectedBankCode == null || selectedBankCode.isEmpty()) {
            showBankError();
            hasError = true;
        } else {
            clearBankError();
        }

        // Validation: Check recipient account
        if (recipientAccount.isEmpty()) {
            showAccountError("Vui lòng nhập số tài khoản người nhận");
            hasError = true;
        } else {
            // Check if account name was loaded (meaning account exists)
            if (tvRecipientName != null && tvRecipientName.getVisibility() != View.VISIBLE) {
                showAccountError("Số tài khoản không tồn tại");
                hasError = true;
            }
        }

        // Validation: Check amount
        if (amountStr.isEmpty() || amountStr.equals("0")) {
            showAmountError("Vui lòng nhập số tiền");
            hasError = true;
        } else {
            double amount;
            try {
                // Remove grouping separators (dots/commas) — app uses only integers
                String cleaned = amountStr.replace(".", "").replace(",", "");
                amount = Double.parseDouble(cleaned);
                if (amount <= 0) {
                    showAmountError("Số tiền phải lớn hơn 0");
                    hasError = true;
                } else if (amount > MAX_TRANSFER_AMOUNT) {
                    showAmountError("Số tiền chuyển tối đa 500.000.000 VNĐ");
                    hasError = true;
                } else if (BigDecimal.valueOf(amount).compareTo(currentBalance) > 0) {
                    showAmountError("Số dư không đủ để thực hiện giao dịch");
                    hasError = true;
                } else {
                    clearAmountError();
                }
            } catch (NumberFormatException e) {
                showAmountError("Số tiền không hợp lệ");
                hasError = true;
            }
        }

        // If there are errors, don't proceed
        if (hasError) {
            return;
        }

        // All validations passed, proceed with transfer
        double amount;
        try {
            String cleaned = amountStr.replace(".", "").replace(",", "");
            amount = Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to confirmation screen
        Intent intent = new Intent(this, TransactionConfirmationActivity.class);
        intent.putExtra("type", "TRANSFER");
        
        // Get recipient name from TextView (already looked up)
        String recipientName = tvRecipientName.getVisibility() == View.VISIBLE ? 
            tvRecipientName.getText().toString() : "";
        
        intent.putExtra("to_account", recipientAccount);
        intent.putExtra("to_name", recipientName);
        intent.putExtra("amount", amount);
        intent.putExtra("note", note);
        intent.putExtra("bank", selectedBank); // Bank code (e.g., "HATBANK")
        intent.putExtra("bank_bin", selectedBankBin); // Bank BIN for external transfer
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

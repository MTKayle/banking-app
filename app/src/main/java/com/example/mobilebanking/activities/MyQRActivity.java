package com.example.mobilebanking.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.QRCodeRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * My QR Activity - MoMo style
 * Hiển thị QR code của tài khoản
 */
public class MyQRActivity extends BaseActivity {
    
    private static final String TAG = "MyQRActivity";
    
    private ImageView ivQRCode, ivToggleMask, ivCopy, btnBack, btnHelp;
    private TextView tvAccountHolderName, tvAccountNumber, tvMaskedAccount, btnAddAmount;
    private TextView tvQrAmount, tvQrMessage;
    private LinearLayout btnDownload, btnCustomize, btnShare, layoutQrInfo;
    
    private String accountNumber;
    private String accountHolderName;
    private Bitmap qrBitmap;
    private boolean isMasked = true;
    
    // QR data with amount and message
    private String qrAmount = "";
    private String qrMessage = "";
    
    // For bottom sheet
    private EditText etAmountBottomSheet;
    private TextView tvAmountInWordsBottomSheet;
    
    // API Service
    private AccountApiService accountApiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr);
        
        // Get data from intent
        accountNumber = getIntent().getStringExtra("accountNumber");
        accountHolderName = getIntent().getStringExtra("accountHolderName");
        
        // Initialize API service
        accountApiService = ApiClient.getAccountApiService();
        
        initViews();
        setupToolbar();
        updateUI();
        loadQRCodeFromAPI(null, null); // Load initial QR without amount/description
        setupClickListeners();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnHelp = findViewById(R.id.btn_help);
        ivQRCode = findViewById(R.id.iv_qr_code);
        tvAccountHolderName = findViewById(R.id.tv_account_holder_name);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvMaskedAccount = findViewById(R.id.tv_masked_account);
        ivToggleMask = findViewById(R.id.iv_toggle_mask);
        ivCopy = findViewById(R.id.iv_copy);
        btnDownload = findViewById(R.id.btn_download);
        btnCustomize = findViewById(R.id.btn_customize);
        btnShare = findViewById(R.id.btn_share);
        btnAddAmount = findViewById(R.id.btn_add_amount);
        layoutQrInfo = findViewById(R.id.layout_qr_info);
        tvQrAmount = findViewById(R.id.tv_qr_amount);
        tvQrMessage = findViewById(R.id.tv_qr_message);
    }
    
    private void setupToolbar() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnHelp.setOnClickListener(v -> 
            Toast.makeText(this, "Trợ giúp", Toast.LENGTH_SHORT).show()
        );
    }
    
    private void updateUI() {
        if (accountHolderName != null) {
            tvAccountHolderName.setText(accountHolderName.toUpperCase());
        }
        if (accountNumber != null) {
            tvAccountNumber.setText(accountNumber);
            updateMaskedAccount();
        }
    }
    
    private void updateMaskedAccount() {
        if (accountNumber != null && accountNumber.length() > 3) {
            if (isMasked) {
                String last3 = accountNumber.substring(accountNumber.length() - 3);
                tvMaskedAccount.setText("*******" + last3);
                ivToggleMask.setImageResource(R.drawable.ic_eye_open);
            } else {
                tvMaskedAccount.setText(accountNumber);
                ivToggleMask.setImageResource(R.drawable.ic_lock);
            }
        }
    }
    
    private void loadQRCodeFromAPI(Long amount, String description) {
        // Create request body
        QRCodeRequest request = new QRCodeRequest(amount, description);
        
        Call<ResponseBody> call = accountApiService.getCheckingQRCode(request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Convert response body to bitmap
                        InputStream inputStream = response.body().byteStream();
                        qrBitmap = BitmapFactory.decodeStream(inputStream);
                        
                        if (qrBitmap != null) {
                            ivQRCode.setImageBitmap(qrBitmap);
                            Log.d(TAG, "QR code loaded successfully");
                        } else {
                            Log.e(TAG, "Failed to decode QR code image");
                            Toast.makeText(MyQRActivity.this, "Không thể tải mã QR", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing QR code image", e);
                        Toast.makeText(MyQRActivity.this, "Lỗi khi xử lý mã QR", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load QR code: " + response.code());
                    Toast.makeText(MyQRActivity.this, "Không thể tải mã QR từ server", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error loading QR code", t);
                Toast.makeText(MyQRActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void generateQRCode() {
        if (accountNumber == null || accountNumber.isEmpty()) {
            Toast.makeText(this, "Không có thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Build QR data with amount and message if available
            String qrData = accountNumber;
            if (!TextUtils.isEmpty(qrAmount) || !TextUtils.isEmpty(qrMessage)) {
                qrData = accountNumber + "|" + qrAmount + "|" + qrMessage;
            }
            
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, 
                    BarcodeFormat.QR_CODE, 500, 500);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            ivQRCode.setImageBitmap(qrBitmap);
            
        } catch (WriterException e) {
            Toast.makeText(this, "Không thể tạo mã QR", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void setupClickListeners() {
        btnDownload.setOnClickListener(v -> saveQRCode());
        btnShare.setOnClickListener(v -> shareQRCode());
        btnCustomize.setOnClickListener(v -> 
            Toast.makeText(this, "Tuỳ chỉnh QR code", Toast.LENGTH_SHORT).show()
        );
        btnAddAmount.setOnClickListener(v -> showAddAmountBottomSheet());
        
        // Toggle mask/unmask
        ivToggleMask.setOnClickListener(v -> {
            isMasked = !isMasked;
            updateMaskedAccount();
        });
        
        // Copy account number
        ivCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Account Number", accountNumber);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép số tài khoản", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void saveQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Không có mã QR để lưu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String fileName = "QR_" + accountNumber + ".png";
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    qrBitmap,
                    fileName,
                    "QR Code for account " + accountNumber
            );
            
            if (savedImageURL != null) {
                Toast.makeText(this, "Đã lưu mã QR vào thư viện ảnh", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể lưu mã QR", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void shareQRCode() {
        if (qrBitmap == null) {
            Toast.makeText(this, "Không có mã QR để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Save to cache
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "qr_code.png");
            FileOutputStream stream = new FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            
            // Share
            Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    file
            );
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "Mã QR tài khoản: " + accountNumber + "\nChủ tài khoản: " + accountHolderName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ mã QR"));
            
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    /**
     * Show bottom sheet to add amount and message to QR
     */
    private void showAddAmountBottomSheet() {
        // Create bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_add_amount, null);
        
        // Get views
        etAmountBottomSheet = bottomSheetView.findViewById(R.id.et_amount);
        tvAmountInWordsBottomSheet = bottomSheetView.findViewById(R.id.tv_amount_in_words);
        EditText etMessage = bottomSheetView.findViewById(R.id.et_message);
        Button btnSave = bottomSheetView.findViewById(R.id.btn_save);
        Button btnClear = bottomSheetView.findViewById(R.id.btn_clear);
        
        // Pre-fill with existing data
        if (!TextUtils.isEmpty(qrAmount)) {
            // Format the amount with dots
            String formatted = formatWithDots(qrAmount);
            etAmountBottomSheet.setText(formatted);
            showAmountInWordsIfNeeded();
        }
        if (!TextUtils.isEmpty(qrMessage)) {
            etMessage.setText(qrMessage);
        }
        
        // Setup TextWatcher for amount formatting
        setupAmountTextWatcher();
        
        // Save button
        btnSave.setOnClickListener(v -> {
            String amountText = etAmountBottomSheet.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            
            // Remove dots to get raw number
            String amountRaw = amountText.replaceAll("[^0-9]", "");
            
            // Validate amount - must have amount
            if (TextUtils.isEmpty(amountRaw) || amountRaw.equals("0")) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Validate amount format
            try {
                long amountValue = Long.parseLong(amountRaw);
                if (amountValue <= 0) {
                    Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Save data (raw number without dots)
                qrAmount = amountRaw;
                qrMessage = message;
                
                // Update UI
                updateQrInfoDisplay();
                
                // Call API to regenerate QR code with amount and description
                loadQRCodeFromAPI(amountValue, TextUtils.isEmpty(message) ? null : message);
                
                Toast.makeText(this, "Đã cập nhật thông tin QR", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        
        // Clear button - only clear inputs, don't close dialog
        btnClear.setOnClickListener(v -> {
            etAmountBottomSheet.setText("");
            etMessage.setText("");
            tvAmountInWordsBottomSheet.setVisibility(View.GONE);
        });
        
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    
    /**
     * Update QR info display (amount and message)
     */
    private void updateQrInfoDisplay() {
        if (!TextUtils.isEmpty(qrAmount)) {
            // Show info layout, hide add button
            btnAddAmount.setVisibility(View.GONE);
            layoutQrInfo.setVisibility(View.VISIBLE);
            
            // Format and display amount with dots
            try {
                long amount = Long.parseLong(qrAmount);
                String formatted = formatWithDots(qrAmount);
                tvQrAmount.setText(formatted + " VND");
            } catch (NumberFormatException e) {
                tvQrAmount.setText(qrAmount + " VND");
            }
            
            // Display message if available
            if (!TextUtils.isEmpty(qrMessage)) {
                tvQrMessage.setVisibility(View.VISIBLE);
                tvQrMessage.setText(qrMessage);
            } else {
                tvQrMessage.setVisibility(View.GONE);
            }
            
            // Make info layout clickable to edit
            layoutQrInfo.setOnClickListener(v -> showAddAmountBottomSheet());
        } else {
            // No amount - show add button, hide info
            btnAddAmount.setVisibility(View.VISIBLE);
            layoutQrInfo.setVisibility(View.GONE);
        }
    }
    
    /**
     * Setup TextWatcher for amount input with dot formatting
     */
    private void setupAmountTextWatcher() {
        final boolean[] isEditing = {false};
        etAmountBottomSheet.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isEditing[0]) return;
                isEditing[0] = true;

                String raw = s == null ? "" : s.toString();
                // keep only digits
                raw = raw.replaceAll("[^0-9]", "");

                if (raw.isEmpty()) {
                    etAmountBottomSheet.setText("0");
                    etAmountBottomSheet.setSelection(1);
                    tvAmountInWordsBottomSheet.setVisibility(View.GONE);
                    isEditing[0] = false;
                    return;
                }

                // remove leading zeros
                if (raw.length() > 1 && raw.startsWith("0")) {
                    raw = raw.replaceFirst("^0+", "");
                    if (raw.isEmpty()) raw = "0";
                }

                String formatted = formatWithDots(raw);
                etAmountBottomSheet.setText(formatted);
                etAmountBottomSheet.setSelection(formatted.length());

                // Show amount in words
                showAmountInWordsIfNeeded();

                isEditing[0] = false;
            }
        });
    }
    
    /**
     * Format number with dots as thousand separators
     */
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
    
    /**
     * Show amount in Vietnamese words
     */
    private void showAmountInWordsIfNeeded() {
        String txt = etAmountBottomSheet.getText() == null ? "" : etAmountBottomSheet.getText().toString().trim();
        if (txt.isEmpty() || txt.equals("0")) {
            tvAmountInWordsBottomSheet.setVisibility(View.GONE);
            return;
        }

        // Clean formatted amount: remove any non-digit characters (grouping dots) and parse full integer
        String intPartStr = txt.replaceAll("[^0-9]", "");

        if (intPartStr.isEmpty()) {
            tvAmountInWordsBottomSheet.setVisibility(View.GONE);
            return;
        }

        try {
            long value = Long.parseLong(intPartStr);
            if (value <= 0) {
                tvAmountInWordsBottomSheet.setVisibility(View.GONE);
                return;
            }
            String words = numberToVietnameseWords(value);
            tvAmountInWordsBottomSheet.setText(words + " đồng");
            tvAmountInWordsBottomSheet.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ex) {
            tvAmountInWordsBottomSheet.setVisibility(View.GONE);
        }
    }
    
    /**
     * Convert number to Vietnamese words
     */
    private String numberToVietnameseWords(long num) {
        if (num == 0) return "Không";
        String[] digits = {"không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
        String[] scales = {""," nghìn"," triệu"," tỷ"," nghìn tỷ"," triệu tỷ"};

        java.util.List<Integer> groups = new java.util.ArrayList<>();
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



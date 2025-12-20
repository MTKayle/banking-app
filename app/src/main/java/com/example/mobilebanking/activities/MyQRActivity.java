package com.example.mobilebanking.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilebanking.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * My QR Activity - MoMo style
 * Hiển thị QR code của tài khoản
 */
public class MyQRActivity extends BaseActivity {
    
    private ImageView ivQRCode, ivToggleMask, ivCopy, btnBack, btnHelp;
    private TextView tvAccountHolderName, tvAccountNumber, tvMaskedAccount;
    private LinearLayout btnDownload, btnCustomize, btnShare;
    private Button btnAddAmount;
    
    private String accountNumber;
    private String accountHolderName;
    private Bitmap qrBitmap;
    private boolean isMasked = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr);
        
        // Get data from intent
        accountNumber = getIntent().getStringExtra("accountNumber");
        accountHolderName = getIntent().getStringExtra("accountHolderName");
        
        initViews();
        setupToolbar();
        updateUI();
        generateQRCode();
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
    
    private void generateQRCode() {
        if (accountNumber == null || accountNumber.isEmpty()) {
            Toast.makeText(this, "Không có thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(accountNumber, 
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
        btnAddAmount.setOnClickListener(v -> 
            Toast.makeText(this, "Thêm số tiền vào QR", Toast.LENGTH_SHORT).show()
        );
        
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
}


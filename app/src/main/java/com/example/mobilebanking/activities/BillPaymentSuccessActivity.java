package com.example.mobilebanking.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.mobilebanking.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Bill Payment Success Activity - Light theme
 * Display transaction receipt
 */
public class BillPaymentSuccessActivity extends AppCompatActivity {

    // Views
    private ImageButton btnBack, btnHome;
    private TextView tvTransactionId, tvTransactionTime, tvAccountNumber, tvAvailableBalance;
    private TextView tvServiceType, tvBillCode, tvCustomerName, tvAmount;
    private LinearLayout layoutAccountHeader, layoutAccountDetails;
    private ImageView ivAccountExpand;
    private Button btnShare, btnSave;
    
    private boolean isAccountExpanded = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment_success_light);
        
        initializeViews();
        loadDataFromIntent();
        setupClickListeners();
    }
    
    private void initializeViews() {
        // Header
        btnBack = findViewById(R.id.btn_back);
        btnHome = findViewById(R.id.btn_home);
        
        // Transaction info
        tvTransactionId = findViewById(R.id.tv_transaction_id);
        tvTransactionTime = findViewById(R.id.tv_transaction_time);
        
        // Account section
        layoutAccountHeader = findViewById(R.id.layout_account_header);
        layoutAccountDetails = findViewById(R.id.layout_account_details);
        ivAccountExpand = findViewById(R.id.iv_account_expand);
        tvAccountNumber = findViewById(R.id.tv_account_number);
        tvAvailableBalance = findViewById(R.id.tv_available_balance);
        
        // Bill details
        tvServiceType = findViewById(R.id.tv_service_type);
        tvBillCode = findViewById(R.id.tv_bill_code);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvAmount = findViewById(R.id.tv_amount);
        
        // Action buttons
        btnShare = findViewById(R.id.btn_share);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void loadDataFromIntent() {
        Intent intent = getIntent();
        
        // Generate transaction ID and time
        String transactionId = generateTransactionId();
        String transactionTime = getCurrentTime();
        
        tvTransactionId.setText(transactionId);
        tvTransactionTime.setText(transactionTime);
        
        // Account info
        String accountNumber = intent.getStringExtra("account_number");
        if (accountNumber != null) {
            tvAccountNumber.setText(accountNumber);
        }
        
        // Available balance (mock - subtract amount from original)
        tvAvailableBalance.setText("252,827 VND");
        
        // Bill details
        String billType = intent.getStringExtra("bill_type");
        String billCode = intent.getStringExtra("bill_code");
        String userName = intent.getStringExtra("user_name");
        String amount = intent.getStringExtra("amount");
        
        if (billType != null) {
            tvServiceType.setText(billType);
        }
        
        if (billCode != null) {
            tvBillCode.setText(billCode);
        }
        
        if (userName != null) {
            tvCustomerName.setText(userName.toUpperCase());
        }
        
        // Format and display amount
        if (amount != null) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvAmount.setText(formatter.format(Double.parseDouble(amount)) + " VND");
        }
    }
    
    private void setupClickListeners() {
        // Header buttons
        btnBack.setOnClickListener(v -> onBackPressed());
        
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        // Account section toggle
        layoutAccountHeader.setOnClickListener(v -> toggleAccountSection());
        
        // Action buttons
        btnShare.setOnClickListener(v -> shareReceipt());
        btnSave.setOnClickListener(v -> saveReceiptAsImage());
    }
    
    /**
     * Toggle account section expand/collapse
     */
    private void toggleAccountSection() {
        isAccountExpanded = !isAccountExpanded;
        
        if (isAccountExpanded) {
            layoutAccountDetails.setVisibility(View.VISIBLE);
            rotateIcon(ivAccountExpand, 0, 180);
        } else {
            layoutAccountDetails.setVisibility(View.GONE);
            rotateIcon(ivAccountExpand, 180, 0);
        }
    }
    
    /**
     * Rotate icon animation
     */
    private void rotateIcon(ImageView imageView, float fromDegrees, float toDegrees) {
        RotateAnimation rotate = new RotateAnimation(
            fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        imageView.startAnimation(rotate);
    }
    
    /**
     * Generate transaction ID
     */
    private String generateTransactionId() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return "FT" + timestamp;
    }
    

    /**
     * Get current time formatted
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    /**
     * Share receipt
     */
    private void shareReceipt() {
        // Create bitmap from view
        View contentView = findViewById(android.R.id.content);
        Bitmap bitmap = createBitmapFromView(contentView);
        
        if (bitmap != null) {
            try {
                // Save to cache
                File cachePath = new File(getCacheDir(), "images");
                cachePath.mkdirs();
                
                File file = new File(cachePath, "receipt_" + System.currentTimeMillis() + ".png");
                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                
                // Share
                Uri contentUri = FileProvider.getUriForFile(this, 
                    getPackageName() + ".fileprovider", file);
                
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                startActivity(Intent.createChooser(shareIntent, "Chia sẻ hóa đơn"));
                
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi khi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Save receipt as image
     */
    private void saveReceiptAsImage() {
        View contentView = findViewById(android.R.id.content);
        Bitmap bitmap = createBitmapFromView(contentView);
        
        if (bitmap != null) {
            try {
                // Save to Pictures directory
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(picturesDir, "BillPayment");
                if (!appDir.exists()) {
                    appDir.mkdirs();
                }
                
                String fileName = "receipt_" + System.currentTimeMillis() + ".png";
                File file = new File(appDir, fileName);
                
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                
                Toast.makeText(this, "Đã lưu hóa đơn vào thư mục Pictures", Toast.LENGTH_LONG).show();
                
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Create bitmap from view
     */
    private Bitmap createBitmapFromView(View view) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public void onBackPressed() {
        // Navigate to home
        Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

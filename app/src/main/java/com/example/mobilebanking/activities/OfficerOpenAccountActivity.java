package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Officer Open Account Activity - Nhân viên mở tài khoản cho khách hàng
 * Mở thẳng MainRegistrationActivity để dùng lại luồng đăng ký với face verification của user
 * 
 * Luồng đăng ký:
 * 1. Basic Info (phone, email, password)
 * 2. QR Scan CCCD
 * 3. Front+Back CCCD capture
 * 4. Face Verification -> gọi API register-with-face
 * 
 * Sau khi đăng ký thành công:
 * - Nếu mở từ Officer: quay về OfficerHomeActivity (không lưu session user mới)
 * - Nếu mở từ User: quay về LoginActivity
 */
public class OfficerOpenAccountActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_OFFICER = "from_officer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Mở MainRegistrationActivity để dùng lại luồng đăng ký với face verification
        // Luồng này sẽ gọi API POST /auth/register-with-face
        Intent intent = new Intent(this, MainRegistrationActivity.class);
        // Truyền flag để biết đang mở từ Officer
        intent.putExtra(EXTRA_FROM_OFFICER, true);
        startActivity(intent);
        finish();
    }
}

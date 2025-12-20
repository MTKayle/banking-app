package com.example.mobilebanking.activities;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.utils.SessionManager;

/**
 * BaseActivity - Activity cơ sở cho tất cả các Activity trong app
 * 
 * Tự động xử lý:
 * 1. Kiểm tra session khi mở Activity (nếu tắt app thì phải login lại)
 * 2. Hiển thị popup yêu cầu đăng nhập lại khi session hết hạn
 */
public abstract class BaseActivity extends AppCompatActivity {
    
    private SessionManager sessionManager;
    private AlertDialog sessionExpiredDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Đánh dấu app đang foreground
        sessionManager.onAppForeground();
        
        // Kiểm tra session có hết hạn không (tắt app và mở lại)
        if (shouldCheckSession() && sessionManager.isSessionExpired()) {
            // Session hết hạn → Hiển thị popup
            showSessionExpiredDialog();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Đánh dấu app đang background
        sessionManager.onAppBackground();
        
        // Đóng dialog nếu đang hiển thị
        if (sessionExpiredDialog != null && sessionExpiredDialog.isShowing()) {
            sessionExpiredDialog.dismiss();
        }
    }
    
    /**
     * Hiển thị popup thông báo session hết hạn
     */
    private void showSessionExpiredDialog() {
        // Nếu dialog đã đang hiển thị, không tạo mới
        if (sessionExpiredDialog != null && sessionExpiredDialog.isShowing()) {
            return;
        }
        
        sessionExpiredDialog = new AlertDialog.Builder(this)
            .setTitle("Phiên Làm Việc Hết Hạn")
            .setMessage("Vui lòng đăng nhập lại để tiếp tục sử dụng.")
            .setCancelable(false) // Không cho phép đóng bằng cách nhấn ngoài dialog
            .setPositiveButton("Đăng Nhập Lại", (dialog, which) -> {
                sessionManager.logout(this);
            })
            .create();
        
        sessionExpiredDialog.show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Đóng dialog nếu đang hiển thị
        if (sessionExpiredDialog != null && sessionExpiredDialog.isShowing()) {
            sessionExpiredDialog.dismiss();
        }
    }
    
    /**
     * Override method này để bỏ qua kiểm tra session cho một số Activity đặc biệt
     * Ví dụ: LoginActivity không cần kiểm tra session
     */
    protected boolean shouldCheckSession() {
        return true;
    }
}

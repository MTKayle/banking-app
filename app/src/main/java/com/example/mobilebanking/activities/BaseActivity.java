package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.utils.SessionManager;

/**
 * BaseActivity - Activity cơ sở cho tất cả các Activity trong app
 * 
 * Tự động xử lý:
 * 1. Kiểm tra session timeout khi mở Activity
 * 2. Theo dõi user interaction để reset timeout timer
 * 3. Hiển thị popup yêu cầu đăng nhập lại khi session hết hạn
 * 4. Chặn mọi tương tác khi session hết hạn cho đến khi người dùng xác nhận
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
        
        // Kiểm tra session có hết hạn không
        if (shouldCheckSession() && sessionManager.isSessionExpired()) {
            // Session hết hạn → Hiển thị popup
            showSessionExpiredDialog();
            return;
        }
        
        // Cập nhật thời gian activity
        sessionManager.updateLastActivityTime();
        
        // Bắt đầu timeout timer (5 phút)
        sessionManager.startTimeoutTimer(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Đánh dấu app đang background
        sessionManager.onAppBackground();
        
        // Dừng timeout timer
        sessionManager.stopTimeoutTimer();
        
        // Đóng dialog nếu đang hiển thị
        if (sessionExpiredDialog != null && sessionExpiredDialog.isShowing()) {
            sessionExpiredDialog.dismiss();
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Kiểm tra session có hết hạn không
        if (shouldCheckSession() && sessionManager.isSessionExpired()) {
            // Session hết hạn → Hiển thị popup và chặn tương tác
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                showSessionExpiredDialog();
            }
            return true; // Chặn event, không cho tương tác
        }
        
        // Mỗi khi user chạm vào màn hình → Reset timeout timer
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            sessionManager.updateLastActivityTime();
            sessionManager.startTimeoutTimer(this);
        }
        return super.dispatchTouchEvent(ev);
    }
    
    /**
     * Hiển thị popup thông báo session hết hạn
     */
    private void showSessionExpiredDialog() {
        // Nếu dialog đã đang hiển thị, không tạo mới
        if (sessionExpiredDialog != null && sessionExpiredDialog.isShowing()) {
            return;
        }
        
        // Đánh dấu dialog đang hiển thị
        sessionManager.setSessionExpiredDialogShowing(true);
        
        sessionExpiredDialog = new AlertDialog.Builder(this)
            .setTitle("Phiên Làm Việc Hết Hạn")
            .setMessage("Phiên làm việc của bạn đã hết hạn vì lý do bảo mật. Vui lòng đăng nhập lại để tiếp tục sử dụng.")
            .setCancelable(false) // Không cho phép đóng bằng cách nhấn ngoài dialog
            .setPositiveButton("Đăng Nhập Lại", (dialog, which) -> {
                sessionManager.setSessionExpiredDialogShowing(false);
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

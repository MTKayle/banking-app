package com.example.mobilebanking.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * OfficerUserUpdateActivity - Màn hình cập nhật thông tin user cho Officer
 * Cho phép cập nhật: Số điện thoại, Email, Số CCCD, Ngày sinh
 */
public class OfficerUserUpdateActivity extends BaseActivity {
    
    private MaterialToolbar toolbar;
    private TextView tvUserName, tvUserId;
    private TextInputEditText etPhone, etEmail, etCccd, etDateOfBirth;
    private Button btnConfirmUpdate;
    
    private Long userId;
    private String originalPhone, originalEmail, originalCccd, originalDob;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_user_update);
        
        initViews();
        setupToolbar();
        loadData();
        setupDatePicker();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserId = findViewById(R.id.tv_user_id);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etCccd = findViewById(R.id.et_cccd);
        etDateOfBirth = findViewById(R.id.et_date_of_birth);
        btnConfirmUpdate = findViewById(R.id.btn_confirm_update);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadData() {
        // Lấy data từ Intent
        userId = getIntent().getLongExtra("user_id", 0L);
        String fullName = getIntent().getStringExtra("user_name");
        originalPhone = getIntent().getStringExtra("user_phone");
        originalEmail = getIntent().getStringExtra("user_email");
        originalCccd = getIntent().getStringExtra("user_cccd");
        originalDob = getIntent().getStringExtra("user_dob");
        
        // Display data
        if (tvUserName != null && fullName != null) {
            tvUserName.setText(fullName);
        }
        
        if (tvUserId != null) {
            tvUserId.setText("Mã người dùng: #" + userId);
        }
        
        // Fill form với data hiện tại
        if (etPhone != null && originalPhone != null) {
            etPhone.setText(originalPhone);
        }
        
        if (etEmail != null && originalEmail != null) {
            etEmail.setText(originalEmail);
        }
        
        if (etCccd != null && originalCccd != null && !originalCccd.isEmpty()) {
            etCccd.setText(originalCccd);
        }
        
        if (etDateOfBirth != null && originalDob != null && !originalDob.isEmpty()) {
            etDateOfBirth.setText(originalDob);
        }
    }
    
    private void setupDatePicker() {
        if (etDateOfBirth != null) {
            etDateOfBirth.setOnClickListener(v -> showDatePicker());
        }
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        // Parse ngày sinh hiện tại nếu có
        if (originalDob != null && !originalDob.isEmpty()) {
            try {
                String[] parts = originalDob.split("-");
                if (parts.length == 3) {
                    calendar.set(Integer.parseInt(parts[0]), 
                               Integer.parseInt(parts[1]) - 1, 
                               Integer.parseInt(parts[2]));
                }
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                if (etDateOfBirth != null) {
                    etDateOfBirth.setText(selectedDate);
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set max date to today (không cho chọn ngày tương lai)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        
        // Set min date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }
    
    private void setupButton() {
        btnConfirmUpdate.setOnClickListener(v -> {
            if (validateInput()) {
                showConfirmDialog();
            }
        });
    }
    
    private boolean validateInput() {
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String cccd = etCccd.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        
        // Validate Phone
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return false;
        }
        
        if (!isValidPhone(phone)) {
            etPhone.setError("Số điện thoại không hợp lệ (10-11 số)");
            etPhone.requestFocus();
            return false;
        }
        
        // Validate Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }
        
        if (!isValidEmail(email)) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }
        
        // Validate CCCD (optional but if provided, must be valid)
        if (!TextUtils.isEmpty(cccd) && (cccd.length() < 9 || cccd.length() > 12)) {
            etCccd.setError("Số CCCD phải có 9-12 chữ số");
            etCccd.requestFocus();
            return false;
        }
        
        // Validate Date of Birth
        if (TextUtils.isEmpty(dob)) {
            etDateOfBirth.setError("Vui lòng chọn ngày sinh");
            etDateOfBirth.requestFocus();
            return false;
        }
        
        if (!isValidDate(dob)) {
            etDateOfBirth.setError("Ngày sinh không hợp lệ (YYYY-MM-DD)");
            etDateOfBirth.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean isValidPhone(String phone) {
        // Vietnamese phone: 10-11 digits, may start with 0 or +84
        Pattern pattern = Pattern.compile("^(0|\\+84)[0-9]{9,10}$");
        return pattern.matcher(phone.replaceAll("\\s+", "")).matches();
    }
    
    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return pattern.matcher(email).matches();
    }
    
    private boolean isValidDate(String date) {
        // Format: YYYY-MM-DD
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        if (!pattern.matcher(date).matches()) {
            return false;
        }
        
        try {
            String[] parts = date.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            
            // Basic validation
            if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                return false;
            }
            if (month < 1 || month > 12) {
                return false;
            }
            if (day < 1 || day > 31) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void showConfirmDialog() {
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String cccd = etCccd.getText().toString().trim();
        String dob = etDateOfBirth.getText().toString().trim();
        
        StringBuilder changes = new StringBuilder();
        changes.append("Thông tin sẽ được cập nhật:\n\n");
        
        if (!phone.equals(originalPhone)) {
            changes.append("• Số điện thoại: ").append(originalPhone).append(" → ").append(phone).append("\n");
        }
        if (!email.equals(originalEmail)) {
            changes.append("• Email: ").append(originalEmail).append(" → ").append(email).append("\n");
        }
        if (!cccd.equals(originalCccd != null ? originalCccd : "")) {
            changes.append("• Số CCCD: ").append(originalCccd != null && !originalCccd.isEmpty() ? originalCccd : "Chưa có")
                   .append(" → ").append(cccd).append("\n");
        }
        if (!dob.equals(originalDob != null ? originalDob : "")) {
            changes.append("• Ngày sinh: ").append(originalDob != null && !originalDob.isEmpty() ? originalDob : "Chưa có")
                   .append(" → ").append(dob).append("\n");
        }
        
        if (changes.toString().equals("Thông tin sẽ được cập nhật:\n\n")) {
            Toast.makeText(this, "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Xác nhận cập nhật")
            .setMessage(changes.toString())
            .setPositiveButton("Xác nhận", (dialog, which) -> {
                performUpdate(phone, email, cccd, dob);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void performUpdate(String phone, String email, String cccd, String dob) {
        // TODO: Call API to update user info
        // Hiện tại chỉ mock - hiển thị toast thành công
        
        Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
        
        // Trả về result để refresh detail screen
        setResult(RESULT_OK);
        finish();
    }
}


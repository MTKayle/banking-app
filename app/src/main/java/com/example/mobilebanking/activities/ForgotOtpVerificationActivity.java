package com.example.mobilebanking.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * OTP verification screen for Forgot Password flow.
 * Hiện tại chỉ xử lý UI, không gọi backend verify thật.
 */
public class ForgotOtpVerificationActivity extends AppCompatActivity {

    // Đếm ngược 15 giây cho nút \"Gửi lại OTP\"
    private static final long COUNTDOWN_MILLIS = 15_000;

    private TextView tvPhoneDisplay;
    private TextView tvError;
    private TextView tvTimer;
    private ProgressBar progressBar;
    private MaterialButton btnVerify;
    private MaterialButton btnResendOtp;

    private TextInputEditText et1, et2, et3, et4, et5, et6;

    private CountDownTimer countDownTimer;
    private String phone;
    private String expectedOtp; // Mã OTP thật từ SMS (lưu trong memory, không lưu vào SharedPreferences)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification_forgot);

        phone = getIntent().getStringExtra("phone");

        initViews();
        setupListeners();
        startCountdown();
        
        // Hiện dialog sau khi đã init views để nhập mã OTP từ SMS
        showOtpInputDialog();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        tvError = findViewById(R.id.tv_error);
        tvTimer = findViewById(R.id.tv_timer);
        progressBar = findViewById(R.id.progress_bar);
        btnVerify = findViewById(R.id.btn_verify);
        btnResendOtp = findViewById(R.id.btn_resend_otp);

        et1 = findViewById(R.id.et_otp_1);
        et2 = findViewById(R.id.et_otp_2);
        et3 = findViewById(R.id.et_otp_3);
        et4 = findViewById(R.id.et_otp_4);
        et5 = findViewById(R.id.et_otp_5);
        et6 = findViewById(R.id.et_otp_6);

        btnBack.setOnClickListener(v -> onBackPressed());

        if (!TextUtils.isEmpty(phone)) {
            String display = getString(R.string.sent_to_phone) + " " + phone;
            tvPhoneDisplay.setText(display);
        }
    }

    private void setupListeners() {
        // Thêm TextWatcher cho từng EditText để so sánh real-time
        setupOtpTextWatchers();

        btnVerify.setOnClickListener(v -> {
            verifyOtp();
        });

        btnResendOtp.setOnClickListener(v -> {
            // Tạm thời chỉ reset countdown. Khi backend sẵn sàng, sẽ gọi lại API gửi OTP.
            Toast.makeText(this, getString(R.string.resend_otp), Toast.LENGTH_SHORT).show();
            startCountdown();
        });
    }

    private void setupOtpTextWatchers() {
        TextInputEditText[] editTexts = {et1, et2, et3, et4, et5, et6};
        
        for (int i = 0; i < editTexts.length; i++) {
            final int index = i;
            final TextInputEditText currentEdit = editTexts[i];
            final TextInputEditText nextEdit = (i < editTexts.length - 1) ? editTexts[i + 1] : null;
            
            currentEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = s.toString();
                    
                    // Chỉ cho phép nhập 1 số
                    if (text.length() > 1) {
                        currentEdit.setText(text.substring(0, 1));
                        currentEdit.setSelection(1);
                        return;
                    }
                    
                    // Xóa lỗi cũ khi bắt đầu nhập lại
                    hideError();
                    
                    // So sánh real-time với mã OTP thật (chỉ khi đã có expectedOtp)
                    if (!TextUtils.isEmpty(expectedOtp) && expectedOtp.length() == 6) {
                        if (text.length() == 1) {
                            String expectedChar = String.valueOf(expectedOtp.charAt(index));
                            if (!text.equals(expectedChar)) {
                                // Số nhập vào không khớp với số trong mã OTP
                                currentEdit.setError("Số này không đúng");
                            } else {
                                // Số đúng, xóa lỗi
                                currentEdit.setError(null);
                            }
                        }
                    }
                    
                    // Tự động chuyển sang ô tiếp theo khi nhập xong
                    if (text.length() == 1 && nextEdit != null) {
                        nextEdit.requestFocus();
                    }
                    
                    // Khi nhập đủ 6 số, tự động verify
                    if (index == 5 && text.length() == 1) {
                        // Đợi một chút để đảm bảo text đã được set vào tất cả các ô
                        currentEdit.postDelayed(() -> {
                            String collectedOtp = collectOtp();
                            if (collectedOtp.length() == 6) {
                                verifyOtp();
                            }
                        }, 200);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void verifyOtp() {
        String otp = collectOtp();
        
        // Debug log
        android.util.Log.d("OTP_VERIFY", "Collected OTP: [" + otp + "], Length: " + otp.length());
        android.util.Log.d("OTP_VERIFY", "Expected OTP: [" + expectedOtp + "]");
        
        if (otp.length() != 6) {
            showError("Vui lòng nhập đủ 6 số OTP");
            return;
        }

        // Kiểm tra OTP: So sánh với mã OTP thật từ SMS
        if (TextUtils.isEmpty(expectedOtp) || expectedOtp.length() != 6) {
            showError("Chưa có mã OTP để xác thực. Vui lòng nhập mã OTP từ SMS.");
            return;
        }

        // So sánh chính xác từng ký tự
        if (otp.equals(expectedOtp)) {
            // OTP đúng - xóa tất cả lỗi và hiển thị thành công
            hideError();
            clearAllErrors();
            Toast.makeText(this, "Xác thực OTP thành công", Toast.LENGTH_SHORT).show();
            
            // TODO: Khi có backend, sẽ gọi API verify OTP ở đây
            // Nếu verify thành công thì chuyển sang màn Reset Password
            // Intent intent = new Intent(this, ResetPasswordActivity.class);
            // intent.putExtra("phone", phone);
            // startActivity(intent);
        } else {
            // OTP sai - hiển thị lỗi và xóa các ô để nhập lại
            showError(getString(R.string.invalid_otp));
            clearOtpInputs();
            clearAllErrors();
        }
    }
    
    private void clearAllErrors() {
        if (et1 != null) et1.setError(null);
        if (et2 != null) et2.setError(null);
        if (et3 != null) et3.setError(null);
        if (et4 != null) et4.setError(null);
        if (et5 != null) et5.setError(null);
        if (et6 != null) et6.setError(null);
    }

    private String collectOtp() {
        StringBuilder sb = new StringBuilder(6);
        String s1 = getTextFromEditText(et1);
        String s2 = getTextFromEditText(et2);
        String s3 = getTextFromEditText(et3);
        String s4 = getTextFromEditText(et4);
        String s5 = getTextFromEditText(et5);
        String s6 = getTextFromEditText(et6);
        
        sb.append(s1 != null ? s1 : "");
        sb.append(s2 != null ? s2 : "");
        sb.append(s3 != null ? s3 : "");
        sb.append(s4 != null ? s4 : "");
        sb.append(s5 != null ? s5 : "");
        sb.append(s6 != null ? s6 : "");
        
        return sb.toString();
    }

    private String getTextFromEditText(TextInputEditText editText) {
        if (editText == null) {
            return null;
        }
        CharSequence text = editText.getText();
        if (text == null) {
            return null;
        }
        String result = text.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnResendOtp.setEnabled(false);

        countDownTimer = new CountDownTimer(COUNTDOWN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText(getString(R.string.resend_timer, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("");
                btnResendOtp.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    private void clearOtpInputs() {
        // Xóa text trong tất cả các ô
        if (et1 != null) {
            et1.setText("");
            et1.setError(null);
        }
        if (et2 != null) {
            et2.setText("");
            et2.setError(null);
        }
        if (et3 != null) {
            et3.setText("");
            et3.setError(null);
        }
        if (et4 != null) {
            et4.setText("");
            et4.setError(null);
        }
        if (et5 != null) {
            et5.setText("");
            et5.setError(null);
        }
        if (et6 != null) {
            et6.setText("");
            et6.setError(null);
        }
        // Focus vào ô đầu tiên
        if (et1 != null) {
            et1.requestFocus();
        }
    }

    /**
     * Hiện dialog để nhập mã OTP từ SMS (chỉ để test, không dùng trong production)
     * LƯU Ý: Trong production, mã OTP sẽ được verify qua backend API, không cần dialog này
     */
    private void showOtpInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập mã OTP từ SMS");
        builder.setMessage("Vui lòng nhập mã OTP 6 số bạn nhận được từ SMS để xác thực:");

        // Tạo EditText để nhập OTP
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setMaxLines(1);
        input.setHint("Nhập 6 số OTP từ SMS");
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String otp = input.getText().toString().trim();
            if (otp.length() == 6 && otp.matches("\\d{6}")) {
                expectedOtp = otp; // Lưu mã OTP vào biến trong memory
                android.util.Log.d("OTP_DIALOG", "Expected OTP saved: " + expectedOtp);
                Toast.makeText(this, "Đã lưu mã OTP. Bây giờ bạn có thể nhập mã vào các ô để xác thực.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Mã OTP phải có đúng 6 chữ số", Toast.LENGTH_SHORT).show();
                showOtpInputDialog(); // Hiện lại dialog
            }
        });

        builder.setNegativeButton("Bỏ qua", (dialog, which) -> {
            dialog.cancel();
            // Nếu bỏ qua, sẽ không có mã OTP để verify
            Toast.makeText(this, "Vui lòng nhập mã OTP từ SMS để tiếp tục", Toast.LENGTH_SHORT).show();
        });

        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}



package com.example.mobilebanking.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.AuthApiService;
import com.example.mobilebanking.api.dto.PhoneExistsResponse;
import com.example.mobilebanking.models.RegistrationData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Step 1: Basic Information (Phone, Email, Password)
 */
public class Step1BasicInfoFragment extends Fragment {
    private static final String TAG = "Step1BasicInfo";
    
    private RegistrationData registrationData;
    
    private TextInputLayout tilPhone, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etPhone, etEmail, etPassword, etConfirmPassword;
    private Button btnContinue;
    private ProgressDialog progressDialog;
    
    public static Step1BasicInfoFragment newInstance(RegistrationData data) {
        Step1BasicInfoFragment fragment = new Step1BasicInfoFragment();
        fragment.registrationData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step1_basic_info, container, false);
        
        // Ensure registrationData is not null
        if (registrationData == null) {
            if (getActivity() instanceof MainRegistrationActivity) {
                registrationData = ((MainRegistrationActivity) getActivity()).getRegistrationData();
            }
            if (registrationData == null) {
                registrationData = new RegistrationData();
            }
        }
        
        initializeViews(view);
        setupListeners();
        loadData();
        
        return view;
    }
    
    private void initializeViews(View view) {
        tilPhone = view.findViewById(R.id.til_phone);
        tilEmail = view.findViewById(R.id.til_email);
        tilPassword = view.findViewById(R.id.til_password);
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password);
        
        etPhone = view.findViewById(R.id.et_phone);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        
        btnContinue = view.findViewById(R.id.btn_continue);
    }
    
    private void setupListeners() {
        btnContinue.setOnClickListener(v -> validateAndContinue());

        // Khi người dùng bắt đầu nhập, tự động xoá thông báo lỗi + dấu chấm than
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPhone.setError(null);
                tilPhone.setErrorEnabled(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                tilEmail.setErrorEnabled(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                tilPassword.setErrorEnabled(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilConfirmPassword.setError(null);
                tilConfirmPassword.setErrorEnabled(false);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadData() {
        // Ensure registrationData is not null
        if (registrationData == null) {
            if (getActivity() instanceof MainRegistrationActivity) {
                registrationData = ((MainRegistrationActivity) getActivity()).getRegistrationData();
            }
            if (registrationData == null) {
                registrationData = new RegistrationData();
            }
        }
        
        // Load saved data if exists
        if (registrationData != null) {
            if (registrationData.getPhoneNumber() != null) {
                etPhone.setText(registrationData.getPhoneNumber());
            }
            if (registrationData.getEmail() != null) {
                etEmail.setText(registrationData.getEmail());
            }
        }
    }
    
    private void validateAndContinue() {
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        
        // Clear previous errors
        tilPhone.setError(null);           tilPhone.setErrorEnabled(false);
        tilEmail.setError(null);           tilEmail.setErrorEnabled(false);
        tilPassword.setError(null);        tilPassword.setErrorEnabled(false);
        tilConfirmPassword.setError(null); tilConfirmPassword.setErrorEnabled(false);
        
        boolean isValid = true;
        
        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!phone.startsWith("0")) {
            tilPhone.setError("Số điện thoại phải bắt đầu bằng 0");
            isValid = false;
        } else if (phone.length() != 10) {
            tilPhone.setError("Số điện thoại phải có 10 chữ số");
            isValid = false;
        } else if (!phone.matches("^0[0-9]{9}$")) {
            tilPhone.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Địa chỉ email không hợp lệ");
            isValid = false;
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }
        
        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            isValid = false;
        }
        
        if (isValid) {
            // Kiểm tra số điện thoại đã tồn tại chưa
            checkPhoneExistsAndContinue(phone, email, password, confirmPassword);
        }
    }
    
    /**
     * Kiểm tra số điện thoại đã tồn tại chưa trước khi tiếp tục
     */
    private void checkPhoneExistsAndContinue(String phone, String email, String password, String confirmPassword) {
        // Show loading
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang kiểm tra số điện thoại...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        AuthApiService authApiService = ApiClient.getAuthApiService();
        Call<PhoneExistsResponse> call = authApiService.checkPhoneExists(phone);
        
        call.enqueue(new Callback<PhoneExistsResponse>() {
            @Override
            public void onResponse(Call<PhoneExistsResponse> call, Response<PhoneExistsResponse> response) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    PhoneExistsResponse result = response.body();
                    
                    // Nếu exists = true → Số điện thoại đã tồn tại → KHÔNG cho phép tiếp tục
                    if (result.isExists()) {
                        tilPhone.setError("Số điện thoại này đã được đăng ký");
                        Toast.makeText(getContext(), "Số điện thoại đã tồn tại. Vui lòng sử dụng số khác.", Toast.LENGTH_LONG).show();
                        // KHÔNG gọi saveDataAndContinue() - dừng lại ở đây
                    } else {
                        // Số điện thoại chưa tồn tại (exists = false) → OK, tiếp tục
                        saveDataAndContinue(phone, email, password, confirmPassword);
                    }
                } else {
                    // Lỗi từ server - KHÔNG cho phép tiếp tục
                    Log.e(TAG, "Check phone exists failed: " + response.code());
                    Toast.makeText(getContext(), "Không thể kiểm tra số điện thoại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<PhoneExistsResponse> call, Throwable t) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                
                Log.e(TAG, "Check phone exists error", t);
                Toast.makeText(getContext(), "Lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Lưu dữ liệu và chuyển sang trang xác thực OTP
     */
    private void saveDataAndContinue(String phone, String email, String password, String confirmPassword) {
        // Ensure registrationData is not null
        if (registrationData == null) {
            if (getActivity() instanceof MainRegistrationActivity) {
                registrationData = ((MainRegistrationActivity) getActivity()).getRegistrationData();
            }
            if (registrationData == null) {
                registrationData = new RegistrationData();
            }
        }
        
        // Save data
        registrationData.setPhoneNumber(phone);
        registrationData.setEmail(email);
        registrationData.setPassword(password);
        registrationData.setConfirmPassword(confirmPassword);
        
        // Navigate to OTP Verification Activity
        android.content.Intent intent = new android.content.Intent(getActivity(), com.example.mobilebanking.activities.OtpVerificationActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("flow", "register");
        startActivityForResult(intent, 100); // Request code 100 for registration OTP
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100) { // Registration OTP verification
            if (resultCode == android.app.Activity.RESULT_OK) {
                // OTP verified successfully, navigate to next step (Step 2: QR Scan)
                if (getActivity() instanceof MainRegistrationActivity) {
                    ((MainRegistrationActivity) getActivity()).goToNextStep();
                }
            } else {
                // OTP verification failed or cancelled
                Toast.makeText(getContext(), "Xác thực OTP thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Dismiss progress dialog if showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}


package com.example.mobilebanking.fragments;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.mobilebanking.models.RegistrationData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Step 1: Basic Information (Phone, Email, Password)
 */
public class Step1BasicInfoFragment extends Fragment {
    private RegistrationData registrationData;
    
    private TextInputLayout tilPhone, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etPhone, etEmail, etPassword, etConfirmPassword;
    private Button btnContinue;
    
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
        tilPhone.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        
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
            
            // Navigate to next step
            if (getActivity() instanceof MainRegistrationActivity) {
                ((MainRegistrationActivity) getActivity()).goToNextStep();
            }
        }
    }
}


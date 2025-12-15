package com.example.mobilebanking.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.MainRegistrationActivity;
import com.example.mobilebanking.activities.QrScannerActivity;
import com.example.mobilebanking.models.RegistrationData;
import com.example.mobilebanking.utils.CccdQrParser;

/**
 * Step 2: QR Code Scanning and Confirmation
 */
public class Step2QrScanFragment extends Fragment {
    private static final int QR_SCANNER_REQUEST_CODE = 200;
    
    private RegistrationData registrationData;
    
    private Button btnScanQr, btnConfirm;
    private LinearLayout llInfoContainer;
    private TextView tvFullName, tvIdNumber, tvDateOfBirth, tvGender, tvAddress, tvIssueDate;
    private TextView tvPhone, tvEmail; // Read-only display
    
    public static Step2QrScanFragment newInstance(RegistrationData data) {
        Step2QrScanFragment fragment = new Step2QrScanFragment();
        fragment.registrationData = data;
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step2_qr_scan, container, false);
        
        // Ensure registrationData is not null
        ensureRegistrationData();
        
        initializeViews(view);
        setupListeners();
        loadData();
        
        return view;
    }
    
    private void ensureRegistrationData() {
        if (registrationData == null) {
            if (getActivity() instanceof MainRegistrationActivity) {
                registrationData = ((MainRegistrationActivity) getActivity()).getRegistrationData();
            }
            if (registrationData == null) {
                registrationData = new RegistrationData();
            }
        }
    }
    
    private void initializeViews(View view) {
        btnScanQr = view.findViewById(R.id.btn_scan_qr);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        llInfoContainer = view.findViewById(R.id.ll_info_container);
        
        tvPhone = view.findViewById(R.id.tv_phone);
        tvEmail = view.findViewById(R.id.tv_email);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvIdNumber = view.findViewById(R.id.tv_id_number);
        tvDateOfBirth = view.findViewById(R.id.tv_date_of_birth);
        tvGender = view.findViewById(R.id.tv_gender);
        tvAddress = view.findViewById(R.id.tv_address);
        tvIssueDate = view.findViewById(R.id.tv_issue_date);
    }
    
    private void setupListeners() {
        btnScanQr.setOnClickListener(v -> openQrScanner());
        btnConfirm.setOnClickListener(v -> confirmAndContinue());
    }
    
    private void loadData() {
        ensureRegistrationData();
        
        // Display phone and email from step 1 (read-only)
        if (registrationData != null) {
            if (registrationData.getPhoneNumber() != null) {
                tvPhone.setText("+84" + registrationData.getPhoneNumber());
            }
            if (registrationData.getEmail() != null) {
                tvEmail.setText(registrationData.getEmail());
            }
            
            // Display CCCD data if already scanned
            if (registrationData.isStep2Complete()) {
                displayCccdData();
            }
        }
    }
    
    private void openQrScanner() {
        Intent intent = new Intent(getActivity(), QrScannerActivity.class);
        startActivityForResult(intent, QR_SCANNER_REQUEST_CODE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == QR_SCANNER_REQUEST_CODE) {
            if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                String qrData = data.getStringExtra("qr_data");
                if (qrData != null && !qrData.isEmpty()) {
                    processQrData(qrData);
                } else {
                    Toast.makeText(getActivity(), "Không thể đọc mã QR", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void processQrData(String qrData) {
        CccdQrParser.CccdData cccdData = CccdQrParser.parseQrData(qrData);
        
        if (cccdData == null) {
            Toast.makeText(getActivity(), "Không thể đọc thông tin từ mã QR", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save data
        registrationData.setFullName(cccdData.getFullName());
        registrationData.setIdNumber(cccdData.getIdNumber());
        registrationData.setDateOfBirth(cccdData.getDateOfBirth());
        registrationData.setGender(cccdData.getGender());
        registrationData.setPermanentAddress(cccdData.getPermanentAddress());
        registrationData.setIssueDate(cccdData.getIssueDate());
        
        // Display data
        displayCccdData();
        Toast.makeText(getActivity(), "Đã lấy thông tin từ CCCD thành công", Toast.LENGTH_SHORT).show();
    }
    
    private void displayCccdData() {
        llInfoContainer.setVisibility(View.VISIBLE);
        btnScanQr.setVisibility(View.GONE);
        btnConfirm.setVisibility(View.VISIBLE);
        
        tvFullName.setText(registrationData.getFullName() != null ? registrationData.getFullName() : "Chưa có");
        tvIdNumber.setText(registrationData.getIdNumber() != null ? registrationData.getIdNumber() : "Chưa có");
        tvDateOfBirth.setText(registrationData.getDateOfBirth() != null ? registrationData.getDateOfBirth() : "Chưa có");
        tvGender.setText(registrationData.getGender() != null ? registrationData.getGender() : "Chưa có");
        tvAddress.setText(registrationData.getPermanentAddress() != null ? registrationData.getPermanentAddress() : "Chưa có");
        tvIssueDate.setText(registrationData.getIssueDate() != null ? registrationData.getIssueDate() : "Chưa có");
    }
    
    private void confirmAndContinue() {
        if (!registrationData.isStep2Complete()) {
            Toast.makeText(getActivity(), "Vui lòng quét mã QR CCCD trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to next step
        if (getActivity() instanceof MainRegistrationActivity) {
            ((MainRegistrationActivity) getActivity()).goToNextStep();
        }
    }
}


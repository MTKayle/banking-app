package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MortgageApiService;
import com.example.mobilebanking.api.dto.MortgageAccountResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfficerMortgageCreateActivity extends BaseActivity {
    
    private static final String TAG = "OfficerMortgageCreate";
    
    private MaterialToolbar toolbar;
    private TextInputEditText etPhone, etDescription;
    private Spinner spinnerCollateralType, spinnerPaymentFrequency;
    private MaterialButton btnCreate;
    private ProgressBar progressBar;
    
    private MortgageApiService mortgageApiService;
    private List<CollateralItem> collateralTypes = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_mortgage_create);
        
        mortgageApiService = ApiClient.getMortgageApiService();
        
        initViews();
        setupToolbar();
        setupPaymentFrequencySpinner();
        loadCollateralTypes();
        setupButton();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etPhone = findViewById(R.id.et_phone);
        etDescription = findViewById(R.id.et_description);
        spinnerCollateralType = findViewById(R.id.spinner_collateral_type);
        spinnerPaymentFrequency = findViewById(R.id.spinner_payment_frequency);
        btnCreate = findViewById(R.id.btn_create);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    /**
     * Setup payment frequency spinner with MONTHLY and BI_WEEKLY
     */
    private void setupPaymentFrequencySpinner() {
        if (spinnerPaymentFrequency != null) {
            List<PaymentFrequencyItem> frequencies = new ArrayList<>();
            frequencies.add(new PaymentFrequencyItem("MONTHLY", "Hàng tháng"));
            frequencies.add(new PaymentFrequencyItem("BI_WEEKLY", "2 tuần/lần"));
            
            List<String> displayNames = new ArrayList<>();
            for (PaymentFrequencyItem item : frequencies) {
                displayNames.add(item.displayName);
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, displayNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPaymentFrequency.setAdapter(adapter);
            spinnerPaymentFrequency.setTag(frequencies);
        }
    }
    
    /**
     * Load collateral types from API
     * Endpoint: GET /mortgage/collateral-types
     */
    private void loadCollateralTypes() {
        showLoading(true);
        
        Call<Map<String, Object>> call = mortgageApiService.getCollateralTypes();
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    
                    if (body.containsKey("data")) {
                        List<Map<String, String>> dataList = (List<Map<String, String>>) body.get("data");
                        collateralTypes.clear();
                        
                        List<String> displayNames = new ArrayList<>();
                        for (Map<String, String> item : dataList) {
                            String value = item.get("value");
                            String displayName = item.get("displayName");
                            collateralTypes.add(new CollateralItem(value, displayName));
                            displayNames.add(displayName);
                        }
                        
                        if (spinnerCollateralType != null && !displayNames.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                OfficerMortgageCreateActivity.this,
                                android.R.layout.simple_spinner_item, 
                                displayNames
                            );
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerCollateralType.setAdapter(adapter);
                        }
                        
                        Log.d(TAG, "Loaded " + collateralTypes.size() + " collateral types");
                    }
                } else {
                    Log.e(TAG, "Load collateral types error: " + response.code());
                    setupDefaultCollateralTypes();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Load collateral types failed: " + t.getMessage(), t);
                setupDefaultCollateralTypes();
            }
        });
    }
    
    /**
     * Setup default collateral types if API fails
     */
    private void setupDefaultCollateralTypes() {
        collateralTypes.clear();
        collateralTypes.add(new CollateralItem("HOUSE", "Nhà ở"));
        collateralTypes.add(new CollateralItem("LAND", "Đất"));
        collateralTypes.add(new CollateralItem("VEHICLE", "Xe"));
        collateralTypes.add(new CollateralItem("OTHER", "Khác"));
        
        if (spinnerCollateralType != null) {
            List<String> displayNames = new ArrayList<>();
            for (CollateralItem item : collateralTypes) {
                displayNames.add(item.displayName);
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, displayNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCollateralType.setAdapter(adapter);
        }
    }
    
    private void setupButton() {
        btnCreate.setOnClickListener(v -> {
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            
            if (phone.isEmpty()) {
                etPhone.setError("Vui lòng nhập số điện thoại khách hàng");
                etPhone.requestFocus();
                return;
            }
            
            if (phone.length() < 10) {
                etPhone.setError("Số điện thoại không hợp lệ");
                etPhone.requestFocus();
                return;
            }
            
            // Get selected collateral type
            String collateralType = "HOUSE"; // default
            if (spinnerCollateralType != null && spinnerCollateralType.getSelectedItemPosition() >= 0 
                && spinnerCollateralType.getSelectedItemPosition() < collateralTypes.size()) {
                collateralType = collateralTypes.get(spinnerCollateralType.getSelectedItemPosition()).value;
            }
            
            // Get selected payment frequency
            String paymentFrequency = "MONTHLY"; // default
            if (spinnerPaymentFrequency != null && spinnerPaymentFrequency.getTag() != null) {
                List<PaymentFrequencyItem> frequencies = (List<PaymentFrequencyItem>) spinnerPaymentFrequency.getTag();
                int pos = spinnerPaymentFrequency.getSelectedItemPosition();
                if (pos >= 0 && pos < frequencies.size()) {
                    paymentFrequency = frequencies.get(pos).value;
                }
            }
            
            // Show confirmation dialog
            String finalCollateralType = collateralType;
            String finalPaymentFrequency = paymentFrequency;
            
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận tạo khoản vay")
                .setMessage("Tạo khoản vay cho khách hàng có SĐT: " + phone + "?\n\n" +
                    "Tài sản thế chấp: " + getCollateralDisplayName(collateralType) + "\n" +
                    "Tần suất thanh toán: " + getFrequencyDisplayName(paymentFrequency))
                .setPositiveButton("Tạo", (dialog, which) -> {
                    createMortgage(phone, finalCollateralType, description, finalPaymentFrequency);
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
    
    private String getCollateralDisplayName(String value) {
        for (CollateralItem item : collateralTypes) {
            if (item.value.equals(value)) {
                return item.displayName;
            }
        }
        return value;
    }
    
    private String getFrequencyDisplayName(String value) {
        if ("MONTHLY".equals(value)) return "Hàng tháng";
        if ("BI_WEEKLY".equals(value)) return "2 tuần/lần";
        return value;
    }
    
    /**
     * Create mortgage via API
     * Endpoint: POST /mortgage/create
     * Request body: { phoneNumber, collateralType, collateralDescription, paymentFrequency }
     */
    private void createMortgage(String phone, String collateralType, String description, String paymentFrequency) {
        showLoading(true);
        btnCreate.setEnabled(false);
        
        // Build request JSON
        MortgageCreateRequestData requestData = new MortgageCreateRequestData();
        requestData.phoneNumber = phone;
        requestData.collateralType = collateralType;
        requestData.collateralDescription = description;
        requestData.paymentFrequency = paymentFrequency;
        
        String jsonRequest = new Gson().toJson(requestData);
        Log.d(TAG, "Create mortgage request: " + jsonRequest);
        
        RequestBody requestBody = RequestBody.create(
            MediaType.parse("application/json"), jsonRequest);
        
        Call<MortgageAccountResponse> call = mortgageApiService.createMortgageSimple(requestBody);
        call.enqueue(new Callback<MortgageAccountResponse>() {
            @Override
            public void onResponse(Call<MortgageAccountResponse> call, Response<MortgageAccountResponse> response) {
                showLoading(false);
                btnCreate.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    new AlertDialog.Builder(OfficerMortgageCreateActivity.this)
                        .setTitle("Thành công")
                        .setMessage("Đã tạo khoản vay thành công!\n\nKhoản vay đang ở trạng thái 'Chờ thỏa thuận'. Số tiền vay sẽ được xác định khi phê duyệt.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } else {
                    Log.e(TAG, "Create mortgage error: " + response.code());
                    String errorMsg = parseErrorMessage(response);
                    showErrorDialog(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<MortgageAccountResponse> call, Throwable t) {
                showLoading(false);
                btnCreate.setEnabled(true);
                Log.e(TAG, "Create mortgage failed: " + t.getMessage(), t);
                showErrorDialog("Không thể kết nối server. Vui lòng thử lại.");
            }
        });
    }
    
    /**
     * Parse error message from response
     */
    private String parseErrorMessage(Response<?> response) {
        String errorMsg = "Lỗi tạo khoản vay";
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "Error body: " + errorBody);
                
                // Try to parse JSON error
                try {
                    JsonObject jsonError = JsonParser.parseString(errorBody).getAsJsonObject();
                    if (jsonError.has("message")) {
                        errorMsg = jsonError.get("message").getAsString();
                    } else if (jsonError.has("error")) {
                        errorMsg = jsonError.get("error").getAsString();
                    }
                } catch (Exception e) {
                    // If not JSON, use raw error body
                    if (!errorBody.isEmpty() && errorBody.length() < 200) {
                        errorMsg = errorBody;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading error body", e);
        }
        return errorMsg;
    }
    
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
            .setTitle("Lỗi")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Helper class for collateral type items
     */
    static class CollateralItem {
        String value;
        String displayName;
        
        CollateralItem(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
    }
    
    /**
     * Helper class for payment frequency items
     */
    static class PaymentFrequencyItem {
        String value;
        String displayName;
        
        PaymentFrequencyItem(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
    }
    
    /**
     * Helper class for create mortgage request
     */
    static class MortgageCreateRequestData {
        String phoneNumber;
        String collateralType;
        String collateralDescription;
        String paymentFrequency;
    }
}

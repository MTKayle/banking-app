package com.example.mobilebanking.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.ui_home.UiHomeActivity;

public class VNPayWebViewActivity extends AppCompatActivity {
    
    private WebView webView;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_webview);
        
        webView = findViewById(R.id.webview_vnpay);
        progressBar = findViewById(R.id.progress_bar);
        
        // Get payment URL from intent
        String paymentUrl = getIntent().getStringExtra("PAYMENT_URL");
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Setup WebView
        setupWebView();
        
        // Load payment URL
        webView.loadUrl(paymentUrl);
    }
    
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                
                // Check if this is callback URL
                if (url.contains("/api/vnpay/callback")) {
                    handleCallback(url);
                }
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                
                // Check if this is callback URL
                if (url.contains("/api/vnpay/callback")) {
                    handleCallback(url);
                    return true;
                }
                
                return false;
            }
        });
    }
    
    private void handleCallback(String callbackUrl) {
        // Parse callback URL to get payment result
        Uri uri = Uri.parse(callbackUrl);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        String txnRef = uri.getQueryParameter("vnp_TxnRef");
        String amount = uri.getQueryParameter("vnp_Amount");
        String vnpSecureHash = uri.getQueryParameter("vnp_SecureHash");
        
        // Close WebView
        webView.stopLoading();
        
        if ("00".equals(responseCode)) {
            // Payment success - Call backend to verify and update balance
            verifyPaymentWithBackend(callbackUrl);
        } else {
            // Payment failed
            String errorMessage = getErrorMessage(responseCode);
            Toast.makeText(this, "Thanh toán thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            
            // Navigate back to home
            Intent intent = new Intent(this, UiHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
    
    /**
     * Call backend API to verify payment and update balance
     */
    private void verifyPaymentWithBackend(String callbackUrl) {
        // Show loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Đang xác nhận thanh toán...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Extract query parameters from callback URL
        Uri uri = Uri.parse(callbackUrl);
        
        // Build the callback URL that backend expects (replace localhost with ngrok)
        String backendCallbackUrl = callbackUrl.replace("http://localhost:8089", 
                com.example.mobilebanking.api.ApiClient.getBaseUrl().replace("/api/", ""));
        
        // Call backend callback endpoint
        com.example.mobilebanking.api.VNPayApiService vnPayService = 
                com.example.mobilebanking.api.ApiClient.getVNPayApiService();
        
        // Create a simple GET request to the callback URL
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(backendCallbackUrl)
                .get()
                .build();
        
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(VNPayWebViewActivity.this, 
                            "Nạp tiền thành công nhưng không thể xác nhận với server. Vui lòng kiểm tra lại số dư.", 
                            Toast.LENGTH_LONG).show();
                    
                    // Navigate back to home
                    Intent intent = new Intent(VNPayWebViewActivity.this, UiHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    if (response.isSuccessful()) {
                        Toast.makeText(VNPayWebViewActivity.this, 
                                "Nạp tiền thành công!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(VNPayWebViewActivity.this, 
                                "Nạp tiền thành công nhưng không thể xác nhận với server. Vui lòng kiểm tra lại số dư.", 
                                Toast.LENGTH_LONG).show();
                    }
                    
                    // Navigate back to home
                    Intent intent = new Intent(VNPayWebViewActivity.this, UiHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
    
    private String getErrorMessage(String responseCode) {
        if (responseCode == null) return "Không xác định";
        
        switch (responseCode) {
            case "00": return "Giao dịch thành công";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            default: return "Lỗi không xác định";
        }
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}

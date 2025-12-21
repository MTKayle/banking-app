package com.example.mobilebanking.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.dto.BookingRequest;
import com.example.mobilebanking.api.dto.BookingResponse;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;
import com.example.mobilebanking.utils.DataManager;

import java.math.BigDecimal;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MoviePaymentActivity
 * Hiển thị thông tin vé + form nhập thông tin khách hàng + gọi API đặt vé
 */
public class MoviePaymentActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";
    public static final String EXTRA_SEATS = "extra_seats";
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";
    public static final String EXTRA_SCREENING_ID = "extra_screening_id";
    public static final String EXTRA_SEAT_IDS = "extra_seat_ids";

    private Long screeningId;
    private long[] seatIds;
    
    private EditText etCustomerName;
    private EditText etCustomerPhone;
    private EditText etCustomerEmail;
    private Button btnConfirmPayment;
    private CheckBox cbConfirm;
    private ProgressDialog progressDialog;
    
    // Balance check
    private BigDecimal userBalance = BigDecimal.ZERO;
    private double totalAmount = 0;
    private boolean balanceLoaded = false;
    private TextView tvBalanceWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_payment_light);

        // Lấy screeningId và seatIds từ Intent
        screeningId = getIntent().getLongExtra(EXTRA_SCREENING_ID, -1);
        if (screeningId <= 0) screeningId = null;
        seatIds = getIntent().getLongArrayExtra(EXTRA_SEAT_IDS);

        bindDataFromIntent();
        setupInputFields();
        setupActions();
        setupCollapsibleSections();
        
        // Fetch user balance to check if sufficient
        fetchUserBalance();
    }
    
    /**
     * Lấy số dư tài khoản người dùng
     */
    private void fetchUserBalance() {
        DataManager dataManager = DataManager.getInstance(this);
        Long userId = dataManager.getUserId();
        
        if (userId == null || userId <= 0) {
            // Không có userId - có thể user chưa đăng nhập
            return;
        }
        
        AccountApiService accountService = ApiClient.getAccountApiService();
        Call<CheckingAccountInfoResponse> call = accountService.getCheckingAccountInfo(userId);
        
        call.enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, 
                                   Response<CheckingAccountInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userBalance = response.body().getBalance();
                    if (userBalance == null) {
                        userBalance = BigDecimal.ZERO;
                    }
                    balanceLoaded = true;
                    checkBalanceAndUpdateUI();
                }
            }
            
            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                // Không thể lấy số dư - vẫn cho phép đặt vé, backend sẽ check
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Kiểm tra số dư và cập nhật UI
     */
    private void checkBalanceAndUpdateUI() {
        if (!balanceLoaded) return;
        
        BigDecimal totalAmountBigDecimal = BigDecimal.valueOf(totalAmount);
        
        if (userBalance.compareTo(totalAmountBigDecimal) < 0) {
            // Số dư không đủ
            showInsufficientBalanceWarning();
        }
    }
    
    /**
     * Hiển thị cảnh báo số dư không đủ
     */
    private void showInsufficientBalanceWarning() {
        // Disable button và checkbox
        if (cbConfirm != null) {
            cbConfirm.setEnabled(false);
        }
        if (btnConfirmPayment != null) {
            btnConfirmPayment.setEnabled(false);
            btnConfirmPayment.setBackgroundResource(R.drawable.bg_movie_tab_light_inactive);
            btnConfirmPayment.setTextColor(getColor(R.color.movie_text_hint));
        }
        
        // Hiển thị cảnh báo
        String balanceStr = formatCurrency(userBalance.doubleValue());
        String totalStr = formatCurrency(totalAmount);
        
        new AlertDialog.Builder(this)
                .setTitle("Số dư không đủ")
                .setMessage("Số dư tài khoản của bạn (" + balanceStr + ") không đủ để thanh toán " + totalStr + ".\n\nVui lòng nạp thêm tiền vào tài khoản.")
                .setPositiveButton("Đóng", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Quay lại trang trước
                })
                .setCancelable(false) // Không cho phép dismiss bằng cách tap bên ngoài
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    
    /**
     * Parse total amount từ string
     */
    private double parseTotalAmount(String amountStr) {
        if (amountStr == null || amountStr.isEmpty()) {
            return 0;
        }
        
        try {
            // Loại bỏ các ký tự không phải số
            String cleanedAmount = amountStr
                    .replace("đ", "")
                    .replace("VND", "")
                    .replace(",", "")
                    .replace(".", "")
                    .replace(" ", "")
                    .trim();
            
            return Double.parseDouble(cleanedAmount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Format currency
     */
    private String formatCurrency(double amount) {
        long amountLong = Math.round(amount);
        String raw = String.valueOf(amountLong);
        StringBuilder sb = new StringBuilder();
        
        int len = raw.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(",");
            }
            sb.append(raw.charAt(i));
        }
        sb.append(" VND");
        return sb.toString();
    }
    
    /**
     * Setup input fields
     */
    private void setupInputFields() {
        etCustomerName = findViewById(R.id.et_customer_name);
        etCustomerPhone = findViewById(R.id.et_customer_phone);
        etCustomerEmail = findViewById(R.id.et_customer_email);
    }
    
    /**
     * Setup các section có thể collapse/expand
     */
    private void setupCollapsibleSections() {
        // Movie info section
        View layoutMovieHeader = findViewById(R.id.layout_movie_header);
        View layoutMovieDetails = findViewById(R.id.layout_movie_details);
        ImageView ivMovieExpand = findViewById(R.id.iv_movie_expand);
        
        if (layoutMovieHeader != null && layoutMovieDetails != null && ivMovieExpand != null) {
            layoutMovieHeader.setOnClickListener(v -> {
                if (layoutMovieDetails.getVisibility() == View.VISIBLE) {
                    layoutMovieDetails.setVisibility(View.GONE);
                    ivMovieExpand.setRotation(0);
                } else {
                    layoutMovieDetails.setVisibility(View.VISIBLE);
                    ivMovieExpand.setRotation(180);
                }
            });
        }
    }

    private void bindDataFromIntent() {
        // New layout fields
        TextView tvMovieTitle = findViewById(R.id.tv_movie_title_value);
        TextView tvShowtime = findViewById(R.id.tv_showtime_value);
        TextView tvRoom = findViewById(R.id.tv_room_value);
        TextView tvSeats = findViewById(R.id.tv_seats_value);
        TextView tvCinema = findViewById(R.id.tv_cinema_value);
        TextView tvAddress = findViewById(R.id.tv_address_value);
        TextView tvTicketAmount = findViewById(R.id.tv_ticket_amount);
        TextView tvTotalAmount = findViewById(R.id.tv_total_amount);
        TextView tvSubtotal = findViewById(R.id.tv_subtotal);

        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        String cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        String cinemaAddress = getIntent().getStringExtra(EXTRA_CINEMA_ADDRESS);
        String showtime = getIntent().getStringExtra(EXTRA_SHOWTIME);
        String showDate = getIntent().getStringExtra(EXTRA_SHOWDATE);
        String room = getIntent().getStringExtra(EXTRA_ROOM);
        String seats = getIntent().getStringExtra(EXTRA_SEATS);
        String totalAmountStr = getIntent().getStringExtra(EXTRA_TOTAL_AMOUNT);
        
        // Parse total amount từ string (format: "100.000đ" hoặc "100,000 VND")
        totalAmount = parseTotalAmount(totalAmountStr);

        // Tên phim
        if (movieTitle != null && tvMovieTitle != null) {
            tvMovieTitle.setText(movieTitle);
        }

        // Suất chiếu
        if (tvShowtime != null) {
            StringBuilder showtimeBuilder = new StringBuilder();
            if (showtime != null) {
                showtimeBuilder.append(showtime);
            }
            if (showDate != null) {
                if (showtimeBuilder.length() > 0) showtimeBuilder.append(" ");
                showtimeBuilder.append(showDate);
            }
            if (showtimeBuilder.length() > 0) {
                tvShowtime.setText(showtimeBuilder.toString());
            }
        }

        // Phòng chiếu
        if (room != null && tvRoom != null) {
            tvRoom.setText(room);
        }

        // Số ghế
        if (seats != null && tvSeats != null) {
            tvSeats.setText(seats);
        }

        // Rạp chiếu
        if (cinemaName != null && tvCinema != null) {
            tvCinema.setText(cinemaName);
        }

        // Địa chỉ
        if (cinemaAddress != null && tvAddress != null) {
            tvAddress.setText(cinemaAddress);
        }

        // Số tiền - hiển thị từ totalAmount đã parse
        String formattedAmount = formatCurrency(totalAmount);
        
        if (tvTicketAmount != null) {
            tvTicketAmount.setText(formattedAmount);
        }
        if (tvSubtotal != null) {
            tvSubtotal.setText(formattedAmount);
        }
        if (tvTotalAmount != null) {
            tvTotalAmount.setText(formattedAmount);
        }

        // Count number of seats for ticket label
        int seatCount = seats != null ? seats.split(",").length : 1;
        TextView tvTicketLabel = findViewById(R.id.tv_ticket_label);
        if (tvTicketLabel != null) {
            tvTicketLabel.setText("Vé xem phim (" + seatCount + ")");
        }
    }

    private void setupActions() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        cbConfirm = findViewById(R.id.cb_confirm);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Chỉ enable button nếu đã check VÀ số dư đủ
            boolean canEnable = isChecked && isBalanceSufficient();
            btnConfirmPayment.setEnabled(canEnable);
            if (canEnable) {
                btnConfirmPayment.setBackgroundResource(R.drawable.bg_movie_tab_light_active);
                btnConfirmPayment.setTextColor(0xFFFFFFFF);
            } else {
                btnConfirmPayment.setBackgroundResource(R.drawable.bg_movie_tab_light_inactive);
                btnConfirmPayment.setTextColor(getColor(R.color.movie_text_hint));
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            // Check balance again before booking
            if (!isBalanceSufficient()) {
                showInsufficientBalanceWarning();
                return;
            }
            
            // Validate input
            if (!validateInput()) {
                return;
            }
            
            // Chuyển sang OTP verification trước khi đặt vé
            navigateToOtpVerification();
        });
    }
    
    /**
     * Kiểm tra số dư có đủ không
     */
    private boolean isBalanceSufficient() {
        if (!balanceLoaded) {
            // Chưa load được balance, cho phép đặt vé (backend sẽ check)
            return true;
        }
        return userBalance.compareTo(BigDecimal.valueOf(totalAmount)) >= 0;
    }
    
    /**
     * Navigate to OTP verification before booking
     * OTP sẽ được gửi đến số điện thoại của tài khoản đang đăng nhập
     */
    private void navigateToOtpVerification() {
        // Lấy thông tin từ form
        String customerName = etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText().toString().trim();
        String customerEmail = etCustomerEmail.getText().toString().trim();
        
        // Lấy số điện thoại của user đang đăng nhập
        DataManager dataManager = DataManager.getInstance(this);
        String userPhone = dataManager.getUserPhone();
        
        if (userPhone == null || userPhone.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy số điện thoại tài khoản. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Check if amount >= 10 million -> require face verification first
        if (totalAmount >= 10000000) {
            // Navigate to face verification first
            navigateToFaceVerification(customerName, customerPhone, customerEmail, userPhone);
        } else {
            // Navigate directly to OTP
            navigateToOtpWithData(customerName, customerPhone, customerEmail, userPhone);
        }
    }
    
    /**
     * Navigate to face verification for high-value transactions (>= 10M)
     */
    private void navigateToFaceVerification(String customerName, String customerPhone, 
                                           String customerEmail, String userPhone) {
        Intent intent = new Intent(this, FaceVerificationTransactionActivity.class);
        intent.putExtra("from", "movie_booking");
        
        // Pass booking data
        intent.putExtra("customer_name", customerName);
        intent.putExtra("customer_phone", customerPhone);
        intent.putExtra("customer_email", customerEmail);
        intent.putExtra("user_phone", userPhone);
        intent.putExtra("screening_id", screeningId);
        intent.putExtra("seat_ids", seatIds);
        
        // Pass display info
        intent.putExtra("movie_title", getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
        intent.putExtra("cinema_name", getIntent().getStringExtra(EXTRA_CINEMA_NAME));
        intent.putExtra("showtime", getIntent().getStringExtra(EXTRA_SHOWTIME));
        intent.putExtra("seats", getIntent().getStringExtra(EXTRA_SEATS));
        intent.putExtra("total_amount", totalAmount);
        
        startActivity(intent);
        finish();
    }
    
    /**
     * Navigate to OTP with booking data
     */
    private void navigateToOtpWithData(String customerName, String customerPhone, 
                                      String customerEmail, String userPhone) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        // OTP gửi đến số điện thoại của tài khoản đang đăng nhập
        intent.putExtra("phone", userPhone);
        intent.putExtra("from", "movie_booking");
        
        // Truyền thông tin booking để xử lý sau khi OTP thành công
        intent.putExtra("customer_name", customerName);
        intent.putExtra("customer_phone", customerPhone);  // SĐT khách hàng (có thể khác với SĐT tài khoản)
        intent.putExtra("customer_email", customerEmail);
        intent.putExtra("screening_id", screeningId);
        intent.putExtra("seat_ids", seatIds);
        
        // Truyền thêm thông tin để hiển thị trong success screen
        intent.putExtra("movie_title", getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
        intent.putExtra("cinema_name", getIntent().getStringExtra(EXTRA_CINEMA_NAME));
        intent.putExtra("showtime", getIntent().getStringExtra(EXTRA_SHOWTIME));
        intent.putExtra("seats", getIntent().getStringExtra(EXTRA_SEATS));
        intent.putExtra("total_amount", totalAmount);
        
        startActivity(intent);
        finish(); // Finish activity này để không quay lại được
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        String name = etCustomerName.getText().toString().trim();
        String phone = etCustomerPhone.getText().toString().trim();
        String email = etCustomerEmail.getText().toString().trim();
        
        if (name.isEmpty()) {
            etCustomerName.setError("Vui lòng nhập họ và tên");
            etCustomerName.requestFocus();
            return false;
        }
        
        if (phone.isEmpty()) {
            etCustomerPhone.setError("Vui lòng nhập số điện thoại");
            etCustomerPhone.requestFocus();
            return false;
        }
        
        if (phone.length() < 10) {
            etCustomerPhone.setError("Số điện thoại không hợp lệ");
            etCustomerPhone.requestFocus();
            return false;
        }
        
        if (screeningId == null) {
            showErrorDialog("Lỗi", "Không tìm thấy thông tin suất chiếu");
            return false;
        }
        
        if (seatIds == null || seatIds.length == 0) {
            showErrorDialog("Lỗi", "Không có ghế nào được chọn");
            return false;
        }
        
        return true;
    }
    
    /**
     * Call booking API
     */
    private void createBooking() {
        // Show loading
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý đặt vé...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // Build request
        String customerName = etCustomerName.getText().toString().trim();
        String customerPhone = etCustomerPhone.getText().toString().trim();
        String customerEmail = etCustomerEmail.getText().toString().trim();
        
        List<Long> seatIdList = new ArrayList<>();
        for (long id : seatIds) {
            seatIdList.add(id);
        }
        
        BookingRequest request = new BookingRequest(
                screeningId,
                seatIdList,
                customerName,
                customerPhone,
                customerEmail
        );
        
        // Call API
        MovieApiService apiService = ApiClient.getMovieApiService();
        Call<BookingResponse> call = apiService.createBooking(request);
        
        call.enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                progressDialog.dismiss();
                
                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse bookingResponse = response.body();
                    
                    if (bookingResponse.getSuccess() != null && bookingResponse.getSuccess()) {
                        // Success - navigate to success screen
                        navigateToSuccessScreen(bookingResponse.getData());
                    } else {
                        // API returned error
                        String errorMsg = bookingResponse.getMessage() != null 
                                ? bookingResponse.getMessage() 
                                : "Đặt vé thất bại. Vui lòng thử lại.";
                        showErrorDialog("Đặt vé thất bại", errorMsg);
                    }
                } else {
                    // HTTP error - parse error message from response body
                    String errorMsg = parseErrorMessage(response);
                    showErrorDialog("Đặt vé thất bại", errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                progressDialog.dismiss();
                showErrorDialog("Lỗi kết nối", "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng và thử lại.");
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Navigate to success screen with booking data
     */
    private void navigateToSuccessScreen(BookingResponse.BookingData data) {
        Intent intent = new Intent(this, MovieTicketSuccessActivity.class);
        
        if (data != null) {
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_BOOKING_CODE, data.getBookingCode());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_MOVIE_TITLE, data.getMovieTitle());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_NAME, data.getCinemaName());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_ADDRESS, data.getCinemaAddress());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_HALL_NAME, data.getHallName());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SCREENING_DATE, data.getScreeningDate());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_START_TIME, data.getStartTime());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SEAT_COUNT, data.getSeatCount() != null ? data.getSeatCount() : 0);
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_TOTAL_AMOUNT, data.getTotalAmount() != null ? data.getTotalAmount() : 0.0);
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_CUSTOMER_NAME, data.getCustomerName());
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_QR_CODE, data.getQrCode());
            
            // Convert seat labels list to string
            if (data.getSeatLabels() != null && !data.getSeatLabels().isEmpty()) {
                StringBuilder seats = new StringBuilder();
                for (int i = 0; i < data.getSeatLabels().size(); i++) {
                    if (i > 0) seats.append(", ");
                    seats.append(data.getSeatLabels().get(i));
                }
                intent.putExtra(MovieTicketSuccessActivity.EXTRA_SEATS, seats.toString());
            }
        } else {
            // Fallback to intent data
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_MOVIE_TITLE,
                    getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_NAME,
                    getIntent().getStringExtra(EXTRA_CINEMA_NAME));
            intent.putExtra(MovieTicketSuccessActivity.EXTRA_SEATS,
                    getIntent().getStringExtra(EXTRA_SEATS));
        }
        
        startActivity(intent);
        finish();
    }
    
    /**
     * Parse error message from API response
     */
    private String parseErrorMessage(Response<BookingResponse> response) {
        String defaultMsg = "Đặt vé thất bại. Vui lòng thử lại.";
        
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorBody);
                
                // Try to get message from response
                if (jsonObject.has("message")) {
                    return jsonObject.getString("message");
                }
                
                // Try to get error field
                if (jsonObject.has("error")) {
                    return jsonObject.getString("error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Fallback based on HTTP status code
        switch (response.code()) {
            case 400:
                return "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại thông tin.";
            case 402:
                return "Số dư tài khoản không đủ để thanh toán.";
            case 409:
                return "Ghế đã được đặt bởi người khác. Vui lòng chọn ghế khác.";
            case 404:
                return "Không tìm thấy suất chiếu hoặc ghế.";
            case 500:
                return "Lỗi hệ thống. Vui lòng thử lại sau.";
            default:
                return defaultMsg;
        }
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

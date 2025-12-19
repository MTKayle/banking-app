package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Hiển thị màn hình đặt vé thành công với thiết kế hiện đại như rạp chiếu phim.
 */
public class MovieTicketSuccessActivity extends AppCompatActivity {

    // Các extra keys từ API response
    public static final String EXTRA_BOOKING_CODE = "extra_booking_code";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_HALL_NAME = "extra_hall_name";
    public static final String EXTRA_SCREENING_DATE = "extra_screening_date";
    public static final String EXTRA_START_TIME = "extra_start_time";
    public static final String EXTRA_SEATS = "extra_seats";
    public static final String EXTRA_SEAT_COUNT = "extra_seat_count";
    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";
    public static final String EXTRA_CUSTOMER_NAME = "extra_customer_name";
    public static final String EXTRA_QR_CODE = "extra_qr_code";
    
    // Legacy extra keys (for backward compatibility)
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_ticket_success_light);

        bindDataFromIntent();
        setupButtons();
    }
    
    private void bindDataFromIntent() {
        Intent intent = getIntent();
        
        // Set Vietnamese labels
        setVietnameseLabels();
        
        // Booking Code
        TextView tvBookingCode = findViewById(R.id.tv_booking_code);
        String bookingCode = intent.getStringExtra(EXTRA_BOOKING_CODE);
        if (tvBookingCode != null && bookingCode != null) {
            tvBookingCode.setText("Mã đặt vé: " + bookingCode);
        }
        
        // Movie Title
        TextView tvMovieTitle = findViewById(R.id.tv_movie_title);
        String movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE);
        if (tvMovieTitle != null && movieTitle != null) {
            tvMovieTitle.setText(movieTitle);
        }
        
        // Cinema Name
        TextView tvCinemaName = findViewById(R.id.tv_cinema_name);
        String cinemaName = intent.getStringExtra(EXTRA_CINEMA_NAME);
        if (tvCinemaName != null && cinemaName != null) {
            tvCinemaName.setText(cinemaName);
        }
        
        // Cinema Address
        TextView tvCinemaAddress = findViewById(R.id.tv_cinema_address);
        String cinemaAddress = intent.getStringExtra(EXTRA_CINEMA_ADDRESS);
        if (tvCinemaAddress != null && cinemaAddress != null) {
            tvCinemaAddress.setText(cinemaAddress);
        }
        
        // Screening Date
        TextView tvScreeningDate = findViewById(R.id.tv_screening_date);
        String screeningDate = intent.getStringExtra(EXTRA_SCREENING_DATE);
        if (tvScreeningDate != null && screeningDate != null) {
            tvScreeningDate.setText(screeningDate);
        }
        
        // Start Time
        TextView tvStartTime = findViewById(R.id.tv_start_time);
        String startTime = intent.getStringExtra(EXTRA_START_TIME);
        if (tvStartTime != null && startTime != null) {
            tvStartTime.setText(startTime);
        }
        
        // Hall Name
        TextView tvHallName = findViewById(R.id.tv_hall_name);
        String hallName = intent.getStringExtra(EXTRA_HALL_NAME);
        if (tvHallName != null && hallName != null) {
            tvHallName.setText(hallName);
        }
        
        // Seat Count
        TextView tvSeatCount = findViewById(R.id.tv_seat_count);
        int seatCount = intent.getIntExtra(EXTRA_SEAT_COUNT, 0);
        if (tvSeatCount != null) {
            tvSeatCount.setText(seatCount + " ghế");
        }
        
        // Seats
        TextView tvSeats = findViewById(R.id.tv_seats);
        String seats = intent.getStringExtra(EXTRA_SEATS);
        if (tvSeats != null && seats != null) {
            tvSeats.setText(seats);
        }
        
        // Total Amount
        TextView tvTotalAmount = findViewById(R.id.tv_total_amount);
        double totalAmount = intent.getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0);
        if (tvTotalAmount != null) {
            tvTotalAmount.setText(formatCurrency(totalAmount));
        }
        
        // Customer Name
        TextView tvCustomerName = findViewById(R.id.tv_customer_name);
        String customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME);
        if (tvCustomerName != null && customerName != null) {
            tvCustomerName.setText("Người đặt: " + customerName.toUpperCase());
        }
    }
    
    private void setupButtons() {
        Button btnViewBookings = findViewById(R.id.btn_view_bookings);
        Button btnBackHome = findViewById(R.id.btn_back_home);
        
        if (btnViewBookings != null) {
            btnViewBookings.setOnClickListener(v -> {
                // Navigate to My Tickets screen
                Intent intent = new Intent(this, MyTicketsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
        
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> {
                // Navigate to UiHomeActivity (green header home screen)
                Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
    
    /**
     * Set Vietnamese labels programmatically to avoid XML encoding issues
     */
    private void setVietnameseLabels() {
        // Success title is already set in bindDataFromIntent
    }
    
    /**
     * Format currency to Vietnamese format
     */
    private String formatCurrency(double amount) {
        if (amount == 0) return "0 VND";
        
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format((long) amount) + " VND";
    }
    
    @Override
    public void onBackPressed() {
        // Go back to services instead of payment screen
        Intent intent = new Intent(this, ServicesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

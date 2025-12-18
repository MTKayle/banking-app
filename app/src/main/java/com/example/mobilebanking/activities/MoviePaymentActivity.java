package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * MoviePaymentActivity
 * FRONTEND ONLY: Hiển thị thông tin vé + tổng tiền và mô phỏng bước thanh toán trong app ngân hàng.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_payment_dark);

        bindDataFromIntent();
        setupActions();
    }

    private void bindDataFromIntent() {
        TextView tvMovieTitle = findViewById(R.id.tv_movie_title_value);
        TextView tvCinema = findViewById(R.id.tv_cinema_value);
        TextView tvShowtime = findViewById(R.id.tv_showtime_value);
        TextView tvSeats = findViewById(R.id.tv_seats_value);
        TextView tvTicketAmount = findViewById(R.id.tv_ticket_amount);
        TextView tvTotalAmount = findViewById(R.id.tv_total_amount);

        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        String cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        String cinemaAddress = getIntent().getStringExtra(EXTRA_CINEMA_ADDRESS);
        String showtime = getIntent().getStringExtra(EXTRA_SHOWTIME);
        String showDate = getIntent().getStringExtra(EXTRA_SHOWDATE);
        String room = getIntent().getStringExtra(EXTRA_ROOM);
        String seats = getIntent().getStringExtra(EXTRA_SEATS);
        String totalAmount = getIntent().getStringExtra(EXTRA_TOTAL_AMOUNT);

        if (movieTitle != null) {
            tvMovieTitle.setText(movieTitle);
        }

        StringBuilder cinemaBuilder = new StringBuilder();
        if (cinemaName != null) {
            cinemaBuilder.append(cinemaName);
        }
        if (room != null) {
            if (cinemaBuilder.length() > 0) cinemaBuilder.append(" • ");
            cinemaBuilder.append(room);
        }
        if (cinemaBuilder.length() > 0) {
            tvCinema.setText(cinemaBuilder.toString());
        }

        StringBuilder showtimeBuilder = new StringBuilder();
        if (showtime != null) {
            showtimeBuilder.append(showtime);
        }
        if (showDate != null) {
            if (showtimeBuilder.length() > 0) showtimeBuilder.append(" • ");
            showtimeBuilder.append(showDate);
        }
        if (showtimeBuilder.length() > 0) {
            tvShowtime.setText(showtimeBuilder.toString());
        }

        if (seats != null) {
            tvSeats.setText("Ghế: " + seats);
        }

        String displayAmount = totalAmount != null ? totalAmount : "110.500đ";
        tvTicketAmount.setText(displayAmount);
        tvTotalAmount.setText(displayAmount);

        // Cinema address không hiển thị riêng, nhưng vẫn có trong Intent nếu cần mở rộng sau này.
    }

    private void setupActions() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        CheckBox cbConfirm = findViewById(R.id.cb_confirm);
        Button btnConfirmPayment = findViewById(R.id.btn_confirm_payment);

        cbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnConfirmPayment.setEnabled(isChecked);
            if (isChecked) {
                btnConfirmPayment.setBackgroundResource(R.drawable.bg_button_primary_rounded);
                btnConfirmPayment.setTextColor(0xFFFFFFFF);
            } else {
                btnConfirmPayment.setBackgroundResource(R.drawable.bg_button_secondary_rounded);
                btnConfirmPayment.setTextColor(0xFF6C757D);
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Thanh toán thành công. Vé sẽ được tạo.",
                    Toast.LENGTH_LONG
            ).show();

            Intent successIntent = new Intent(this, MovieTicketSuccessActivity.class);
            successIntent.putExtra(MovieTicketSuccessActivity.EXTRA_MOVIE_TITLE,
                    getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
            successIntent.putExtra(MovieTicketSuccessActivity.EXTRA_CINEMA_NAME,
                    getIntent().getStringExtra(EXTRA_CINEMA_NAME));
            successIntent.putExtra(MovieTicketSuccessActivity.EXTRA_SHOWTIME,
                    getIntent().getStringExtra(EXTRA_SHOWTIME));
            successIntent.putExtra(MovieTicketSuccessActivity.EXTRA_SHOWDATE,
                    getIntent().getStringExtra(EXTRA_SHOWDATE));
            successIntent.putExtra(MovieTicketSuccessActivity.EXTRA_ROOM,
                    getIntent().getStringExtra(EXTRA_ROOM));
            successIntent.putExtra(MovieTicketSuccessActivity.EXTRA_SEATS,
                    getIntent().getStringExtra(EXTRA_SEATS));
            startActivity(successIntent);
            finish();
        });
    }
}



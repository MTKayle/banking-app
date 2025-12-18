package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * Hiển thị màn hình xác nhận vé sau khi thanh toán thành công.
 * FRONTEND ONLY: chỉ show thông tin vé mẫu, không xử lý lưu trữ.
 */
public class MovieTicketSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";
    public static final String EXTRA_SEATS = "extra_seats";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_ticket_success_dark);

        bindDataFromIntent();

        Button btnBackHome = findViewById(R.id.btn_back_home);
        btnBackHome.setOnClickListener(v -> {
            // Quay về màn hình dịch vụ (hoặc Home) để kết thúc flow
            Intent intent = new Intent(this, ServicesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void bindDataFromIntent() {
        TextView tvMovieTitle = findViewById(R.id.tv_movie_title_value);
        TextView tvCinemaLine = findViewById(R.id.tv_cinema_line);
        TextView tvTimeLine = findViewById(R.id.tv_time_line);
        TextView tvSeatsLine = findViewById(R.id.tv_seats_line);

        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        String cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        String showtime = getIntent().getStringExtra(EXTRA_SHOWTIME);
        String showDate = getIntent().getStringExtra(EXTRA_SHOWDATE);
        String room = getIntent().getStringExtra(EXTRA_ROOM);
        String seats = getIntent().getStringExtra(EXTRA_SEATS);

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
            tvCinemaLine.setText(cinemaBuilder.toString());
        }

        StringBuilder timeBuilder = new StringBuilder();
        if (showtime != null) {
            timeBuilder.append(showtime);
        }
        if (showDate != null) {
            if (timeBuilder.length() > 0) timeBuilder.append(" • ");
            timeBuilder.append(showDate);
        }
        if (timeBuilder.length() > 0) {
            tvTimeLine.setText(timeBuilder.toString());
        }

        if (seats != null) {
            tvSeatsLine.setText("Ghế: " + seats);
        }
    }
}



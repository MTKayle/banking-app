package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * SelectShowtimeActivity
 * FRONTEND ONLY: Cho phép người dùng chọn ngày chiếu, rạp và suất chiếu mẫu.
 */
public class SelectShowtimeActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";

    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_showtime_dark);

        bindHeader();
        setupClickListeners();
    }

    private void bindHeader() {
        TextView tvHeaderTitle = findViewById(R.id.tv_header_title);
        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        if (movieTitle != null && tvHeaderTitle != null) {
            tvHeaderTitle.setText(movieTitle);
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupClickListeners() {
        // Ví dụ: chọn suất 23:20 tại CGV Sư Vạn Hạnh
        Button btnShowtimeCgv2320 = findViewById(R.id.btn_showtime_1_2320);
        if (btnShowtimeCgv2320 != null) {
            btnShowtimeCgv2320.setOnClickListener(v ->
                    openSeatSelection(
                            "CGV Sư Vạn Hạnh",
                            "Tầng 6, Vạn Hạnh Mall, 11 Sư Vạn Hạnh, Phường 12, Quận 10",
                            "23:20",
                            "18/12/2025",
                            "Cinema 9"
                    ));
        }

        // Ví dụ: chọn suất 18:00 tại Lotte Gò Vấp
        Button btnShowtimeLotte1800 = findViewById(R.id.btn_showtime_2_1800);
        if (btnShowtimeLotte1800 != null) {
            btnShowtimeLotte1800.setOnClickListener(v ->
                    openSeatSelection(
                            "Lotte Gò Vấp",
                            "Tầng 3, TTTM Lotte Mart Gò Vấp, 242 Nguyễn Văn Lượng",
                            "18:00",
                            "18/12/2025",
                            "Phòng 2"
                    ));
        }
    }

    private void openSeatSelection(String cinemaName,
                                   String cinemaAddress,
                                   String showtime,
                                   String showDate,
                                   String room) {
        Intent fromIntent = getIntent();
        String movieTitle = fromIntent != null ? fromIntent.getStringExtra(EXTRA_MOVIE_TITLE) : null;

        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_TITLE, movieTitle);
        intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_NAME, cinemaName);
        intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_ADDRESS, cinemaAddress);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWDATE, showDate);
        intent.putExtra(SeatSelectionActivity.EXTRA_ROOM, room);
        startActivity(intent);
    }
}



package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * SelectShowtimeActivity
 * FRONTEND ONLY: Cho phép người dùng chọn ngày chiếu, hệ thống rạp và suất chiếu mẫu.
 */
public class SelectShowtimeActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";

    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";

    private String selectedShowDate = "18/12/2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_showtime_dark);

        bindHeader();
        setupDateSelection();
        setupBrandSelection();
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

    private void setupDateSelection() {
        // Ngày 18
        LinearLayout date18 = findViewById(R.id.date_item_18);
        TextView num18 = findViewById(R.id.tv_date_18_number);
        TextView lbl18 = findViewById(R.id.tv_date_18_label);

        LinearLayout date19 = findViewById(R.id.date_item_19);
        TextView num19 = findViewById(R.id.tv_date_19_number);
        TextView lbl19 = findViewById(R.id.tv_date_19_label);

        LinearLayout date20 = findViewById(R.id.date_item_20);
        TextView num20 = findViewById(R.id.tv_date_20_number);
        TextView lbl20 = findViewById(R.id.tv_date_20_label);

        LinearLayout date21 = findViewById(R.id.date_item_21);
        TextView num21 = findViewById(R.id.tv_date_21_number);
        TextView lbl21 = findViewById(R.id.tv_date_21_label);

        LinearLayout date22 = findViewById(R.id.date_item_22);
        TextView num22 = findViewById(R.id.tv_date_22_number);
        TextView lbl22 = findViewById(R.id.tv_date_22_label);

        LinearLayout date23 = findViewById(R.id.date_item_23);
        TextView num23 = findViewById(R.id.tv_date_23_number);
        TextView lbl23 = findViewById(R.id.tv_date_23_label);

        LinearLayout date24 = findViewById(R.id.date_item_24);
        TextView num24 = findViewById(R.id.tv_date_24_number);
        TextView lbl24 = findViewById(R.id.tv_date_24_label);

        View.OnClickListener listener = v -> {
            resetAllDateStyles(
                    date18, num18, lbl18,
                    date19, num19, lbl19,
                    date20, num20, lbl20,
                    date21, num21, lbl21,
                    date22, num22, lbl22,
                    date23, num23, lbl23,
                    date24, num24, lbl24
            );

            if (v == date18) {
                highlightDate(date18, num18, lbl18);
                selectedShowDate = "18/12/2025";
            } else if (v == date19) {
                highlightDate(date19, num19, lbl19);
                selectedShowDate = "19/12/2025";
            } else if (v == date20) {
                highlightDate(date20, num20, lbl20);
                selectedShowDate = "20/12/2025";
            } else if (v == date21) {
                highlightDate(date21, num21, lbl21);
                selectedShowDate = "21/12/2025";
            } else if (v == date22) {
                highlightDate(date22, num22, lbl22);
                selectedShowDate = "22/12/2025";
            } else if (v == date23) {
                highlightDate(date23, num23, lbl23);
                selectedShowDate = "23/12/2025";
            } else if (v == date24) {
                highlightDate(date24, num24, lbl24);
                selectedShowDate = "24/12/2025";
            }
        };

        if (date18 != null) date18.setOnClickListener(listener);
        if (date19 != null) date19.setOnClickListener(listener);
        if (date20 != null) date20.setOnClickListener(listener);
        if (date21 != null) date21.setOnClickListener(listener);
        if (date22 != null) date22.setOnClickListener(listener);
        if (date23 != null) date23.setOnClickListener(listener);
        if (date24 != null) date24.setOnClickListener(listener);
    }

    private void resetAllDateStyles(
            LinearLayout d18, TextView n18, TextView l18,
            LinearLayout d19, TextView n19, TextView l19,
            LinearLayout d20, TextView n20, TextView l20,
            LinearLayout d21, TextView n21, TextView l21,
            LinearLayout d22, TextView n22, TextView l22,
            LinearLayout d23, TextView n23, TextView l23,
            LinearLayout d24, TextView n24, TextView l24
    ) {
        resetSingleDate(d18, n18, l18);
        resetSingleDate(d19, n19, l19);
        resetSingleDate(d20, n20, l20);
        resetSingleDate(d21, n21, l21);
        resetSingleDate(d22, n22, l22);
        resetSingleDate(d23, n23, l23);
        resetSingleDate(d24, n24, l24);
    }

    private void resetSingleDate(LinearLayout container, TextView number, TextView label) {
        if (container == null || number == null || label == null) return;
        container.setBackgroundResource(R.drawable.bg_movie_date_inactive);
        number.setTextColor(0xFF212121); // text_primary
        label.setTextColor(0xFF757575);  // text_secondary
    }

    private void highlightDate(LinearLayout container, TextView number, TextView label) {
        container.setBackgroundResource(R.drawable.bg_movie_date_active);
        number.setTextColor(0xFFFFFFFF);
        label.setTextColor(0xFFFFFFFF);
    }

    private void setupBrandSelection() {
        LinearLayout brandFavorite = findViewById(R.id.brand_favorite);
        ImageView ivFavorite = findViewById(R.id.iv_brand_favorite);
        TextView tvFavorite = findViewById(R.id.tv_brand_favorite);

        LinearLayout brandCgv = findViewById(R.id.brand_cgv);
        ImageView ivCgv = findViewById(R.id.iv_brand_cgv);
        TextView tvCgv = findViewById(R.id.tv_brand_cgv);

        LinearLayout brandLotte = findViewById(R.id.brand_lotte);
        ImageView ivLotte = findViewById(R.id.iv_brand_lotte);
        TextView tvLotte = findViewById(R.id.tv_brand_lotte);

        LinearLayout brandBeta = findViewById(R.id.brand_beta);
        ImageView ivBeta = findViewById(R.id.iv_brand_beta);
        TextView tvBeta = findViewById(R.id.tv_brand_beta);

        View.OnClickListener listener = v -> {
            resetBrandStyle(brandFavorite, ivFavorite, tvFavorite);
            resetBrandStyle(brandCgv, ivCgv, tvCgv);
            resetBrandStyle(brandLotte, ivLotte, tvLotte);
            resetBrandStyle(brandBeta, ivBeta, tvBeta);

            if (v == brandFavorite) {
                highlightBrand(brandFavorite, ivFavorite, tvFavorite);
            } else if (v == brandCgv) {
                highlightBrand(brandCgv, ivCgv, tvCgv);
            } else if (v == brandLotte) {
                highlightBrand(brandLotte, ivLotte, tvLotte);
            } else if (v == brandBeta) {
                highlightBrand(brandBeta, ivBeta, tvBeta);
            }
        };

        if (brandFavorite != null) brandFavorite.setOnClickListener(listener);
        if (brandCgv != null) brandCgv.setOnClickListener(listener);
        if (brandLotte != null) brandLotte.setOnClickListener(listener);
        if (brandBeta != null) brandBeta.setOnClickListener(listener);

        // Mặc định chọn "Gần tôi"
        highlightBrand(brandFavorite, ivFavorite, tvFavorite);
    }

    /**
     * Check if the icon is a bitmap logo icon (PNG/JPG) that should not have color filters applied
     * Logo icons: ic_cgv, ic_lotte, ic_beta
     * Vector icon: ic_location (iv_brand_favorite)
     */
    private boolean isLogoIcon(ImageView icon) {
        if (icon == null) return false;
        
        int iconId = icon.getId();
        // Logo icons are bitmap images (PNG/JPG) that should not have color filters
        return iconId == R.id.iv_brand_cgv || 
               iconId == R.id.iv_brand_lotte || 
               iconId == R.id.iv_brand_beta;
    }

    private void resetBrandStyle(LinearLayout container, ImageView icon, TextView label) {
        if (container == null || icon == null || label == null) return;
        container.setBackgroundResource(R.drawable.bg_movie_brand_card);
        
        // Only apply color filter to vector icons (ic_location), not bitmap logo icons
        if (isLogoIcon(icon)) {
            icon.clearColorFilter(); // Clear any existing filters for logo icons
        } else {
            icon.setColorFilter(0xFF0EB378); // Apply green filter for vector icons
        }
        
        label.setTextColor(0xFF212121);
    }

    private void highlightBrand(LinearLayout container, ImageView icon, TextView label) {
        if (container == null || icon == null || label == null) return;
        container.setBackgroundResource(R.drawable.bg_movie_brand_active);
        
        // Only apply color filter to vector icons (ic_location), not bitmap logo icons
        if (isLogoIcon(icon)) {
            icon.clearColorFilter(); // Clear any existing filters for logo icons
        } else {
            icon.setColorFilter(0xFFFFFFFF); // Apply white filter for vector icons
        }
        
        label.setTextColor(0xFFFFFFFF);
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
                            selectedShowDate,
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
                            selectedShowDate,
                            "Phòng 2"
                    ));
        }

        // Một vài suất demo cho các rạp khác
        Button btnShowtimeBhd1930 = findViewById(R.id.btn_showtime_3_1930);
        if (btnShowtimeBhd1930 != null) {
            btnShowtimeBhd1930.setOnClickListener(v ->
                    openSeatSelection(
                            "BHD Star Bitexco",
                            "Tầng 3, Tháp Tài Chính Bitexco, Quận 1",
                            "19:30",
                            selectedShowDate,
                            "Phòng 3"
                    ));
        }

        Button btnShowtimeGalaxy2030 = findViewById(R.id.btn_showtime_4_2030);
        if (btnShowtimeGalaxy2030 != null) {
            btnShowtimeGalaxy2030.setOnClickListener(v ->
                    openSeatSelection(
                            "Galaxy Nguyễn Du",
                            "116 Nguyễn Du, Quận 1",
                            "20:30",
                            selectedShowDate,
                            "Phòng 5"
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


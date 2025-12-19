package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.dto.ScreeningListResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SelectShowtimeActivity
 * FRONTEND ONLY: Cho phép người dùng chọn ngày chiếu, hệ thống rạp và suất chiếu mẫu.
 */
public class SelectShowtimeActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";

    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";

    private Long movieId;
    private String selectedShowDate;
    private String selectedShowDateAPI; // Format YYYY-MM-DD for API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_showtime_light);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_MOVIE_ID)) {
            movieId = intent.getLongExtra(EXTRA_MOVIE_ID, -1);
            if (movieId <= 0) {
                movieId = null;
            }
        }

        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedShowDate = displayFormat.format(calendar.getTime());
        selectedShowDateAPI = apiFormat.format(calendar.getTime());

        bindHeader();
        setupDateSelection();
        setupBrandSelection();
        updateCurrentDateLabel();
        
        // Gọi API lấy screenings cho ngày hiện tại
        if (movieId != null) {
            fetchScreenings(movieId, selectedShowDateAPI);
        }
    }
    
    /**
     * Cập nhật label ngày chiếu hiện tại
     */
    private void updateCurrentDateLabel() {
        TextView tvCurrentDate = findViewById(R.id.tv_current_date);
        if (tvCurrentDate != null) {
            tvCurrentDate.setText("Ngày chiếu: " + selectedShowDate);
        }
    }
    
    /**
     * Gọi API để lấy danh sách suất chiếu theo ngày
     */
    private void fetchScreenings(Long movieId, String date) {
        MovieApiService movieApiService = ApiClient.getMovieApiService();
        Call<ScreeningListResponse> call = movieApiService.getScreeningsByDate(movieId, date);
        
        call.enqueue(new Callback<ScreeningListResponse>() {
            @Override
            public void onResponse(Call<ScreeningListResponse> call, Response<ScreeningListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScreeningListResponse screeningResponse = response.body();
                    if (screeningResponse.getSuccess() != null && screeningResponse.getSuccess() 
                            && screeningResponse.getData() != null) {
                        updateScreeningsUI(screeningResponse.getData());
                    } else {
                        showError("Không thể lấy danh sách suất chiếu");
                    }
                } else {
                    showError("Không thể lấy danh sách suất chiếu");
                }
            }
            
            @Override
            public void onFailure(Call<ScreeningListResponse> call, Throwable t) {
                showError("Không thể kết nối đến server");
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Cập nhật UI với danh sách suất chiếu từ API
     */
    private void updateScreeningsUI(List<ScreeningListResponse.ScreeningData> screenings) {
        LinearLayout container = findViewById(R.id.container_cinemas);
        if (container == null) return;
        
        // Xóa tất cả items cũ
        container.removeAllViews();
        
        if (screenings == null || screenings.isEmpty()) {
            // Hiển thị thông báo không có suất chiếu
            TextView noData = new TextView(this);
            noData.setText("Không có suất chiếu nào trong ngày này");
            noData.setTextColor(getColor(R.color.movie_text_secondary));
            noData.setTextSize(14);
            noData.setPadding(0, 32, 0, 32);
            container.addView(noData);
            
            // Cập nhật số lượng rạp
            TextView tvCinemaCount = findViewById(R.id.tv_cinema_count);
            if (tvCinemaCount != null) {
                tvCinemaCount.setText("0 rạp chiếu");
            }
            return;
        }

        // Cập nhật số lượng rạp
        TextView tvCinemaCount = findViewById(R.id.tv_cinema_count);
        if (tvCinemaCount != null) {
            tvCinemaCount.setText(screenings.size() + " rạp chiếu");
        }

        // Tạo cinema items động từ API
        for (ScreeningListResponse.ScreeningData cinemaData : screenings) {
            LinearLayout cinemaItem = createCinemaItem(cinemaData);
            container.addView(cinemaItem);
        }
    }
    
    /**
     * Tạo cinema item động
     */
    private LinearLayout createCinemaItem(ScreeningListResponse.ScreeningData cinemaData) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setBackgroundResource(R.drawable.bg_movie_cinema_card);
        itemLayout.setElevation(4);
        
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        itemParams.setMargins(0, (int)(12 * getResources().getDisplayMetrics().density), 0, 0);
        itemLayout.setLayoutParams(itemParams);
        int padding = (int)(12 * getResources().getDisplayMetrics().density);
        itemLayout.setPadding(padding, padding, padding, padding);
        
        // Header row (Name + Distance)
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        // Favorite icon
        ImageView ivFavorite = new ImageView(this);
        ivFavorite.setLayoutParams(new LinearLayout.LayoutParams(
                (int)(20 * getResources().getDisplayMetrics().density),
                (int)(20 * getResources().getDisplayMetrics().density)
        ));
        ivFavorite.setImageResource(R.drawable.ic_heart_outline);
        ivFavorite.setColorFilter(getColor(R.color.movie_text_hint));
        headerRow.addView(ivFavorite);
        
        // Cinema name + address container
        LinearLayout nameContainer = new LinearLayout(this);
        nameContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        nameParams.setMargins((int)(8 * getResources().getDisplayMetrics().density), 0, 0, 0);
        nameContainer.setLayoutParams(nameParams);
        
        // Cinema name
        TextView tvName = new TextView(this);
        tvName.setText(cinemaData.getCinemaName() != null ? cinemaData.getCinemaName() : "");
        tvName.setTextColor(getColor(R.color.movie_text_primary));
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        nameContainer.addView(tvName);
        
        // Cinema address
        TextView tvAddress = new TextView(this);
        tvAddress.setText(cinemaData.getCinemaAddress() != null ? cinemaData.getCinemaAddress() : "");
        tvAddress.setTextColor(getColor(R.color.movie_text_secondary));
        tvAddress.setTextSize(12);
        tvAddress.setMaxLines(1);
        tvAddress.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        addressParams.setMargins(0, (int)(2 * getResources().getDisplayMetrics().density), 0, 0);
        tvAddress.setLayoutParams(addressParams);
        nameContainer.addView(tvAddress);
        
        headerRow.addView(nameContainer);
        
        // Distance (placeholder)
        TextView tvDistance = new TextView(this);
        tvDistance.setText("--km");
        tvDistance.setTextColor(getColor(R.color.movie_primary_green));
        tvDistance.setTextSize(13);
        tvDistance.setTypeface(null, android.graphics.Typeface.BOLD);
        headerRow.addView(tvDistance);
        
        itemLayout.addView(headerRow);
        
        // Format label
        TextView tvFormat = new TextView(this);
        tvFormat.setText("2D - Phụ đề");
        tvFormat.setTextColor(getColor(R.color.movie_text_primary));
        tvFormat.setTextSize(13);
        tvFormat.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams formatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        formatParams.setMargins(0, (int)(12 * getResources().getDisplayMetrics().density), 0, 0);
        tvFormat.setLayoutParams(formatParams);
        itemLayout.addView(tvFormat);
        
        // Showtimes row
        LinearLayout showtimeRow = new LinearLayout(this);
        showtimeRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams showtimeRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        showtimeRowParams.setMargins(0, (int)(8 * getResources().getDisplayMetrics().density), 0, 0);
        showtimeRow.setLayoutParams(showtimeRowParams);
        
        // Add showtime buttons
                List<ScreeningListResponse.ScreeningItem> screeningItems = cinemaData.getScreenings();
                if (screeningItems != null && !screeningItems.isEmpty()) {
                    for (ScreeningListResponse.ScreeningItem screening : screeningItems) {
                        if (screening.getStartTime() != null) {
                            Button showtimeButton = createShowtimeButton(
                                    screening.getStartTime(),
                                    cinemaData.getCinemaName(),
                                    cinemaData.getCinemaAddress(),
                                    screening.getHallName(),
                                    screening.getScreeningId()
                            );
                    showtimeRow.addView(showtimeButton);
                }
            }
        }
        
        itemLayout.addView(showtimeRow);
        
        return itemLayout;
    }

    /**
     * Tạo button showtime động
     */
    private Button createShowtimeButton(String startTime, String cinemaName, 
                                        String cinemaAddress, String hallName, Long screeningId) {
        Button button = new Button(this);
        button.setText(startTime);
        button.setBackgroundResource(R.drawable.bg_movie_showtime_light);
        button.setTextColor(getColor(R.color.movie_text_primary));
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setPadding(32, 0, 32, 0);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) (36 * getResources().getDisplayMetrics().density)
        );
        params.setMargins(0, 0, 16, 0);
        button.setLayoutParams(params);

        // Gắn click listener
        button.setOnClickListener(v -> {
            openSeatSelection(
                    cinemaName,
                    cinemaAddress,
                    startTime,
                    selectedShowDate,
                    hallName != null ? hallName : "",
                    screeningId
            );
        });

        return button;
    }

    /**
     * Xóa tất cả cinema items trong container
     */
    private void hideAllCinemaItems() {
        LinearLayout container = findViewById(R.id.container_cinemas);
        if (container != null) {
            container.removeAllViews();
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Chuyển đổi ngày từ format DD/MM/YYYY sang YYYY-MM-DD
     */
    private String convertDateToAPIFormat(String date) {
        try {
            // Format: "18/12/2025" -> "2025-12-18"
            String[] parts = date.split("/");
            if (parts.length == 3) {
                return parts[2] + "-" + parts[1] + "-" + parts[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Cập nhật TextView với ngày và label thứ
     */
    private void updateDateItem(Calendar calendar, TextView numberView, TextView labelView,
                               SimpleDateFormat dayFormat, SimpleDateFormat dayLabelFormat) {
        if (numberView != null) {
            numberView.setText(dayFormat.format(calendar.getTime()));
        }
        if (labelView != null) {
            // Sử dụng Calendar.DAY_OF_WEEK để lấy thứ chính xác (không phụ thuộc locale)
            String vietnameseLabel = getDayOfWeekLabel(calendar);
            labelView.setText(vietnameseLabel);
        }
    }

    /**
     * Lấy label thứ tiếng Việt từ Calendar (T2, T3, T4, T5, T6, T7, CN)
     * Sử dụng Calendar.DAY_OF_WEEK thay vì SimpleDateFormat để tránh lỗi locale
     */
    private String getDayOfWeekLabel(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "T2";
            case Calendar.TUESDAY: return "T3";
            case Calendar.WEDNESDAY: return "T4";
            case Calendar.THURSDAY: return "T5";
            case Calendar.FRIDAY: return "T6";
            case Calendar.SATURDAY: return "T7";
            case Calendar.SUNDAY: return "CN";
            default: return "";
        }
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
        // Cập nhật các ngày hiển thị dựa trên ngày hiện tại (7 ngày liên tiếp)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat dayLabelFormat = new SimpleDateFormat("E", Locale.getDefault());
        
        // Ngày 18 (ngày hiện tại)
        LinearLayout date18 = findViewById(R.id.date_item_18);
        TextView num18 = findViewById(R.id.tv_date_18_number);
        TextView lbl18 = findViewById(R.id.tv_date_18_label);
        updateDateItem(calendar, num18, lbl18, dayFormat, dayLabelFormat);
        // Highlight ngày hiện tại
        if (date18 != null && num18 != null && lbl18 != null) {
            highlightDate(date18, num18, lbl18);
        }

        // Ngày 19 (ngày hiện tại + 1)
        LinearLayout date19 = findViewById(R.id.date_item_19);
        TextView num19 = findViewById(R.id.tv_date_19_number);
        TextView lbl19 = findViewById(R.id.tv_date_19_label);
        Calendar cal19 = (Calendar) calendar.clone();
        cal19.add(Calendar.DAY_OF_MONTH, 1);
        updateDateItem(cal19, num19, lbl19, dayFormat, dayLabelFormat);

        // Ngày 20 (ngày hiện tại + 2)
        LinearLayout date20 = findViewById(R.id.date_item_20);
        TextView num20 = findViewById(R.id.tv_date_20_number);
        TextView lbl20 = findViewById(R.id.tv_date_20_label);
        Calendar cal20 = (Calendar) calendar.clone();
        cal20.add(Calendar.DAY_OF_MONTH, 2);
        updateDateItem(cal20, num20, lbl20, dayFormat, dayLabelFormat);

        // Ngày 21 (ngày hiện tại + 3)
        LinearLayout date21 = findViewById(R.id.date_item_21);
        TextView num21 = findViewById(R.id.tv_date_21_number);
        TextView lbl21 = findViewById(R.id.tv_date_21_label);
        Calendar cal21 = (Calendar) calendar.clone();
        cal21.add(Calendar.DAY_OF_MONTH, 3);
        updateDateItem(cal21, num21, lbl21, dayFormat, dayLabelFormat);

        // Ngày 22 (ngày hiện tại + 4)
        LinearLayout date22 = findViewById(R.id.date_item_22);
        TextView num22 = findViewById(R.id.tv_date_22_number);
        TextView lbl22 = findViewById(R.id.tv_date_22_label);
        Calendar cal22 = (Calendar) calendar.clone();
        cal22.add(Calendar.DAY_OF_MONTH, 4);
        updateDateItem(cal22, num22, lbl22, dayFormat, dayLabelFormat);

        // Ngày 23 (ngày hiện tại + 5)
        LinearLayout date23 = findViewById(R.id.date_item_23);
        TextView num23 = findViewById(R.id.tv_date_23_number);
        TextView lbl23 = findViewById(R.id.tv_date_23_label);
        Calendar cal23 = (Calendar) calendar.clone();
        cal23.add(Calendar.DAY_OF_MONTH, 5);
        updateDateItem(cal23, num23, lbl23, dayFormat, dayLabelFormat);

        // Ngày 24 (ngày hiện tại + 6)
        LinearLayout date24 = findViewById(R.id.date_item_24);
        TextView num24 = findViewById(R.id.tv_date_24_number);
        TextView lbl24 = findViewById(R.id.tv_date_24_label);
        Calendar cal24 = (Calendar) calendar.clone();
        cal24.add(Calendar.DAY_OF_MONTH, 6);
        updateDateItem(cal24, num24, lbl24, dayFormat, dayLabelFormat);

        // Lưu trữ Calendar cho mỗi ngày để dễ dàng truy cập
        final Calendar[] dateCalendars = {
                calendar,      // date18 (ngày hiện tại)
                cal19,        // date19
                cal20,        // date20
                cal21,        // date21
                cal22,        // date22
                cal23,        // date23
                cal24         // date24
        };
        
        final LinearLayout[] dateLayouts = {date18, date19, date20, date21, date22, date23, date24};
        final TextView[] dateNumbers = {num18, num19, num20, num21, num22, num23, num24};
        final TextView[] dateLabels = {lbl18, lbl19, lbl20, lbl21, lbl22, lbl23, lbl24};

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

            // Tìm index của date item được click
            for (int i = 0; i < dateLayouts.length; i++) {
                if (v == dateLayouts[i]) {
                    // Lấy ngày từ Calendar tương ứng TRƯỚC KHI highlight
                    Calendar selectedCal = dateCalendars[i];
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedShowDate = displayFormat.format(selectedCal.getTime());
                    selectedShowDateAPI = apiFormat.format(selectedCal.getTime());
                    
                    // Highlight sau khi đã update selectedShowDate
                    highlightDate(dateLayouts[i], dateNumbers[i], dateLabels[i]);
                    
                    if (movieId != null) {
                        fetchScreenings(movieId, selectedShowDateAPI);
                    }
                    break;
                }
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
        container.setBackgroundResource(R.drawable.bg_movie_date_light_inactive);
        number.setTextColor(getColor(R.color.movie_text_primary));
        label.setTextColor(getColor(R.color.movie_text_secondary));
    }

    private void highlightDate(LinearLayout container, TextView number, TextView label) {
        container.setBackgroundResource(R.drawable.bg_movie_date_light_active);
        number.setTextColor(0xFFFFFFFF);
        label.setTextColor(0xFFFFFFFF);
        
        // Cập nhật label ngày chiếu
        updateCurrentDateLabel();
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
        if (container == null || icon == null) return;
        container.setBackgroundResource(R.drawable.bg_movie_brand_light);
        
        // Only apply color filter to vector icons (ic_location), not bitmap logo icons
        if (isLogoIcon(icon)) {
            icon.clearColorFilter(); // Clear any existing filters for logo icons
        } else {
            icon.setColorFilter(getColor(R.color.movie_primary_green));
        }
        
        if (label != null) {
            label.setTextColor(getColor(R.color.movie_text_primary));
        }
    }

    private void highlightBrand(LinearLayout container, ImageView icon, TextView label) {
        if (container == null || icon == null) return;
        container.setBackgroundResource(R.drawable.bg_movie_brand_light_active);
        
        // Only apply color filter to vector icons (ic_location), not bitmap logo icons
        if (isLogoIcon(icon)) {
            icon.clearColorFilter(); // Clear any existing filters for logo icons
        } else {
            icon.setColorFilter(0xFFFFFFFF); // Apply white filter for vector icons
        }
        
        if (label != null) {
        label.setTextColor(0xFFFFFFFF);
        }
    }


    private void openSeatSelection(String cinemaName,
                                   String cinemaAddress,
                                   String showtime,
                                   String showDate,
                                   String room,
                                   Long screeningId) {
        Intent fromIntent = getIntent();
        String movieTitle = fromIntent != null ? fromIntent.getStringExtra(EXTRA_MOVIE_TITLE) : null;

        Intent intent = new Intent(this, SeatSelectionActivity.class);
        intent.putExtra(SeatSelectionActivity.EXTRA_SCREENING_ID, screeningId);
        intent.putExtra(SeatSelectionActivity.EXTRA_MOVIE_TITLE, movieTitle);
        intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_NAME, cinemaName);
        intent.putExtra(SeatSelectionActivity.EXTRA_CINEMA_ADDRESS, cinemaAddress);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWTIME, showtime);
        intent.putExtra(SeatSelectionActivity.EXTRA_SHOWDATE, showDate);
        intent.putExtra(SeatSelectionActivity.EXTRA_ROOM, room);
        startActivity(intent);
    }
}


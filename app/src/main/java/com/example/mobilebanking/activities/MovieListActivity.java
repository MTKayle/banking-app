package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * Movie List Activity - Dark mode movie list inside banking super app.
 * Hiển thị danh sách phim mẫu và điều hướng sang màn chi tiết phim.
 * FRONTEND ONLY: chỉ xử lý UI và chuyển màn, không gọi API / backend.
 */
public class MovieListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list_dark);

        setupNavigation();
        setupTabs();
    }

    private void setupNavigation() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Card containers
        LinearLayout itemKingOfKings = findViewById(R.id.item_movie_king_of_kings);
        LinearLayout itemAvatar3 = findViewById(R.id.item_movie_avatar3);
        LinearLayout itemTotoro = findViewById(R.id.item_movie_totoro);

        // "Đặt vé" buttons
        Button btnBookKingOfKings = findViewById(R.id.btn_book_king_of_kings);
        Button btnBookAvatar3 = findViewById(R.id.btn_book_avatar3);
        Button btnBookTotoro = findViewById(R.id.btn_book_totoro);

        View.OnClickListener kingOfKingsClick = v -> openMovieDetail(
                "VUA CỦA CÁC VUA (T13)",
                "Gia đình, Hoạt hình",
                "101 phút",
                "T13",
                R.drawable.home_banner_1
        );

        View.OnClickListener avatar3Click = v -> openMovieDetail(
                "AVATAR 3: LỬA VÀ TRO TÀN (T13)",
                "Khoa học viễn tưởng, Hành động",
                "180 phút",
                "T13",
                R.drawable.home_banner_2
        );

        View.OnClickListener totoroClick = v -> openMovieDetail(
                "PHIM ĐIỆN ẢNH HÀNG XÓM CỦA TÔI TOTORO (P)",
                "Hoạt hình, Phiêu lưu",
                "87 phút",
                "P",
                R.drawable.home_banner_3
        );

        if (itemKingOfKings != null) itemKingOfKings.setOnClickListener(kingOfKingsClick);
        if (btnBookKingOfKings != null) btnBookKingOfKings.setOnClickListener(kingOfKingsClick);

        if (itemAvatar3 != null) itemAvatar3.setOnClickListener(avatar3Click);
        if (btnBookAvatar3 != null) btnBookAvatar3.setOnClickListener(avatar3Click);

        if (itemTotoro != null) itemTotoro.setOnClickListener(totoroClick);
        if (btnBookTotoro != null) btnBookTotoro.setOnClickListener(totoroClick);
    }

    /**
     * Đăng ký click cho 2 tab Đang chiếu / Sắp chiếu để người dùng có cảm giác chuyển màn,
     * hiện tại chỉ đổi style (demo, chưa lọc danh sách phim thực tế).
     */
    private void setupTabs() {
        TextView tabNow = findViewById(R.id.tab_now_showing);
        TextView tabComing = findViewById(R.id.tab_coming_soon);

        if (tabNow == null || tabComing == null) return;

        View.OnClickListener listener = v -> {
            if (v == tabNow) {
                // Kích hoạt "Đang chiếu"
                tabNow.setBackgroundColor(0xFF0EB378);
                tabNow.setTextColor(0xFFFFFFFF);

                tabComing.setBackgroundColor(0xFF26313A);
                tabComing.setTextColor(0xFFB0BAC5);
            } else if (v == tabComing) {
                // Kích hoạt "Sắp chiếu"
                tabComing.setBackgroundColor(0xFF0EB378);
                tabComing.setTextColor(0xFFFFFFFF);

                tabNow.setBackgroundColor(0xFF26313A);
                tabNow.setTextColor(0xFFB0BAC5);
            }
            // Demo: danh sách phim hiện dùng chung cho cả 2 tab, chỉ khác trạng thái tab.
        };

        tabNow.setOnClickListener(listener);
        tabComing.setOnClickListener(listener);
    }

    private void openMovieDetail(String title,
                                 String genre,
                                 String duration,
                                 String ageRating,
                                 int posterResId) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE, title);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_GENRE, genre);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_DURATION, duration);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_AGE_RATING, ageRating);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_RES_ID, posterResId);
        startActivity(intent);
    }
}


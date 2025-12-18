package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

import java.util.Locale;

/**
 * Movie List Activity - Dark mode movie list inside banking super app.
 * Hiển thị danh sách phim mẫu và điều hướng sang màn chi tiết phim.
 * FRONTEND ONLY: chỉ xử lý UI và chuyển màn, không gọi API / backend.
 */
public class MovieListActivity extends AppCompatActivity {

    private LinearLayout itemKingOfKings, itemAvatar3, itemTotoro,
            itemExtraDownload, itemExtraImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list_dark);

        setupNavigation();
        setupTabs();
        setupSearch();
    }

    private void setupNavigation() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Card containers
        itemKingOfKings = findViewById(R.id.item_movie_king_of_kings);
        itemAvatar3 = findViewById(R.id.item_movie_avatar3);
        itemTotoro = findViewById(R.id.item_movie_totoro);
        itemExtraDownload = findViewById(R.id.item_movie_extra_download);
        itemExtraImages = findViewById(R.id.item_movie_extra_images);

        // "Đặt vé" buttons
        Button btnBookKingOfKings = findViewById(R.id.btn_book_king_of_kings);
        Button btnBookAvatar3 = findViewById(R.id.btn_book_avatar3);
        Button btnBookTotoro = findViewById(R.id.btn_book_totoro);
        Button btnBookExtraDownload = findViewById(R.id.btn_book_extra_download);
        Button btnBookExtraImages = findViewById(R.id.btn_book_extra_images);

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

        View.OnClickListener extraDownloadClick = v -> openMovieDetail(
                "HÀNH TRÌNH BÍ ẨN (T16)",
                "Hành động, Phiêu lưu",
                "120 phút",
                "T16",
                R.drawable.download_1
        );

        View.OnClickListener extraImagesClick = v -> openMovieDetail(
                "KỲ NGHỈ TRONG MƠ (P)",
                "Gia đình, Hài hước",
                "95 phút",
                "P",
                R.drawable.images_1
        );

        if (itemKingOfKings != null) itemKingOfKings.setOnClickListener(kingOfKingsClick);
        if (btnBookKingOfKings != null) btnBookKingOfKings.setOnClickListener(kingOfKingsClick);

        if (itemAvatar3 != null) itemAvatar3.setOnClickListener(avatar3Click);
        if (btnBookAvatar3 != null) btnBookAvatar3.setOnClickListener(avatar3Click);

        if (itemTotoro != null) itemTotoro.setOnClickListener(totoroClick);
        if (btnBookTotoro != null) btnBookTotoro.setOnClickListener(totoroClick);

        if (itemExtraDownload != null) itemExtraDownload.setOnClickListener(extraDownloadClick);
        if (btnBookExtraDownload != null) btnBookExtraDownload.setOnClickListener(extraDownloadClick);

        if (itemExtraImages != null) itemExtraImages.setOnClickListener(extraImagesClick);
        if (btnBookExtraImages != null) btnBookExtraImages.setOnClickListener(extraImagesClick);
    }

    /**
     * Đăng ký click cho 2 tab Đang chiếu / Sắp chiếu để người dùng có cảm giác chuyển màn,
     * hiện tại chỉ đổi style (demo, chưa lọc danh sách phim thực tế).
     */
    private void setupTabs() {
        TextView tabNow = findViewById(R.id.tab_now_showing);
        TextView tabComing = findViewById(R.id.tab_coming_soon);

        if (tabNow == null || tabComing == null) return;

        // Trạng thái mặc định: Đang chiếu được chọn
        tabNow.setBackgroundResource(R.drawable.bg_movie_tab_active);
        tabNow.setTextColor(0xFFFFFFFF);
        tabComing.setBackgroundResource(R.drawable.bg_movie_tab_inactive);
        tabComing.setTextColor(getColor(R.color.bidv_text_secondary));

        View.OnClickListener listener = v -> {
            if (v == tabNow) {
                // Kích hoạt \"Đang chiếu\"
                tabNow.setBackgroundResource(R.drawable.bg_movie_tab_active);
                tabNow.setTextColor(0xFFFFFFFF);

                tabComing.setBackgroundResource(R.drawable.bg_movie_tab_inactive);
                tabComing.setTextColor(getColor(R.color.bidv_text_secondary));
            } else if (v == tabComing) {
                // Kích hoạt \"Sắp chiếu\"
                tabComing.setBackgroundResource(R.drawable.bg_movie_tab_active);
                tabComing.setTextColor(0xFFFFFFFF);

                tabNow.setBackgroundResource(R.drawable.bg_movie_tab_inactive);
                tabNow.setTextColor(getColor(R.color.bidv_text_secondary));
            }
            // Demo: danh sách phim hiện dùng chung cho cả 2 tab, chỉ khác trạng thái tab.
        };

        tabNow.setOnClickListener(listener);
        tabComing.setOnClickListener(listener);
    }

    /**
     * Tìm kiếm phim theo tiêu đề (chứa từ khóa, không phân biệt hoa/thường)
     */
    private void setupSearch() {
        TextView etSearch = findViewById(R.id.et_search);
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase(Locale.getDefault()).trim();

                filterMovie(itemKingOfKings, "VUA CỦA CÁC VUA (T13)", query);
                filterMovie(itemAvatar3, "AVATAR 3: LỬA VÀ TRO TÀN (T13)", query);
                filterMovie(itemTotoro, "PHIM ĐIỆN ẢNH HÀNG XÓM CỦA TÔI TOTORO (P)", query);
                filterMovie(itemExtraDownload, "HÀNH TRÌNH BÍ ẨN (T16)", query);
                filterMovie(itemExtraImages, "KỲ NGHỈ TRONG MƠ (P)", query);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filterMovie(LinearLayout item, String title, String query) {
        if (item == null) return;
        if (query.isEmpty() || title.toLowerCase(Locale.getDefault()).contains(query)) {
            item.setVisibility(View.VISIBLE);
        } else {
            item.setVisibility(View.GONE);
        }
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


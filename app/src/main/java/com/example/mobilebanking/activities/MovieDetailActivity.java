package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * MovieDetailActivity
 * FRONTEND ONLY: Hiển thị chi tiết phim và điều hướng sang màn chọn ngày/rạp.
 */
public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_MOVIE_GENRE = "extra_movie_genre";
    public static final String EXTRA_MOVIE_DURATION = "extra_movie_duration";
    public static final String EXTRA_MOVIE_AGE_RATING = "extra_movie_age_rating";
    public static final String EXTRA_MOVIE_POSTER_RES_ID = "extra_movie_poster_res_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail_dark);

        bindDataFromIntent();
        setupActions();
    }

    private void bindDataFromIntent() {
        ImageView ivPoster = findViewById(R.id.iv_movie_poster);
        TextView tvTitle = findViewById(R.id.tv_movie_title);
        TextView tvGenre = findViewById(R.id.tv_movie_genre);
        TextView tvDuration = findViewById(R.id.tv_duration);
        TextView tvAgeRating = findViewById(R.id.tv_age_rating);

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String title = intent.getStringExtra(EXTRA_MOVIE_TITLE);
        String genre = intent.getStringExtra(EXTRA_MOVIE_GENRE);
        String duration = intent.getStringExtra(EXTRA_MOVIE_DURATION);
        String ageRating = intent.getStringExtra(EXTRA_MOVIE_AGE_RATING);
        int posterResId = intent.getIntExtra(EXTRA_MOVIE_POSTER_RES_ID, R.drawable.home_banner_1);

        if (title != null) {
            tvTitle.setText(title);
        }
        if (genre != null) {
            tvGenre.setText(genre);
        }
        if (duration != null) {
            tvDuration.setText(duration);
        }
        if (ageRating != null) {
            tvAgeRating.setText(ageRating);
        }
        ivPoster.setImageResource(posterResId);
    }

    private void setupActions() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        findViewById(R.id.btn_book_now).setOnClickListener(v -> {
            Intent currentIntent = getIntent();
            String movieTitle = currentIntent != null
                    ? currentIntent.getStringExtra(EXTRA_MOVIE_TITLE)
                    : null;

            Intent intent = new Intent(this, SelectShowtimeActivity.class);
            intent.putExtra(SelectShowtimeActivity.EXTRA_MOVIE_TITLE, movieTitle);
            startActivity(intent);
        });
    }
}



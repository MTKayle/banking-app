package com.example.mobilebanking.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.dto.MovieDetailResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MovieDetailActivity
 * FRONTEND ONLY: Hiển thị chi tiết phim và điều hướng sang màn chọn ngày/rạp.
 */
public class MovieDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_MOVIE_GENRE = "extra_movie_genre";
    public static final String EXTRA_MOVIE_DURATION = "extra_movie_duration";
    public static final String EXTRA_MOVIE_AGE_RATING = "extra_movie_age_rating";
    public static final String EXTRA_MOVIE_POSTER_RES_ID = "extra_movie_poster_res_id";
    
    private Long movieId;
    private String trailerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail_light);

        Intent intent = getIntent();
        if (intent != null) {
            movieId = intent.hasExtra(EXTRA_MOVIE_ID) 
                    ? intent.getLongExtra(EXTRA_MOVIE_ID, -1) 
                    : null;
        }
        
        if (movieId != null && movieId > 0) {
            // Gọi API lấy chi tiết phim
            fetchMovieDetail(movieId);
        } else {
            // Sử dụng dữ liệu từ Intent (fallback)
            bindDataFromIntent();
        }
        
        setupActions();
        setupTrailerButton();
    }
    
    /**
     * Setup nút xem trailer
     */
    private void setupTrailerButton() {
        View btnWatchTrailer = findViewById(R.id.btn_watch_trailer);
        if (btnWatchTrailer != null) {
            btnWatchTrailer.setOnClickListener(v -> {
                if (trailerUrl != null && !trailerUrl.isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể mở trailer", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Trailer chưa có sẵn", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Gọi API để lấy chi tiết phim
     */
    private void fetchMovieDetail(Long movieId) {
        MovieApiService movieApiService = ApiClient.getMovieApiService();
        Call<MovieDetailResponse> call = movieApiService.getMovieDetail(movieId);
        
        call.enqueue(new Callback<MovieDetailResponse>() {
            @Override
            public void onResponse(Call<MovieDetailResponse> call, Response<MovieDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailResponse movieResponse = response.body();
                    if (movieResponse.getSuccess() != null && movieResponse.getSuccess() 
                            && movieResponse.getData() != null) {
                        bindDataFromAPI(movieResponse.getData());
                    } else {
                        showError("Không thể lấy chi tiết phim");
                        bindDataFromIntent(); // Fallback
                    }
                } else {
                    showError("Không thể lấy chi tiết phim");
                    bindDataFromIntent(); // Fallback
                }
            }
            
            @Override
            public void onFailure(Call<MovieDetailResponse> call, Throwable t) {
                showError("Không thể kết nối đến server");
                bindDataFromIntent(); // Fallback
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Hiển thị dữ liệu phim từ API
     */
    private void bindDataFromAPI(MovieDetailResponse.MovieDetailData movie) {
        // Basic info
        ImageView ivPoster = findViewById(R.id.iv_movie_poster);
        TextView tvTitle = findViewById(R.id.tv_movie_title);
        TextView tvGenre = findViewById(R.id.tv_movie_genre);
        TextView tvDuration = findViewById(R.id.tv_duration);
        TextView tvAgeRating = findViewById(R.id.tv_age_rating);
        
        // Description
        TextView tvDescription = findViewById(R.id.tv_movie_description);
        TextView tvReadMore = findViewById(R.id.tv_read_more);
        
        // Technical info
        TextView tvDirector = findViewById(R.id.tv_director);
        TextView tvActors = findViewById(R.id.tv_actors);
        TextView tvLanguage = findViewById(R.id.tv_language);
        TextView tvReleaseDate = findViewById(R.id.tv_release_date);
        
        // Screening types
        TextView chipFormat2d = findViewById(R.id.chip_format_2d);
        TextView chipFormat3d = findViewById(R.id.chip_format_3d);

        // Title
        if (movie.getTitle() != null && tvTitle != null) {
            tvTitle.setText(movie.getTitle());
        }
        
        // Genre
        if (movie.getGenreDisplay() != null && tvGenre != null) {
            tvGenre.setText(movie.getGenreDisplay());
        }
        
        // Duration
        if (movie.getDurationMinutes() != null && tvDuration != null) {
            tvDuration.setText(movie.getDurationMinutes() + " phút");
        }
        
        // Age rating
        if (movie.getAgeRating() != null && tvAgeRating != null) {
            String ageRatingText = "T" + movie.getAgeRating();
            if (movie.getAgeRating() == 0) {
                ageRatingText = "P";
            }
            tvAgeRating.setText(ageRatingText);
        }
        
        // Description
        if (movie.getDescription() != null && tvDescription != null) {
            tvDescription.setText(movie.getDescription());
            // Show "Đọc thêm" if description is long
            if (tvReadMore != null) {
                tvReadMore.setVisibility(movie.getDescription().length() > 100 ? View.VISIBLE : View.GONE);
                tvReadMore.setOnClickListener(v -> {
                    if (tvDescription.getMaxLines() == 4) {
                        tvDescription.setMaxLines(Integer.MAX_VALUE);
                        tvReadMore.setText("Thu gọn");
                    } else {
                        tvDescription.setMaxLines(4);
                        tvReadMore.setText("Đọc thêm");
                    }
                });
            }
        }
        
        // Director
        if (movie.getDirector() != null && tvDirector != null) {
            tvDirector.setText(movie.getDirector());
        }
        
        // Cast/Actors
        if (movie.getCast() != null && tvActors != null) {
            tvActors.setText(movie.getCast());
        }
        
        // Language
        if (movie.getLanguageDisplay() != null && tvLanguage != null) {
            String languageText = movie.getLanguageDisplay();
            // Có thể thêm "Phụ đề Tiếng Việt" nếu cần
            tvLanguage.setText(languageText);
        }
        
        // Release date
        if (movie.getReleaseDate() != null && tvReleaseDate != null) {
            // Format date from YYYY-MM-DD to DD/MM/YYYY
            String formattedDate = formatDate(movie.getReleaseDate());
            tvReleaseDate.setText(formattedDate);
        }
        
        // Screening types (2D, 3D, IMAX, etc.)
        if (movie.getScreeningTypes() != null && !movie.getScreeningTypes().isEmpty()) {
            List<String> types = movie.getScreeningTypes();
            
            // Show/hide format chips based on available types
            if (chipFormat2d != null) {
                boolean has2d = types.contains("TWO_D");
                chipFormat2d.setVisibility(has2d ? View.VISIBLE : View.GONE);
            }
            
            if (chipFormat3d != null) {
                boolean has3d = types.contains("THREE_D") || types.contains("IMAX_3D");
                chipFormat3d.setVisibility(has3d ? View.VISIBLE : View.GONE);
            }
            
            // Có thể thêm các format khác nếu cần (IMAX, 4DX, etc.)
        } else {
            // Hide format chips if no screening types
            if (chipFormat2d != null) chipFormat2d.setVisibility(View.GONE);
            if (chipFormat3d != null) chipFormat3d.setVisibility(View.GONE);
        }
        
        // Load poster image from URL using Glide
        if (ivPoster != null) {
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                Glide.with(this)
                        .load(movie.getPosterUrl())
                        .placeholder(R.drawable.home_banner_1) // Placeholder while loading
                        .error(R.drawable.home_banner_1) // Error image if load fails
                        .centerCrop()
                        .into(ivPoster);
            } else {
                // Use default image if no URL
                ivPoster.setImageResource(R.drawable.home_banner_1);
            }
        }
        
        // Store trailer URL for later use
        if (movie.getTrailerUrl() != null && !movie.getTrailerUrl().isEmpty()) {
            this.trailerUrl = movie.getTrailerUrl();
        }
        
        // Update movie title in Intent for navigation
        if (movie.getTitle() != null) {
            getIntent().putExtra(EXTRA_MOVIE_TITLE, movie.getTitle());
        }
    }
    
    /**
     * Format date from YYYY-MM-DD to DD/MM/YYYY
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Return original if parsing fails
        return dateStr;
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
            intent.putExtra(SelectShowtimeActivity.EXTRA_MOVIE_ID, movieId);
            intent.putExtra(SelectShowtimeActivity.EXTRA_MOVIE_TITLE, movieTitle);
            startActivity(intent);
        });
    }
}



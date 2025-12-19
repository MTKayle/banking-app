package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.MovieAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.dto.MovieListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Movie List Activity - Dark mode movie list inside banking super app.
 * Hiển thị danh sách phim từ API và điều hướng sang màn chi tiết phim.
 */
public class MovieListActivity extends AppCompatActivity {

    private RecyclerView rvMovies;
    private MovieAdapter movieAdapter;
    private List<MovieListResponse.MovieItem> movieList = new ArrayList<>();
    private TextView etSearch;
    private ViewPager2 vpBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list_light);

        setupRecyclerView();
        setupBanner();
        setupNavigation();
        setupTabs();
        setupSearch();
        fetchMovies();
    }
    
    /**
     * Setup banner carousel
     */
    private void setupBanner() {
        vpBanner = findViewById(R.id.vp_banner);
        // Banner sẽ được update khi có dữ liệu từ API
    }
    
    /**
     * Update banner với phim đầu tiên trong danh sách
     */
    private void updateBannerFromMovies() {
        if (movieList == null || movieList.isEmpty()) return;
        
        // Lấy phim đầu tiên làm banner
        MovieListResponse.MovieItem firstMovie = movieList.get(0);
        
        // Update banner info
        TextView tvBannerTitle = findViewById(R.id.tv_banner_title);
        TextView tvBannerDuration = findViewById(R.id.tv_banner_duration);
        TextView tvBannerAge = findViewById(R.id.tv_banner_age);
        Button btnBannerBook = findViewById(R.id.btn_banner_book);
        
        if (tvBannerTitle != null && firstMovie.getTitle() != null) {
            String title = firstMovie.getTitle();
            if (firstMovie.getAgeRating() != null) {
                title += " (T" + firstMovie.getAgeRating() + ")";
            }
            tvBannerTitle.setText(title);
        }
        
        if (tvBannerDuration != null && firstMovie.getDurationMinutes() != null) {
            tvBannerDuration.setText(firstMovie.getDurationMinutes() + " phút");
        }
        
        if (tvBannerAge != null && firstMovie.getAgeRating() != null) {
            tvBannerAge.setText("T" + firstMovie.getAgeRating());
        }
        
        // Setup banner click
        if (btnBannerBook != null) {
            btnBannerBook.setOnClickListener(v -> openMovieDetailFromAPI(firstMovie));
        }
        
        // Setup banner adapter để hiển thị poster
        if (vpBanner != null) {
            List<MovieListResponse.MovieItem> bannerMovies = movieList.subList(0, Math.min(3, movieList.size()));
            BannerAdapter bannerAdapter = new BannerAdapter(bannerMovies);
            vpBanner.setAdapter(bannerAdapter);
            
            // Cập nhật thông tin banner khi swipe
            vpBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (position >= 0 && position < bannerMovies.size()) {
                        MovieListResponse.MovieItem currentMovie = bannerMovies.get(position);
                        updateBannerInfo(currentMovie);
                    }
                }
            });
        }
    }
    
    /**
     * Cập nhật thông tin hiển thị trên banner
     */
    private void updateBannerInfo(MovieListResponse.MovieItem movie) {
        TextView tvBannerTitle = findViewById(R.id.tv_banner_title);
        TextView tvBannerDuration = findViewById(R.id.tv_banner_duration);
        TextView tvBannerAge = findViewById(R.id.tv_banner_age);
        Button btnBannerBook = findViewById(R.id.btn_banner_book);
        
        if (tvBannerTitle != null && movie.getTitle() != null) {
            String title = movie.getTitle();
            if (movie.getAgeRating() != null) {
                title += " (T" + movie.getAgeRating() + ")";
            }
            tvBannerTitle.setText(title);
        }
        
        if (tvBannerDuration != null && movie.getDurationMinutes() != null) {
            tvBannerDuration.setText(movie.getDurationMinutes() + " phút");
        }
        
        if (tvBannerAge != null && movie.getAgeRating() != null) {
            tvBannerAge.setText("T" + movie.getAgeRating());
        }
        
        // Update click listener
        if (btnBannerBook != null) {
            btnBannerBook.setOnClickListener(v -> openMovieDetailFromAPI(movie));
        }
    }
    
    /**
     * Adapter cho banner ViewPager2
     */
    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
        private List<MovieListResponse.MovieItem> bannerMovies;
        
        BannerAdapter(List<MovieListResponse.MovieItem> movies) {
            this.bannerMovies = movies;
        }
        
        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new BannerViewHolder(imageView);
        }
        
        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            MovieListResponse.MovieItem movie = bannerMovies.get(position);
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(movie.getPosterUrl())
                        .placeholder(R.drawable.home_banner_1)
                        .error(R.drawable.home_banner_1)
                        .centerCrop()
                        .into((ImageView) holder.itemView);
            } else {
                ((ImageView) holder.itemView).setImageResource(R.drawable.home_banner_1);
            }
            
            // Click để mở chi tiết phim
            holder.itemView.setOnClickListener(v -> openMovieDetailFromAPI(movie));
        }
        
        @Override
        public int getItemCount() {
            return bannerMovies != null ? bannerMovies.size() : 0;
        }
        
        class BannerViewHolder extends RecyclerView.ViewHolder {
            BannerViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
    
    /**
     * Setup RecyclerView for movie list
     */
    private void setupRecyclerView() {
        rvMovies = findViewById(R.id.rv_movies);
        if (rvMovies == null) return;
        
        movieAdapter = new MovieAdapter(new ArrayList<>(), movie -> {
            // Navigate to movie detail
            openMovieDetailFromAPI(movie);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMovies.setLayoutManager(layoutManager);
        rvMovies.setAdapter(movieAdapter);
        rvMovies.setNestedScrollingEnabled(false); // Cho phép NestedScrollView xử lý scroll
        rvMovies.setHasFixedSize(false); // Cho phép wrap_content
    }
    
    /**
     * Gọi API để lấy danh sách phim
     */
    private void fetchMovies() {
        MovieApiService movieApiService = ApiClient.getMovieApiService();
        Call<MovieListResponse> call = movieApiService.getAllMovies();
        
        call.enqueue(new Callback<MovieListResponse>() {
            @Override
            public void onResponse(Call<MovieListResponse> call, Response<MovieListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieListResponse movieResponse = response.body();
                    if (movieResponse.getSuccess() != null && movieResponse.getSuccess() 
                            && movieResponse.getData() != null) {
                        movieList = movieResponse.getData();
                        updateMovieListUI();
                    } else {
                        showError("Không thể lấy danh sách phim");
                    }
                } else {
                    showError("Không thể lấy danh sách phim");
                }
            }
            
            @Override
            public void onFailure(Call<MovieListResponse> call, Throwable t) {
                showError("Không thể kết nối đến server");
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Cập nhật UI với danh sách phim từ API
     */
    private void updateMovieListUI() {
        if (movieAdapter != null) {
            // Cập nhật adapter với tất cả phim từ API (không giới hạn 5 phim)
            movieAdapter.updateMovies(movieList);
        }
        
        // Cập nhật banner với phim đầu tiên
        updateBannerFromMovies();
        
        // Cập nhật số lượng phim
        TextView tvMovieCount = findViewById(R.id.tv_movie_count);
        if (tvMovieCount != null && movieList != null) {
            tvMovieCount.setText("Danh sách " + movieList.size() + " phim");
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupNavigation() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    /**
     * Đăng ký click cho 2 tab Đang chiếu / Sắp chiếu để người dùng có cảm giác chuyển màn,
     * hiện tại chỉ đổi style (demo, chưa lọc danh sách phim thực tế).
     */
    private void setupTabs() {
        TextView tabNow = findViewById(R.id.tab_now_showing);
        TextView tabComing = findViewById(R.id.tab_coming_soon);

        if (tabNow == null || tabComing == null) return;

        // Trạng thái mặc định: Đang chiếu được chọn (Light theme)
        tabNow.setBackgroundResource(R.drawable.bg_movie_tab_light_active);
        tabNow.setTextColor(0xFFFFFFFF);
        tabComing.setBackgroundResource(android.R.color.transparent);
        tabComing.setTextColor(getColor(R.color.movie_text_secondary));

        View.OnClickListener listener = v -> {
            if (v == tabNow) {
                // Kích hoạt "Đang chiếu"
                tabNow.setBackgroundResource(R.drawable.bg_movie_tab_light_active);
                tabNow.setTextColor(0xFFFFFFFF);

                tabComing.setBackgroundResource(android.R.color.transparent);
                tabComing.setTextColor(getColor(R.color.movie_text_secondary));
            } else if (v == tabComing) {
                // Kích hoạt "Sắp chiếu"
                tabComing.setBackgroundResource(R.drawable.bg_movie_tab_light_active);
                tabComing.setTextColor(0xFFFFFFFF);

                tabNow.setBackgroundResource(android.R.color.transparent);
                tabNow.setTextColor(getColor(R.color.movie_text_secondary));
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
        etSearch = findViewById(R.id.et_search);
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (movieAdapter != null) {
                    movieAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
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
    
    /**
     * Mở màn chi tiết phim từ dữ liệu API
     */
    private void openMovieDetailFromAPI(MovieListResponse.MovieItem movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.getMovieId());
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_TITLE, movie.getTitle());
        if (movie.getGenreDisplay() != null) {
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_GENRE, movie.getGenreDisplay());
        }
        if (movie.getDurationMinutes() != null) {
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_DURATION, movie.getDurationMinutes() + " phút");
        }
        if (movie.getAgeRating() != null) {
            String ageRating = "T" + movie.getAgeRating();
            intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_AGE_RATING, ageRating);
        }
        // TODO: Load poster từ URL
        intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_POSTER_RES_ID, R.drawable.home_banner_1);
        startActivity(intent);
    }
}


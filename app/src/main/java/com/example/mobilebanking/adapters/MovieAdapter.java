package com.example.mobilebanking.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.MovieDetailActivity;
import com.example.mobilebanking.api.dto.MovieListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying movie list in RecyclerView
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> implements Filterable {
    
    private List<MovieListResponse.MovieItem> movies;
    private List<MovieListResponse.MovieItem> moviesFiltered;
    private OnMovieClickListener listener;
    
    public interface OnMovieClickListener {
        void onMovieClick(MovieListResponse.MovieItem movie);
    }
    
    public MovieAdapter(List<MovieListResponse.MovieItem> movies, OnMovieClickListener listener) {
        this.movies = movies != null ? new ArrayList<>(movies) : new ArrayList<>();
        this.moviesFiltered = new ArrayList<>(this.movies);
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_light, parent, false);
        return new MovieViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieListResponse.MovieItem movie = moviesFiltered.get(position);
        holder.bind(movie, listener);
    }
    
    @Override
    public int getItemCount() {
        return moviesFiltered != null ? moviesFiltered.size() : 0;
    }
    
    /**
     * Update movies list
     */
    public void updateMovies(List<MovieListResponse.MovieItem> newMovies) {
        this.movies = newMovies != null ? new ArrayList<>(newMovies) : new ArrayList<>();
        this.moviesFiltered = new ArrayList<>(this.movies);
        notifyDataSetChanged();
    }
    
    /**
     * Filter movies by search query
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<MovieListResponse.MovieItem> filteredList = new ArrayList<>();
                
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(movies);
                } else {
                    String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                    for (MovieListResponse.MovieItem movie : movies) {
                        if (movie.getTitle() != null && 
                            movie.getTitle().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                            filteredList.add(movie);
                        }
                    }
                }
                
                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }
            
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                moviesFiltered = (List<MovieListResponse.MovieItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    
    /**
     * ViewHolder for movie item
     */
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPoster;
        private TextView tvTitle;
        private TextView tvGenre;
        private TextView tvReleaseDate;
        private TextView tvDuration;
        private TextView tvAgeRating;
        private Button btnBook;
        
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            tvReleaseDate = itemView.findViewById(R.id.tv_release_date);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvAgeRating = itemView.findViewById(R.id.tv_age_rating);
            btnBook = itemView.findViewById(R.id.btn_book);
        }
        
        public void bind(MovieListResponse.MovieItem movie, OnMovieClickListener listener) {
            // Set title
            if (movie.getTitle() != null) {
                tvTitle.setText(movie.getTitle());
            }
            
            // Set genre
            if (movie.getGenreDisplay() != null) {
                tvGenre.setText(movie.getGenreDisplay());
            }
            
            // Set release date
            if (movie.getReleaseDate() != null) {
                tvReleaseDate.setText("Khởi chiếu: " + movie.getReleaseDate());
            }
            
            // Set duration
            if (movie.getDurationMinutes() != null) {
                tvDuration.setText(movie.getDurationMinutes() + " phút");
            }
            
            // Set age rating
            if (movie.getAgeRating() != null) {
                String ageRating = "T" + movie.getAgeRating();
                tvAgeRating.setText(ageRating);
            }
            
            // Load poster image from URL using Glide
            if (movie.getPosterUrl() != null && !movie.getPosterUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(movie.getPosterUrl())
                        .placeholder(R.drawable.home_banner_1) // Placeholder while loading
                        .error(R.drawable.home_banner_1) // Error image if load fails
                        .centerCrop()
                        .into(ivPoster);
            } else {
                // Use default image if no URL
                ivPoster.setImageResource(R.drawable.home_banner_1);
            }
            
            // Set click listeners
            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onMovieClick(movie);
                }
            };
            
            itemView.setOnClickListener(clickListener);
            btnBook.setOnClickListener(clickListener);
        }
    }
}


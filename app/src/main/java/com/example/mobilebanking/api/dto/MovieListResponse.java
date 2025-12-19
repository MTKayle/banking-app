package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response DTO for movie list API
 */
public class MovieListResponse {
    private Boolean success;
    private List<MovieItem> data;
    private String message;

    public MovieListResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<MovieItem> getData() {
        return data;
    }

    public void setData(List<MovieItem> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Movie item in list
     */
    public static class MovieItem {
        private Long movieId;
        private String title;
        private String genre;
        private String genreDisplay;
        private String releaseDate;
        private Integer durationMinutes;
        private Integer ageRating;
        private String posterUrl;
        private Boolean isShowing;

        public MovieItem() {
        }

        public Long getMovieId() {
            return movieId;
        }

        public void setMovieId(Long movieId) {
            this.movieId = movieId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getGenre() {
            return genre;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public String getGenreDisplay() {
            return genreDisplay;
        }

        public void setGenreDisplay(String genreDisplay) {
            this.genreDisplay = genreDisplay;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public Integer getAgeRating() {
            return ageRating;
        }

        public void setAgeRating(Integer ageRating) {
            this.ageRating = ageRating;
        }

        public String getPosterUrl() {
            return posterUrl;
        }

        public void setPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
        }

        public Boolean getIsShowing() {
            return isShowing;
        }

        public void setIsShowing(Boolean isShowing) {
            this.isShowing = isShowing;
        }
    }
}


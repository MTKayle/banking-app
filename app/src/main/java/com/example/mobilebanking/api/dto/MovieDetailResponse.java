package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response DTO for movie detail API
 */
public class MovieDetailResponse {
    private Boolean success;
    private MovieDetailData data;
    private String message;

    public MovieDetailResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public MovieDetailData getData() {
        return data;
    }

    public void setData(MovieDetailData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Movie detail data
     */
    public static class MovieDetailData {
        private Long movieId;
        private String title;
        private String description;
        private Integer durationMinutes;
        private String genre;
        private String genreDisplay;
        private String releaseDate;
        private Integer ageRating;
        private String director;
        private String cast;
        private String country;
        private String language;
        private String languageDisplay;
        private String posterUrl;
        private String trailerUrl;
        private List<String> screeningTypes;
        private Boolean isShowing;

        public MovieDetailData() {
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
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

        public Integer getAgeRating() {
            return ageRating;
        }

        public void setAgeRating(Integer ageRating) {
            this.ageRating = ageRating;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public String getCast() {
            return cast;
        }

        public void setCast(String cast) {
            this.cast = cast;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getLanguageDisplay() {
            return languageDisplay;
        }

        public void setLanguageDisplay(String languageDisplay) {
            this.languageDisplay = languageDisplay;
        }

        public String getPosterUrl() {
            return posterUrl;
        }

        public void setPosterUrl(String posterUrl) {
            this.posterUrl = posterUrl;
        }

        public String getTrailerUrl() {
            return trailerUrl;
        }

        public void setTrailerUrl(String trailerUrl) {
            this.trailerUrl = trailerUrl;
        }

        public List<String> getScreeningTypes() {
            return screeningTypes;
        }

        public void setScreeningTypes(List<String> screeningTypes) {
            this.screeningTypes = screeningTypes;
        }

        public Boolean getIsShowing() {
            return isShowing;
        }

        public void setIsShowing(Boolean isShowing) {
            this.isShowing = isShowing;
        }
    }
}


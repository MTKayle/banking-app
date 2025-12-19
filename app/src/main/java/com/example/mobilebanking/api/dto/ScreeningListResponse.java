package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response DTO for screenings list API
 */
public class ScreeningListResponse {
    private Boolean success;
    private List<ScreeningData> data;
    private String message;

    public ScreeningListResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<ScreeningData> getData() {
        return data;
    }

    public void setData(List<ScreeningData> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Screening data
     */
    public static class ScreeningData {
        private Long cinemaId;
        private String cinemaName;
        private String cinemaAddress;
        private List<ScreeningItem> screenings;

        public ScreeningData() {
        }

        public Long getCinemaId() {
            return cinemaId;
        }

        public void setCinemaId(Long cinemaId) {
            this.cinemaId = cinemaId;
        }

        public String getCinemaName() {
            return cinemaName;
        }

        public void setCinemaName(String cinemaName) {
            this.cinemaName = cinemaName;
        }

        public String getCinemaAddress() {
            return cinemaAddress;
        }

        public void setCinemaAddress(String cinemaAddress) {
            this.cinemaAddress = cinemaAddress;
        }

        public List<ScreeningItem> getScreenings() {
            return screenings;
        }

        public void setScreenings(List<ScreeningItem> screenings) {
            this.screenings = screenings;
        }
    }

    /**
     * Screening item
     */
    public static class ScreeningItem {
        private Long screeningId;
        private Long hallId;
        private String hallName;
        private String startTime;
        private String endTime;
        private String screeningType;
        private String screeningTypeDisplay;
        private Integer availableSeats;
        private Integer totalSeats;
        private Double priceMultiplier;

        public ScreeningItem() {
        }

        public Long getScreeningId() {
            return screeningId;
        }

        public void setScreeningId(Long screeningId) {
            this.screeningId = screeningId;
        }

        public Long getHallId() {
            return hallId;
        }

        public void setHallId(Long hallId) {
            this.hallId = hallId;
        }

        public String getHallName() {
            return hallName;
        }

        public void setHallName(String hallName) {
            this.hallName = hallName;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getScreeningType() {
            return screeningType;
        }

        public void setScreeningType(String screeningType) {
            this.screeningType = screeningType;
        }

        public String getScreeningTypeDisplay() {
            return screeningTypeDisplay;
        }

        public void setScreeningTypeDisplay(String screeningTypeDisplay) {
            this.screeningTypeDisplay = screeningTypeDisplay;
        }

        public Integer getAvailableSeats() {
            return availableSeats;
        }

        public void setAvailableSeats(Integer availableSeats) {
            this.availableSeats = availableSeats;
        }

        public Integer getTotalSeats() {
            return totalSeats;
        }

        public void setTotalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
        }

        public Double getPriceMultiplier() {
            return priceMultiplier;
        }

        public void setPriceMultiplier(Double priceMultiplier) {
            this.priceMultiplier = priceMultiplier;
        }
    }
}


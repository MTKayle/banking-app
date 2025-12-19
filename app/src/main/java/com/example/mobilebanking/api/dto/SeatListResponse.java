package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response DTO for seats list API
 */
public class SeatListResponse {
    private Boolean success;
    private SeatListData data;
    private String message;

    public SeatListResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public SeatListData getData() {
        return data;
    }

    public void setData(SeatListData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Seat list data
     */
    public static class SeatListData {
        private Long screeningId;
        private String movieTitle;
        private String cinemaName;
        private String hallName;
        private String screeningDate;
        private String startTime;
        private String screeningType;
        private Double priceMultiplier;
        private List<SeatItem> seats;

        public SeatListData() {
        }

        public Long getScreeningId() {
            return screeningId;
        }

        public void setScreeningId(Long screeningId) {
            this.screeningId = screeningId;
        }

        public String getMovieTitle() {
            return movieTitle;
        }

        public void setMovieTitle(String movieTitle) {
            this.movieTitle = movieTitle;
        }

        public String getCinemaName() {
            return cinemaName;
        }

        public void setCinemaName(String cinemaName) {
            this.cinemaName = cinemaName;
        }

        public String getHallName() {
            return hallName;
        }

        public void setHallName(String hallName) {
            this.hallName = hallName;
        }

        public String getScreeningDate() {
            return screeningDate;
        }

        public void setScreeningDate(String screeningDate) {
            this.screeningDate = screeningDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getScreeningType() {
            return screeningType;
        }

        public void setScreeningType(String screeningType) {
            this.screeningType = screeningType;
        }

        public Double getPriceMultiplier() {
            return priceMultiplier;
        }

        public void setPriceMultiplier(Double priceMultiplier) {
            this.priceMultiplier = priceMultiplier;
        }

        public List<SeatItem> getSeats() {
            return seats;
        }

        public void setSeats(List<SeatItem> seats) {
            this.seats = seats;
        }
    }

    /**
     * Seat item
     */
    public static class SeatItem {
        private Long seatId;
        private String rowLabel;
        private Integer seatNumber;
        private String seatLabel;
        private String seatType; // STANDARD, VIP, COUPLE
        private String seatTypeDisplay;
        private Double basePrice;
        private Double finalPrice;
        private String status; // AVAILABLE, BOOKED, RESERVED, MAINTENANCE

        public SeatItem() {
        }

        public Long getSeatId() {
            return seatId;
        }

        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }

        public String getRowLabel() {
            return rowLabel;
        }

        public void setRowLabel(String rowLabel) {
            this.rowLabel = rowLabel;
        }

        public Integer getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(Integer seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getSeatLabel() {
            return seatLabel;
        }

        public void setSeatLabel(String seatLabel) {
            this.seatLabel = seatLabel;
        }

        public String getSeatType() {
            return seatType;
        }

        public void setSeatType(String seatType) {
            this.seatType = seatType;
        }

        public String getSeatTypeDisplay() {
            return seatTypeDisplay;
        }

        public void setSeatTypeDisplay(String seatTypeDisplay) {
            this.seatTypeDisplay = seatTypeDisplay;
        }

        public Double getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(Double basePrice) {
            this.basePrice = basePrice;
        }

        public Double getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(Double finalPrice) {
            this.finalPrice = finalPrice;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}


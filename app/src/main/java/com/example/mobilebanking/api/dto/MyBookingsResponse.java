package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class MyBookingsResponse {
    private Boolean success;
    private String message;
    private Integer total;
    private List<BookingItem> data;

    // Getters and Setters
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public List<BookingItem> getData() { return data; }
    public void setData(List<BookingItem> data) { this.data = data; }

    public static class BookingItem implements java.io.Serializable {
        private Long bookingId;
        private String bookingCode;

        // Movie Information
        private Long movieId;
        private String movieTitle;
        private String posterUrl;
        private Integer durationMinutes;

        // Screening Information
        private Long screeningId;
        private String screeningDate; // YYYY-MM-DD
        private String startTime;     // HH:mm:ss
        private String endTime;       // HH:mm:ss
        private String screeningType;
        private String screeningTypeDisplay;

        // Cinema Information
        private String cinemaName;
        private String cinemaAddress;
        private String hallName;

        // Customer Information
        private String customerName;
        private String customerPhone;
        private String customerEmail;

        // Seat Information
        private List<SeatInfo> seats;
        private Integer totalSeats;

        // Payment Information
        private BigDecimal totalAmount;
        private String status; // CONFIRMED, CANCELLED
        private String bookingTime; // LocalDateTime string

        // Getters and Setters
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public String getBookingCode() { return bookingCode; }
        public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
        public Long getMovieId() { return movieId; }
        public void setMovieId(Long movieId) { this.movieId = movieId; }
        public String getMovieTitle() { return movieTitle; }
        public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
        public String getPosterUrl() { return posterUrl; }
        public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public Long getScreeningId() { return screeningId; }
        public void setScreeningId(Long screeningId) { this.screeningId = screeningId; }
        public String getScreeningDate() { return screeningDate; }
        public void setScreeningDate(String screeningDate) { this.screeningDate = screeningDate; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public String getScreeningType() { return screeningType; }
        public void setScreeningType(String screeningType) { this.screeningType = screeningType; }
        public String getScreeningTypeDisplay() { return screeningTypeDisplay; }
        public void setScreeningTypeDisplay(String screeningTypeDisplay) { this.screeningTypeDisplay = screeningTypeDisplay; }
        public String getCinemaName() { return cinemaName; }
        public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
        public String getCinemaAddress() { return cinemaAddress; }
        public void setCinemaAddress(String cinemaAddress) { this.cinemaAddress = cinemaAddress; }
        public String getHallName() { return hallName; }
        public void setHallName(String hallName) { this.hallName = hallName; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public List<SeatInfo> getSeats() { return seats; }
        public void setSeats(List<SeatInfo> seats) { this.seats = seats; }
        public Integer getTotalSeats() { return totalSeats; }
        public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBookingTime() { return bookingTime; }
        public void setBookingTime(String bookingTime) { this.bookingTime = bookingTime; }
    }

    public static class SeatInfo implements java.io.Serializable {
        private Long seatId;
        private String seatLabel;
        private String seatType;
        private String seatTypeDisplay;
        private BigDecimal price;

        // Getters and Setters
        public Long getSeatId() { return seatId; }
        public void setSeatId(Long seatId) { this.seatId = seatId; }
        public String getSeatLabel() { return seatLabel; }
        public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }
        public String getSeatType() { return seatType; }
        public void setSeatType(String seatType) { this.seatType = seatType; }
        public String getSeatTypeDisplay() { return seatTypeDisplay; }
        public void setSeatTypeDisplay(String seatTypeDisplay) { this.seatTypeDisplay = seatTypeDisplay; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}


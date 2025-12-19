package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response cho API đặt chỗ (giữ ghế)
 * POST /api/bookings/reserve
 */
public class BookingReserveResponse {
    private Boolean success;
    private String message;
    private BookingData data;
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public BookingData getData() {
        return data;
    }
    
    public void setData(BookingData data) {
        this.data = data;
    }
    
    public static class BookingData {
        private Long bookingId;
        private String bookingCode;
        private Long screeningId;
        private String movieTitle;
        private String cinemaName;
        private String hallName;
        private String screeningDate;
        private String startTime;
        private List<SeatBookingInfo> seats;
        private Integer totalSeats;
        private Integer totalAmount;
        private String status;
        private String bookingTime;
        private String expiryTime;
        
        public Long getBookingId() {
            return bookingId;
        }
        
        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }
        
        public String getBookingCode() {
            return bookingCode;
        }
        
        public void setBookingCode(String bookingCode) {
            this.bookingCode = bookingCode;
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
        
        public List<SeatBookingInfo> getSeats() {
            return seats;
        }
        
        public void setSeats(List<SeatBookingInfo> seats) {
            this.seats = seats;
        }
        
        public Integer getTotalSeats() {
            return totalSeats;
        }
        
        public void setTotalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
        }
        
        public Integer getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(Integer totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getBookingTime() {
            return bookingTime;
        }
        
        public void setBookingTime(String bookingTime) {
            this.bookingTime = bookingTime;
        }
        
        public String getExpiryTime() {
            return expiryTime;
        }
        
        public void setExpiryTime(String expiryTime) {
            this.expiryTime = expiryTime;
        }
    }
    
    public static class SeatBookingInfo {
        private String seatLabel;
        private String seatType;
        private Integer price;
        
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
        
        public Integer getPrice() {
            return price;
        }
        
        public void setPrice(Integer price) {
            this.price = price;
        }
    }
}


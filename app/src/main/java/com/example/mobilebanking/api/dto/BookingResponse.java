package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Response DTO for booking API
 * POST /api/bookings
 */
public class BookingResponse {
    private Boolean success;
    private String message;
    private BookingData data;

    public BookingResponse() {
    }

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

    /**
     * Booking data
     */
    public static class BookingData {
        private Long bookingId;
        private String bookingCode;
        private String status;
        private String movieTitle;
        private String cinemaName;
        private String cinemaAddress;
        private String hallName;
        private String screeningDate;
        private String startTime;
        private String endTime;
        private List<String> seatLabels;
        private Integer seatCount;
        private Double totalAmount;
        private String customerName;
        private String customerPhone;
        private String customerEmail;
        private String createdAt;
        private String expiresAt;
        private String qrCode;

        public BookingData() {
        }

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public String getCinemaAddress() {
            return cinemaAddress;
        }

        public void setCinemaAddress(String cinemaAddress) {
            this.cinemaAddress = cinemaAddress;
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

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public List<String> getSeatLabels() {
            return seatLabels;
        }

        public void setSeatLabels(List<String> seatLabels) {
            this.seatLabels = seatLabels;
        }

        public Integer getSeatCount() {
            return seatCount;
        }

        public void setSeatCount(Integer seatCount) {
            this.seatCount = seatCount;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public void setCustomerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }
    }
}


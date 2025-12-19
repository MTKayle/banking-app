package com.example.mobilebanking.api.dto;

/**
 * Response cho API thanh toán đặt vé
 * POST /api/bookings/{bookingId}/payment
 */
public class BookingPaymentResponse {
    private Boolean success;
    private String message;
    private PaymentData data;
    
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
    
    public PaymentData getData() {
        return data;
    }
    
    public void setData(PaymentData data) {
        this.data = data;
    }
    
    public static class PaymentData {
        private Long bookingId;
        private String bookingCode;
        private Long paymentId;
        private Integer amount;
        private String paymentMethod;
        private String paymentStatus;
        private String paymentTime;
        private String transactionId;
        private String bookingStatus;
        
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
        
        public Long getPaymentId() {
            return paymentId;
        }
        
        public void setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
        }
        
        public Integer getAmount() {
            return amount;
        }
        
        public void setAmount(Integer amount) {
            this.amount = amount;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
        
        public String getPaymentStatus() {
            return paymentStatus;
        }
        
        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
        
        public String getPaymentTime() {
            return paymentTime;
        }
        
        public void setPaymentTime(String paymentTime) {
            this.paymentTime = paymentTime;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
        
        public String getBookingStatus() {
            return bookingStatus;
        }
        
        public void setBookingStatus(String bookingStatus) {
            this.bookingStatus = bookingStatus;
        }
    }
}


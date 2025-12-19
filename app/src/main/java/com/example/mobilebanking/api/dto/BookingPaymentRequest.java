package com.example.mobilebanking.api.dto;

/**
 * Request body cho API thanh toán đặt vé
 * POST /api/bookings/{bookingId}/payment
 */
public class BookingPaymentRequest {
    private String paymentMethod;
    private String accountNumber;
    
    public BookingPaymentRequest() {}
    
    public BookingPaymentRequest(String paymentMethod, String accountNumber) {
        this.paymentMethod = paymentMethod;
        this.accountNumber = accountNumber;
    }
    
    // Getters and Setters
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}


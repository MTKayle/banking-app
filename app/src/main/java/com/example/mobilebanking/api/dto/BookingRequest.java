package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Request DTO for booking API
 * POST /api/bookings
 */
public class BookingRequest {
    private Long screeningId;
    private List<Long> seatIds;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    public BookingRequest() {
    }

    public BookingRequest(Long screeningId, List<Long> seatIds, String customerName, 
                          String customerPhone, String customerEmail) {
        this.screeningId = screeningId;
        this.seatIds = seatIds;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
    }

    public Long getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(Long screeningId) {
        this.screeningId = screeningId;
    }

    public List<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<Long> seatIds) {
        this.seatIds = seatIds;
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
}


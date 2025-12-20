package com.example.mobilebanking.api.dto;

import java.util.List;

public class BankListResponse {
    private boolean success;
    private String message;
    private List<BankResponse> data;
    private int total;

    public BankListResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BankResponse> getData() {
        return data;
    }

    public void setData(List<BankResponse> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}

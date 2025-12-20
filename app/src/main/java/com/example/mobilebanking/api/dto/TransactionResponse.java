package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * Transaction Response wrapper
 * Maps với response từ /api/transactions/my-transactions
 */
public class TransactionResponse {
    private boolean success;
    private String message;
    private int total;
    private List<TransactionDTO> data;

    public TransactionResponse() {
    }

    public TransactionResponse(boolean success, String message, int total, List<TransactionDTO> data) {
        this.success = success;
        this.message = message;
        this.total = total;
        this.data = data;
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

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<TransactionDTO> getData() {
        return data;
    }

    public void setData(List<TransactionDTO> data) {
        this.data = data;
    }
}


package com.example.mobilebanking.api.dto;

import java.util.List;

/**
 * DTO for Saving Term List API Response
 */
public class SavingTermListResponse {
    private Integer total;
    private List<SavingTermResponse> data;
    private Boolean success;
    private String message;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<SavingTermResponse> getData() {
        return data;
    }

    public void setData(List<SavingTermResponse> data) {
        this.data = data;
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
}

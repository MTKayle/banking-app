package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Saving Terms Response Wrapper
 * Wraps the response from GET /api/saving/terms
 */
public class SavingTermsResponse {
    @SerializedName("total")
    private Integer total;
    
    @SerializedName("data")
    private List<SavingTermDTO> data;
    
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    public SavingTermsResponse() {
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public List<SavingTermDTO> getData() {
        return data;
    }
    
    public void setData(List<SavingTermDTO> data) {
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

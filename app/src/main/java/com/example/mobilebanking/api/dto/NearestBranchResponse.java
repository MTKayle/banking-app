package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response cho API tìm chi nhánh gần nhất
 */
public class NearestBranchResponse {
    @SerializedName("total")
    private Integer total;
    
    @SerializedName("data")
    private List<BranchDTO> data;
    
    @SerializedName("success")
    private Boolean success;
    
    @SerializedName("message")
    private String message;
    
    // Constructors
    public NearestBranchResponse() {}
    
    // Getters and Setters
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
    
    public List<BranchDTO> getData() {
        return data;
    }
    
    public void setData(List<BranchDTO> data) {
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

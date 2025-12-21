package com.example.mobilebanking.api.dto;

import com.google.gson.annotations.SerializedName;

public class VNPayDepositRequest {
    
    @SerializedName("amount")
    private Long amount;

    public VNPayDepositRequest(Long amount) {
        this.amount = amount;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}

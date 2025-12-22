package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.BranchListResponse;
import com.example.mobilebanking.api.dto.NearestBranchRequest;
import com.example.mobilebanking.api.dto.NearestBranchResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Branch API Service - API endpoints cho chi nhánh ngân hàng
 */
public interface BranchApiService {
    
    /**
     * Lấy danh sách tất cả chi nhánh
     * Public endpoint - không cần token
     */
    @GET("bank-branches")
    Call<BranchListResponse> getAllBranches();
    
    /**
     * Tìm chi nhánh gần nhất dựa trên tọa độ hiện tại
     * Public endpoint - không cần token
     */
    @POST("bank-branches/nearest")
    Call<NearestBranchResponse> getNearestBranches(@Body NearestBranchRequest request);
}

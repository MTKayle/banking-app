package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.BillTypesResponse;
import com.example.mobilebanking.api.dto.BillSearchResponse;
import com.example.mobilebanking.api.dto.BillPaymentRequest;
import com.example.mobilebanking.api.dto.BillPaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Utility Bill API Service
 * Handles utility bill payment operations
 */
public interface UtilityBillApiService {
    
    /**
     * Lấy danh sách loại hóa đơn
     * GET /api/utility-bills/bill-types
     */
    @GET("utility-bills/bill-types")
    Call<BillTypesResponse> getBillTypes();
    
    /**
     * Tìm kiếm hóa đơn theo mã và loại
     * GET /api/utility-bills/search?billCode=EVN202411001&billType=ELECTRICITY
     */
    @GET("utility-bills/search")
    Call<BillSearchResponse> searchBill(
        @Query("billCode") String billCode,
        @Query("billType") String billType
    );
    
    /**
     * Thanh toán hóa đơn
     * POST /api/utility-bills/pay
     * Header cần có: Authorization: Bearer {token}
     */
    @POST("utility-bills/pay")
    Call<BillPaymentResponse> payBill(@Body BillPaymentRequest request);
}

package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.ApiResponse;
import com.example.mobilebanking.api.dto.CollateralTypeResponse;
import com.example.mobilebanking.api.dto.InterestRateResponse;
import com.example.mobilebanking.api.dto.MortgageAccountResponse;
import com.example.mobilebanking.api.dto.MortgageApproveRequest;
import com.example.mobilebanking.api.dto.MortgageRejectRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Mortgage API Service for Officer
 * Provides endpoints for mortgage management operations
 */
public interface MortgageApiService {

    /**
     * Get mortgages by status - Officer only
     * Endpoint: GET /mortgage/status/{status}
     * @param status PENDING_APPRAISAL, APPROVED, ACTIVE, COMPLETED, REJECTED
     */
    @GET("mortgage/status/{status}")
    Call<List<MortgageAccountResponse>> getMortgagesByStatus(@Path("status") String status);

    /**
     * Get pending mortgages - Officer only
     * Endpoint: GET /mortgage/pending
     */
    @GET("mortgage/pending")
    Call<List<MortgageAccountResponse>> getPendingMortgages();

    /**
     * Search mortgages by status and phone - Officer only
     * Endpoint: GET /mortgage/status/{status}/search?phone={phone}
     */
    @GET("mortgage/status/{status}/search")
    Call<List<MortgageAccountResponse>> searchMortgages(
            @Path("status") String status,
            @Query("phone") String phone
    );

    /**
     * Get mortgage detail
     * Endpoint: GET /mortgage/{mortgageId}
     */
    @GET("mortgage/{mortgageId}")
    Call<MortgageAccountResponse> getMortgageDetail(@Path("mortgageId") Long mortgageId);

    /**
     * Get mortgages by user ID
     * Endpoint: GET /mortgage/user/{userId}
     */
    @GET("mortgage/user/{userId}")
    Call<List<MortgageAccountResponse>> getMortgagesByUser(@Path("userId") Long userId);

    /**
     * Create new mortgage - Officer only
     * Endpoint: POST /mortgage/create
     * Content-Type: multipart/form-data
     */
    @Multipart
    @POST("mortgage/create")
    Call<MortgageAccountResponse> createMortgage(
            @Part("request") RequestBody request,
            @Part MultipartBody.Part cccdFront,
            @Part MultipartBody.Part cccdBack,
            @Part List<MultipartBody.Part> collateralDocuments
    );

    /**
     * Create new mortgage without files - Officer only
     * Endpoint: POST /mortgage/create
     */
    @Multipart
    @POST("mortgage/create")
    Call<MortgageAccountResponse> createMortgageSimple(
            @Part("request") RequestBody request
    );

    /**
     * Approve mortgage - Officer only
     * Endpoint: POST /mortgage/approve
     */
    @POST("mortgage/approve")
    Call<MortgageAccountResponse> approveMortgage(@Body MortgageApproveRequest request);

    /**
     * Reject mortgage - Officer only
     * Endpoint: POST /mortgage/reject
     */
    @POST("mortgage/reject")
    Call<MortgageAccountResponse> rejectMortgage(@Body MortgageRejectRequest request);

    /**
     * Get all collateral types
     * Endpoint: GET /mortgage/collateral-types
     * Response: { success: true, data: [{ value, displayName }] }
     */
    @GET("mortgage/collateral-types")
    Call<Map<String, Object>> getCollateralTypes();

    /**
     * Get all mortgage interest rates
     * Endpoint: GET /mortgage/interest-rates
     * Response: { success: true, data: [{ termMonths, interestRate, ... }] }
     */
    @GET("mortgage/interest-rates")
    Call<Map<String, Object>> getInterestRates();

    /**
     * Make mortgage payment (full settlement)
     * Endpoint: POST /mortgage/payment
     * @param request MortgagePaymentRequest with mortgageId and amount
     */
    @POST("mortgage/payment")
    Call<Map<String, Object>> makeMortgagePayment(@Body Map<String, Object> request);

    /**
     * Make current period payment
     * Endpoint: POST /mortgage/payment/current
     * @param request MortgagePaymentRequest with mortgageId and amount
     */
    @POST("mortgage/payment/current")
    Call<Map<String, Object>> makeCurrentPeriodPayment(@Body Map<String, Object> request);
}

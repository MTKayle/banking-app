package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.GoogleDirectionsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Google Directions API Service
 * API để tìm đường đi tối ưu giữa các điểm
 * 
 * Lưu ý: Google Directions API miễn phí có giới hạn:
 * - 2,500 requests/ngày miễn phí
 * - Sau đó $5/1000 requests
 */
public interface GoogleDirectionsApiService {
    
    /**
     * Lấy đường đi từ điểm A đến điểm B
     * 
     * @param origin Điểm xuất phát: "lat,lng"
     * @param destination Điểm đến: "lat,lng"
     * @param mode Phương tiện: driving, walking, bicycling, transit
     * @param language Ngôn ngữ: vi (tiếng Việt)
     * @param alternatives Có trả về các tuyến đường thay thế không
     * @return GoogleDirectionsResponse chứa thông tin đường đi
     */
    @GET("maps/api/directions/json")
    Call<GoogleDirectionsResponse> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("language") String language,
            @Query("alternatives") boolean alternatives,
            @Query("key") String apiKey
    );
}

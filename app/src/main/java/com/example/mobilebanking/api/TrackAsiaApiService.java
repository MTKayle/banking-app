package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.DirectionsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * OSRM (Open Source Routing Machine) API Service
 * API miễn phí để tìm đường đi - thay thế TrackAsia
 * 
 * Public server: https://router.project-osrm.org/
 * Hoàn toàn miễn phí, không cần API key
 */
public interface TrackAsiaApiService {
    
    /**
     * Lấy đường đi từ điểm A đến điểm B
     * 
     * @param profile Phương tiện: car (xe hơi), foot (đi bộ), bike (xe đạp)
     * @param coordinates Tọa độ dạng: "lng1,lat1;lng2,lat2"
     * @param alternatives Có trả về các tuyến đường thay thế không
     * @param steps Có trả về hướng dẫn từng bước không
     * @param geometries Định dạng geometry: polyline hoặc geojson
     * @param overview Mức độ chi tiết: full, simplified, false
     * @return DirectionsResponse chứa thông tin đường đi
     */
    @GET("route/v1/{profile}/{coordinates}")
    Call<DirectionsResponse> getDirections(
            @Path("profile") String profile,
            @Path("coordinates") String coordinates,
            @Query("alternatives") boolean alternatives,
            @Query("steps") boolean steps,
            @Query("geometries") String geometries,
            @Query("overview") String overview
    );
}

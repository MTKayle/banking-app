package com.example.mobilebanking.api;

import com.example.mobilebanking.api.dto.BookingPaymentRequest;
import com.example.mobilebanking.api.dto.BookingPaymentResponse;
import com.example.mobilebanking.api.dto.BookingRequest;
import com.example.mobilebanking.api.dto.BookingResponse;
import com.example.mobilebanking.api.dto.BookingReserveRequest;
import com.example.mobilebanking.api.dto.BookingReserveResponse;
import com.example.mobilebanking.api.dto.MovieListResponse;
import com.example.mobilebanking.api.dto.MovieDetailResponse;
import com.example.mobilebanking.api.dto.MyBookingsResponse;
import com.example.mobilebanking.api.dto.ScreeningListResponse;
import com.example.mobilebanking.api.dto.SeatListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Movie API Service
 */
public interface MovieApiService {
    
    /**
     * Lấy danh sách tất cả các phim
     */
    @GET("movies")
    Call<MovieListResponse> getAllMovies();
    
    /**
     * Lấy danh sách phim đang chiếu
     */
    @GET("movies/now-showing")
    Call<MovieListResponse> getNowShowingMovies();
    
    /**
     * Lấy chi tiết phim theo ID
     */
    @GET("movies/{movieId}")
    Call<MovieDetailResponse> getMovieDetail(@Path("movieId") Long movieId);
    
    /**
     * Lấy danh sách suất chiếu của phim theo ngày
     * @param movieId ID phim
     * @param date Ngày chiếu (format: YYYY-MM-DD)
     */
    @GET("movies/{movieId}/screenings")
    Call<ScreeningListResponse> getScreeningsByDate(
            @Path("movieId") Long movieId,
            @Query("date") String date
    );
    
    /**
     * Lấy danh sách ghế của suất chiếu
     * @param screeningId ID suất chiếu
     */
    @GET("movies/screenings/{screeningId}")
    Call<SeatListResponse> getSeatsByScreening(@Path("screeningId") Long screeningId);
    
    /**
     * Đặt vé (đặt và thanh toán luôn)
     */
    @POST("bookings")
    Call<BookingResponse> createBooking(@Body BookingRequest request);
    
    /**
     * Đặt chỗ (giữ ghế trong 10 phút)
     */
    @POST("bookings/reserve")
    Call<BookingReserveResponse> reserveBooking(
            @Header("Authorization") String authToken,
            @Body BookingReserveRequest request
    );
    
    /**
     * Thanh toán đặt vé
     */
    @POST("bookings/{bookingId}/payment")
    Call<BookingPaymentResponse> payBooking(
            @Header("Authorization") String authToken,
            @Path("bookingId") Long bookingId,
            @Body BookingPaymentRequest request
    );
    
    /**
     * Lấy danh sách vé đã đặt của user
     */
    @GET("bookings/my-bookings")
    Call<MyBookingsResponse> getMyBookings(@Header("Authorization") String authToken);
}


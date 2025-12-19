package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.TicketPagerAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.dto.MyBookingsResponse;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyTicketsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DataManager dataManager;
    private MovieApiService movieApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vé của tôi");
        }

        dataManager = DataManager.getInstance(this);
        movieApiService = ApiClient.getMovieApiService();

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        fetchMyBookings();
    }

    private void fetchMyBookings() {
        String accessToken = dataManager.getAccessToken();
        if (accessToken == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem vé của bạn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<MyBookingsResponse> call = movieApiService.getMyBookings("Bearer " + accessToken);
        call.enqueue(new Callback<MyBookingsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyBookingsResponse> call, @NonNull Response<MyBookingsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                    List<MyBookingsResponse.BookingItem> allBookings = response.body().getData();
                    if (allBookings != null) {
                        setupViewPager(allBookings);
                    } else {
                        Toast.makeText(MyTicketsActivity.this, "Không có vé nào được tìm thấy.", Toast.LENGTH_SHORT).show();
                        setupViewPager(new ArrayList<>());
                    }
                } else {
                    String errorMessage = "Lỗi khi tải vé";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MyTicketsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    setupViewPager(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyBookingsResponse> call, @NonNull Throwable t) {
                Toast.makeText(MyTicketsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                setupViewPager(new ArrayList<>());
            }
        });
    }

    private void setupViewPager(List<MyBookingsResponse.BookingItem> allBookings) {
        List<MyBookingsResponse.BookingItem> currentTickets = new ArrayList<>();
        List<MyBookingsResponse.BookingItem> pastTickets = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();
        today = stripTime(today);

        for (MyBookingsResponse.BookingItem booking : allBookings) {
            try {
                Date screeningDate = dateFormat.parse(booking.getScreeningDate());
                screeningDate = stripTime(screeningDate);

                if (screeningDate != null && (screeningDate.after(today) || screeningDate.equals(today))) {
                    currentTickets.add(booking);
                } else {
                    pastTickets.add(booking);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Sort current tickets by screening date and time (ascending)
        Collections.sort(currentTickets, (b1, b2) -> {
            int dateCompare = b1.getScreeningDate().compareTo(b2.getScreeningDate());
            if (dateCompare != 0) return dateCompare;
            return b1.getStartTime().compareTo(b2.getStartTime());
        });

        // Sort past tickets by screening date and time (descending)
        Collections.sort(pastTickets, (b1, b2) -> {
            int dateCompare = b2.getScreeningDate().compareTo(b1.getScreeningDate());
            if (dateCompare != 0) return dateCompare;
            return b2.getStartTime().compareTo(b1.getStartTime());
        });

        TicketPagerAdapter pagerAdapter = new TicketPagerAdapter(this, currentTickets, pastTickets);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Hiện tại");
                    } else {
                        tab.setText("Đã đi");
                    }
                }).attach();
    }

    private Date stripTime(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, com.example.mobilebanking.ui_home.UiHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}


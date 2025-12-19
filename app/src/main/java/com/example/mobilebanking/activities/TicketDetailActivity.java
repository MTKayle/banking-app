package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.MyBookingsResponse;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ITEM = "extra_booking_item";

    private ImageView ivMoviePosterDetail;
    private TextView tvMovieTitleDetail, tvBookingCodeDetail, tvCinemaNameDetail, tvCinemaAddressDetail,
            tvHallNameDetail, tvScreeningDateDetail, tvStartTimeDetail, tvScreeningTypeDetail,
            tvTotalSeatsDetail, tvSeatLabelsDetail, tvCustomerNameDetail, tvCustomerPhoneDetail,
            tvCustomerEmailDetail, tvTotalAmountDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết vé");
        }

        initViews();
        bindData();
    }

    private void initViews() {
        ivMoviePosterDetail = findViewById(R.id.iv_movie_poster_detail);
        tvMovieTitleDetail = findViewById(R.id.tv_movie_title_detail);
        tvBookingCodeDetail = findViewById(R.id.tv_booking_code_detail);
        tvCinemaNameDetail = findViewById(R.id.tv_cinema_name_detail);
        tvCinemaAddressDetail = findViewById(R.id.tv_cinema_address_detail);
        tvHallNameDetail = findViewById(R.id.tv_hall_name_detail);
        tvScreeningDateDetail = findViewById(R.id.tv_screening_date_detail);
        tvStartTimeDetail = findViewById(R.id.tv_start_time_detail);
        tvScreeningTypeDetail = findViewById(R.id.tv_screening_type_detail);
        tvTotalSeatsDetail = findViewById(R.id.tv_total_seats_detail);
        tvSeatLabelsDetail = findViewById(R.id.tv_seat_labels_detail);
        tvCustomerNameDetail = findViewById(R.id.tv_customer_name_detail);
        tvCustomerPhoneDetail = findViewById(R.id.tv_customer_phone_detail);
        tvCustomerEmailDetail = findViewById(R.id.tv_customer_email_detail);
        tvTotalAmountDetail = findViewById(R.id.tv_total_amount_detail);
    }

    private void bindData() {
        Serializable serializable = getIntent().getSerializableExtra(EXTRA_BOOKING_ITEM);
        if (!(serializable instanceof MyBookingsResponse.BookingItem)) {
            finish();
            return;
        }
        
        MyBookingsResponse.BookingItem bookingItem = (MyBookingsResponse.BookingItem) serializable;

        Glide.with(this)
                .load(bookingItem.getPosterUrl())
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(ivMoviePosterDetail);

        tvMovieTitleDetail.setText(bookingItem.getMovieTitle());
        tvBookingCodeDetail.setText(String.format("Mã đặt vé: %s", bookingItem.getBookingCode()));
        tvCinemaNameDetail.setText(bookingItem.getCinemaName());
        tvCinemaAddressDetail.setText(bookingItem.getCinemaAddress());
        tvHallNameDetail.setText(bookingItem.getHallName());
        tvScreeningDateDetail.setText(bookingItem.getScreeningDate());
        tvStartTimeDetail.setText(bookingItem.getStartTime() != null && bookingItem.getStartTime().length() >= 5 
                ? bookingItem.getStartTime().substring(0, 5) 
                : bookingItem.getStartTime());
        tvScreeningTypeDetail.setText(bookingItem.getScreeningTypeDisplay());

        tvTotalSeatsDetail.setText(String.format(Locale.getDefault(), "%d ghế", 
                bookingItem.getTotalSeats() != null ? bookingItem.getTotalSeats() : 0));
        
        if (bookingItem.getSeats() != null && !bookingItem.getSeats().isEmpty()) {
            String seatLabels = bookingItem.getSeats().stream()
                    .map(MyBookingsResponse.SeatInfo::getSeatLabel)
                    .collect(Collectors.joining(", "));
            tvSeatLabelsDetail.setText(seatLabels);
        }

        tvCustomerNameDetail.setText(bookingItem.getCustomerName());
        tvCustomerPhoneDetail.setText(bookingItem.getCustomerPhone());
        tvCustomerEmailDetail.setText(bookingItem.getCustomerEmail());

        if (bookingItem.getTotalAmount() != null) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            tvTotalAmountDetail.setText(formatter.format(bookingItem.getTotalAmount()) + " VND");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


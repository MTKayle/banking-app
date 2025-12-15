package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;

/**
 * Hotel Booking Activity - Hotel reservation interface
 */
public class HotelBookingActivity extends AppCompatActivity {
    private EditText etLocation, etCheckIn, etCheckOut, etGuests;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_booking);

        setupToolbar();
        initializeViews();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hotel Booking");
        }
    }

    private void initializeViews() {
        etLocation = findViewById(R.id.et_location);
        etCheckIn = findViewById(R.id.et_check_in);
        etCheckOut = findViewById(R.id.et_check_out);
        etGuests = findViewById(R.id.et_guests);
        btnSearch = findViewById(R.id.btn_search);
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Hotel search feature", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

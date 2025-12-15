package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;

/**
 * Ticket Booking Activity - Flight and movie tickets
 */
public class TicketBookingActivity extends AppCompatActivity {
    private RadioGroup rgTicketType;
    private Button btnBookFlight, btnBookMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_booking);

        setupToolbar();
        initializeViews();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ticket Booking");
        }
    }

    private void initializeViews() {
        rgTicketType = findViewById(R.id.rg_ticket_type);
        btnBookFlight = findViewById(R.id.btn_book_flight);
        btnBookMovie = findViewById(R.id.btn_book_movie);
    }

    private void setupListeners() {
        btnBookFlight.setOnClickListener(v -> {
            Toast.makeText(this, "Flight booking feature", Toast.LENGTH_SHORT).show();
        });

        btnBookMovie.setOnClickListener(v -> {
            Toast.makeText(this, "Movie booking feature", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


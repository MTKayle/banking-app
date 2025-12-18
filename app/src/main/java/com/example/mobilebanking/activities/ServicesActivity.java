package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.mobilebanking.R;

/**
 * Services Activity - All banking services
 */
public class ServicesActivity extends AppCompatActivity {
    private CardView cvTransfer, cvBillPay, cvTopUp, cvTickets, cvMovieTickets, cvHotels, cvBranches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        setupToolbar();
        initializeViews();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Services");
        }
    }

    private void initializeViews() {
        cvTransfer = findViewById(R.id.cv_transfer);
        cvBillPay = findViewById(R.id.cv_bill_pay);
        cvTopUp = findViewById(R.id.cv_top_up);
        cvTickets = findViewById(R.id.cv_tickets);
        cvMovieTickets = findViewById(R.id.cv_movie_tickets);
        cvHotels = findViewById(R.id.cv_hotels);
        cvBranches = findViewById(R.id.cv_branches);
    }

    private void setupListeners() {
        cvTransfer.setOnClickListener(v -> 
            startActivity(new Intent(this, TransferActivity.class)));
        
        cvBillPay.setOnClickListener(v -> 
            startActivity(new Intent(this, BillPaymentActivity.class)));
        
        cvTopUp.setOnClickListener(v -> 
            startActivity(new Intent(this, MobileTopUpActivity.class)));
        
        cvTickets.setOnClickListener(v -> 
            startActivity(new Intent(this, TicketBookingActivity.class)));

        // New: cinema ticket booking flow (CGV / Galaxy style) inside super app
        if (cvMovieTickets != null) {
            cvMovieTickets.setOnClickListener(v ->
                    startActivity(new Intent(this, MovieListActivity.class)));
        }
        
        cvHotels.setOnClickListener(v -> 
            startActivity(new Intent(this, HotelBookingActivity.class)));
        
        cvBranches.setOnClickListener(v -> 
            startActivity(new Intent(this, BranchLocatorActivity.class)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


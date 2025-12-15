package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.BankBranch;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Branch Locator Activity - Google Maps integration for bank branches
 */
public class BranchLocatorActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_locator);

        dataManager = DataManager.getInstance(this);

        setupToolbar();
        initializeMap();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Branch Locator");
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Get bank branches
        List<BankBranch> branches = dataManager.getMockBankBranches();

        // Add markers for each branch
        for (BankBranch branch : branches) {
            LatLng position = new LatLng(branch.getLatitude(), branch.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(branch.getBranchName())
                    .snippet(branch.getAddress()));
        }

        // Move camera to first branch
        if (!branches.isEmpty()) {
            BankBranch firstBranch = branches.get(0);
            LatLng hanoi = new LatLng(firstBranch.getLatitude(), firstBranch.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 12));
        }

        // Enable location button
        try {
            mMap.setMyLocationEnabled(false); // Set to true if you handle permissions
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}


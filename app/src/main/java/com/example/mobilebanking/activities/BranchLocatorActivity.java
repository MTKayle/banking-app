package com.example.mobilebanking.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.BranchAdapter;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.BranchApiService;
import com.example.mobilebanking.api.TrackAsiaApiService;
import com.example.mobilebanking.api.dto.BranchDTO;
import com.example.mobilebanking.api.dto.BranchListResponse;
import com.example.mobilebanking.api.dto.DirectionsResponse;
import com.example.mobilebanking.api.dto.NearestBranchRequest;
import com.example.mobilebanking.api.dto.NearestBranchResponse;
import com.example.mobilebanking.utils.PolylineDecoder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Branch Locator Activity - Google Maps integration với API thật
 */
public class BranchLocatorActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private static final String TAG = "BranchLocatorActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private GoogleMap mMap;
    private BranchApiService branchApiService;
    private TrackAsiaApiService trackAsiaApiService;
    private FusedLocationProviderClient fusedLocationClient;
    
    // UI Components
    private RecyclerView rvBranches;
    private BranchAdapter branchAdapter;
    private ProgressBar progressBar;
    private MaterialButton btnFindNearest;
    private TextView tvBranchListTitle;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    
    // Data
    private List<BranchDTO> allBranches = new ArrayList<>();
    private Map<Marker, BranchDTO> markerBranchMap = new HashMap<>();
    private Location currentLocation;
    private Polyline currentPolyline; // Đường đi hiện tại trên map
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_locator);
        
        branchApiService = ApiClient.getBranchApiService();
        trackAsiaApiService = ApiClient.getTrackAsiaApiService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        setupToolbar();
        setupBottomSheet();
        setupRecyclerView();
        initializeMap();
        setupButtons();
        
        // Load tất cả chi nhánh
        loadAllBranches();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tìm Chi Nhánh");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(600);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    
    private void setupRecyclerView() {
        rvBranches = findViewById(R.id.rv_branches);
        progressBar = findViewById(R.id.progress_bar);
        tvBranchListTitle = findViewById(R.id.tv_branch_list_title);
        
        branchAdapter = new BranchAdapter();
        rvBranches.setLayoutManager(new LinearLayoutManager(this));
        rvBranches.setAdapter(branchAdapter);
        
        // Click vào branch trong list -> focus trên map
        branchAdapter.setOnBranchClickListener(new BranchAdapter.OnDirectionsClickListener() {
            @Override
            public void onBranchClick(BranchDTO branch) {
                focusOnBranch(branch);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            
            @Override
            public void onDirectionsClick(BranchDTO branch) {
                // Vẽ đường đi từ vị trí hiện tại đến chi nhánh
                drawDirections(branch);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }
    
    private void setupButtons() {
        btnFindNearest = findViewById(R.id.btn_find_nearest);
        btnFindNearest.setOnClickListener(v -> findNearestBranches());
    }
    
    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Setup map UI
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        
        // Enable my location nếu có permission
        enableMyLocation();
        
        // Click marker -> hiển thị info window và scroll đến item trong list
        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            BranchDTO branch = markerBranchMap.get(marker);
            if (branch != null) {
                scrollToBranchInList(branch);
            }
            return true;
        });
    }
    
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = location;
                    }
                });
    }
    
    /**
     * Load tất cả chi nhánh từ API
     */
    private void loadAllBranches() {
        progressBar.setVisibility(View.VISIBLE);
        tvBranchListTitle.setText("Đang tải...");
        
        Call<BranchListResponse> call = branchApiService.getAllBranches();
        call.enqueue(new Callback<BranchListResponse>() {
            @Override
            public void onResponse(Call<BranchListResponse> call, Response<BranchListResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    BranchListResponse branchResponse = response.body();
                    if (branchResponse.getSuccess() && branchResponse.getData() != null) {
                        allBranches = branchResponse.getData();
                        tvBranchListTitle.setText("Danh sách chi nhánh (" + allBranches.size() + ")");
                        branchAdapter.setBranches(allBranches);
                        displayBranchesOnMap(allBranches, false);
                    } else {
                        Toast.makeText(BranchLocatorActivity.this, 
                            branchResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BranchLocatorActivity.this, 
                        "Không thể tải danh sách chi nhánh", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<BranchListResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvBranchListTitle.setText("Danh sách chi nhánh");
                Toast.makeText(BranchLocatorActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Tìm chi nhánh gần nhất dựa trên vị trí hiện tại
     */
    private void findNearestBranches() {
        if (currentLocation == null) {
            // Nếu chưa có location, request lại
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Vui lòng cấp quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            callNearestBranchesAPI();
                        } else {
                            Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            callNearestBranchesAPI();
        }
    }
    
    private void callNearestBranchesAPI() {
        progressBar.setVisibility(View.VISIBLE);
        tvBranchListTitle.setText("Đang tìm chi nhánh gần nhất...");
        
        NearestBranchRequest request = new NearestBranchRequest(
                currentLocation.getLatitude(),
                currentLocation.getLongitude()
        );
        
        Call<NearestBranchResponse> call = branchApiService.getNearestBranches(request);
        call.enqueue(new Callback<NearestBranchResponse>() {
            @Override
            public void onResponse(Call<NearestBranchResponse> call, Response<NearestBranchResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    NearestBranchResponse nearestResponse = response.body();
                    if (nearestResponse.getSuccess() && nearestResponse.getData() != null) {
                        List<BranchDTO> nearestBranches = nearestResponse.getData();
                        tvBranchListTitle.setText("Chi nhánh gần nhất (" + nearestBranches.size() + ")");
                        branchAdapter.setBranches(nearestBranches);
                        displayBranchesOnMap(nearestBranches, true);
                        
                        // Focus vào chi nhánh gần nhất
                        if (!nearestBranches.isEmpty()) {
                            focusOnBranch(nearestBranches.get(0));
                        }
                    } else {
                        Toast.makeText(BranchLocatorActivity.this,
                            nearestResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BranchLocatorActivity.this,
                        "Không thể tìm chi nhánh gần nhất", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<NearestBranchResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvBranchListTitle.setText("Danh sách chi nhánh");
                Toast.makeText(BranchLocatorActivity.this,
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Hiển thị các chi nhánh trên map
     */
    private void displayBranchesOnMap(List<BranchDTO> branches, boolean isNearestSearch) {
        if (mMap == null) return;
        
        // Xóa tất cả markers cũ
        mMap.clear();
        markerBranchMap.clear();
        
        if (branches.isEmpty()) return;
        
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        
        for (int i = 0; i < branches.size(); i++) {
            BranchDTO branch = branches.get(i);
            LatLng position = new LatLng(branch.getLatitude(), branch.getLongitude());
            
            // Marker đầu tiên (gần nhất) có màu khác
            BitmapDescriptor icon;
            if (isNearestSearch && i == 0) {
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else {
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
            
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(branch.getName())
                    .snippet(branch.getAddress())
                    .icon(icon));
            
            if (marker != null) {
                markerBranchMap.put(marker, branch);
            }
            
            boundsBuilder.include(position);
        }
        
        // Zoom camera để hiển thị tất cả markers
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 150; // padding in pixels
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            // Fallback: zoom vào chi nhánh đầu tiên
            BranchDTO firstBranch = branches.get(0);
            LatLng position = new LatLng(firstBranch.getLatitude(), firstBranch.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
        }
    }
    
    /**
     * Focus camera vào một chi nhánh cụ thể
     */
    private void focusOnBranch(BranchDTO branch) {
        if (mMap == null) return;
        
        LatLng position = new LatLng(branch.getLatitude(), branch.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        
        // Tìm và hiển thị info window của marker
        for (Map.Entry<Marker, BranchDTO> entry : markerBranchMap.entrySet()) {
            if (entry.getValue().getBranchId().equals(branch.getBranchId())) {
                entry.getKey().showInfoWindow();
                break;
            }
        }
    }
    
    /**
     * Scroll RecyclerView đến branch được chọn
     */
    private void scrollToBranchInList(BranchDTO branch) {
        List<BranchDTO> currentBranches = branchAdapter.getBranches();
        for (int i = 0; i < currentBranches.size(); i++) {
            if (currentBranches.get(i).getBranchId().equals(branch.getBranchId())) {
                rvBranches.smoothScrollToPosition(i);
                break;
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    /**
     * Vẽ đường đi từ vị trí hiện tại đến chi nhánh
     * Sử dụng OSRM API (miễn phí, không cần key) để lấy đường đi thực tế
     */
    private void drawDirections(BranchDTO branch) {
        if (currentLocation == null) {
            Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Xóa đường đi cũ nếu có
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        
        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        
        // Tạo coordinates string: "lng1,lat1;lng2,lat2" (LƯU Ý: lng trước, lat sau)
        String coordinates = String.format(java.util.Locale.US, "%f,%f;%f,%f",
                currentLocation.getLongitude(),
                currentLocation.getLatitude(),
                branch.getLongitude(),
                branch.getLatitude());
        
        // Gọi OSRM API (miễn phí, không cần API key)
        Call<DirectionsResponse> call = trackAsiaApiService.getDirections(
                "car",          // Profile: car (xe hơi), foot (đi bộ), bike (xe đạp)
                coordinates,    // Tọa độ điểm đầu và điểm cuối
                false,          // Không cần tuyến đường thay thế
                true,           // Có trả về hướng dẫn từng bước
                "polyline",     // Định dạng geometry
                "full"          // Chi tiết đầy đủ
        );
        
        call.enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    DirectionsResponse directionsResponse = response.body();
                    
                    if (directionsResponse.isSuccessful() && 
                        directionsResponse.getRoutes() != null && 
                        !directionsResponse.getRoutes().isEmpty()) {
                        
                        DirectionsResponse.Route route = directionsResponse.getRoutes().get(0);
                        
                        // Decode polyline và vẽ lên map
                        String encodedPolyline = route.getGeometry();
                        List<LatLng> points = PolylineDecoder.decode(encodedPolyline);
                        
                        if (!points.isEmpty()) {
                            // Vẽ đường đi lên map
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(points)
                                    .width(12)
                                    .color(Color.parseColor("#1976D2"))
                                    .geodesic(true);
                            
                            currentPolyline = mMap.addPolyline(polylineOptions);
                            
                            // Focus camera để hiển thị toàn bộ đường đi
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                            for (LatLng point : points) {
                                boundsBuilder.include(point);
                            }
                            
                            try {
                                LatLngBounds bounds = boundsBuilder.build();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
                            } catch (Exception e) {
                                Log.e(TAG, "Error animating camera", e);
                            }
                            
                            // Hiển thị thông tin đường đi
                            showRouteInfo(route, branch);
                        }
                    } else {
                        Toast.makeText(BranchLocatorActivity.this, 
                            "Không tìm thấy đường đi", Toast.LENGTH_SHORT).show();
                        // Fallback: vẽ đường thẳng
                        drawStraightLine(branch);
                    }
                } else {
                    Toast.makeText(BranchLocatorActivity.this, 
                        "Lỗi khi tìm đường: " + response.message(), Toast.LENGTH_SHORT).show();
                    // Fallback: vẽ đường thẳng
                    drawStraightLine(branch);
                }
            }
            
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error getting directions", t);
                Toast.makeText(BranchLocatorActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Fallback: vẽ đường thẳng
                drawStraightLine(branch);
            }
        });
    }
    
    /**
     * Vẽ đường thẳng khi không lấy được đường đi từ API
     */
    private void drawStraightLine(BranchDTO branch) {
        if (currentLocation == null) return;
        
        // Xóa đường đi cũ nếu có
        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        
        LatLng origin = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng destination = new LatLng(branch.getLatitude(), branch.getLongitude());
        
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origin)
                .add(destination)
                .width(12)
                .color(Color.parseColor("#FF9800"))
                .geodesic(true);
        
        currentPolyline = mMap.addPolyline(polylineOptions);
        
        // Tính khoảng cách
        float[] results = new float[1];
        Location.distanceBetween(
                currentLocation.getLatitude(), 
                currentLocation.getLongitude(),
                branch.getLatitude(), 
                branch.getLongitude(), 
                results);
        float distanceKm = results[0] / 1000;
        
        // Focus camera
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(origin);
        boundsBuilder.include(destination);
        
        try {
            LatLngBounds bounds = boundsBuilder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
        } catch (Exception e) {
            Log.e(TAG, "Error animating camera", e);
        }
        
        Toast.makeText(this, 
            String.format("Khoảng cách: %.2f km (đường chim bay)", distanceKm), 
            Toast.LENGTH_LONG).show();
    }
    
    /**
     * Hiển thị thông tin đường đi (khoảng cách, thời gian)
     */
    private void showRouteInfo(DirectionsResponse.Route route, BranchDTO branch) {
        double distanceKm = route.getDistanceKm();
        int durationMinutes = route.getDurationMinutes();
        
        String message = String.format(java.util.Locale.getDefault(),
                "📍 %s\n\n" +
                "🚗 Khoảng cách: %.2f km\n" +
                "⏱️ Thời gian: %d phút\n\n" +
                "Đường đi đã được vẽ trên bản đồ",
                branch.getName(),
                distanceKm,
                durationMinutes);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chỉ đường")
                .setMessage(message)
                .setPositiveButton("Xem hướng dẫn", (dialog, which) -> {
                    showStepByStepDirections(route);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }
    
    /**
     * Hiển thị hướng dẫn từng bước
     */
    private void showStepByStepDirections(DirectionsResponse.Route route) {
        if (route.getLegs() == null || route.getLegs().isEmpty()) {
            Toast.makeText(this, "Không có hướng dẫn chi tiết", Toast.LENGTH_SHORT).show();
            return;
        }
        
        DirectionsResponse.Leg leg = route.getLegs().get(0);
        List<DirectionsResponse.Step> steps = leg.getSteps();
        
        if (steps == null || steps.isEmpty()) {
            Toast.makeText(this, "Không có hướng dẫn chi tiết", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo danh sách hướng dẫn
        StringBuilder directions = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            DirectionsResponse.Step step = steps.get(i);
            DirectionsResponse.Maneuver maneuver = step.getManeuver();
            
            if (maneuver != null && maneuver.getInstruction() != null) {
                directions.append(String.format("%d. %s\n", i + 1, maneuver.getInstruction()));
                
                if (step.getDistance() > 0) {
                    if (step.getDistance() >= 1000) {
                        directions.append(String.format("   (%.2f km)\n", step.getDistance() / 1000));
                    } else {
                        directions.append(String.format("   (%.0f m)\n", step.getDistance()));
                    }
                }
                directions.append("\n");
            }
        }
        
        // Hiển thị dialog với hướng dẫn
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hướng dẫn từng bước")
                .setMessage(directions.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }
}

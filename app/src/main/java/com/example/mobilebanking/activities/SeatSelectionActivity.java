package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.MovieApiService;
import com.example.mobilebanking.api.dto.SeatListResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SeatSelectionActivity
 * Hiển thị sơ đồ ghế từ API và cho phép chọn ghế với màu sắc theo loại ghế.
 */
public class SeatSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SCREENING_ID = "extra_screening_id";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";

    // Default prices (fallback khi không có API)
    private static final double DEFAULT_PRICE_STANDARD = 105000;
    private static final double DEFAULT_PRICE_VIP = 110500;
    private static final double DEFAULT_PRICE_COUPLE = 270000;

    private enum SeatType {
        STANDARD,
        VIP,
        COUPLE
    }

    private static class SeatInfo {
        final String code;
        final SeatType type;
        final String status; // AVAILABLE, BOOKED, RESERVED, MAINTENANCE
        final double price; // Giá từ API
        final Long seatId; // ID ghế từ API để gửi khi đặt vé
        boolean selected;

        // Constructor với giá mặc định
        SeatInfo(String code, SeatType type) {
            this.code = code;
            this.type = type;
            this.status = "AVAILABLE";
            this.seatId = null;
            // Giá mặc định theo loại ghế
            switch (type) {
                case VIP:
                    this.price = DEFAULT_PRICE_VIP;
                    break;
                case COUPLE:
                    this.price = DEFAULT_PRICE_COUPLE;
                    break;
                default:
                    this.price = DEFAULT_PRICE_STANDARD;
                    break;
            }
        }
        
        // Constructor với đầy đủ thông tin từ API
        SeatInfo(String code, SeatType type, String status, double price, Long seatId) {
            this.code = code;
            this.type = type;
            this.status = status;
            this.price = price;
            this.seatId = seatId;
        }
    }

    private final Map<Button, SeatInfo> seats = new LinkedHashMap<>();
    
    // Map để lưu button theo seatLabel để dễ lookup
    private final Map<String, Button> seatButtonMap = new LinkedHashMap<>();

    private Long screeningId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection_light);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SCREENING_ID)) {
            screeningId = intent.getLongExtra(EXTRA_SCREENING_ID, -1);
            if (screeningId <= 0) {
                screeningId = null;
            }
        }

        bindHeaderFromExtras();
        bindScreeningInfo();
        setupBottomBar();
        setupSeatMap();
        
        if (screeningId != null) {
            // Gọi API lấy danh sách ghế
            fetchSeats(screeningId);
        }
        // Dynamic seats đã được tạo trong setupSeatMap(), không cần fallback
    }
    
    /**
     * Bind thông tin suất chiếu vào header
     */
    private void bindScreeningInfo() {
        TextView tvCinemaName = findViewById(R.id.tv_cinema_name);
        TextView tvShowtime = findViewById(R.id.tv_showtime);
        TextView tvRoom = findViewById(R.id.tv_room);
        
        String cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        String showtime = getIntent().getStringExtra(EXTRA_SHOWTIME);
        String showDate = getIntent().getStringExtra(EXTRA_SHOWDATE);
        String room = getIntent().getStringExtra(EXTRA_ROOM);
        
        if (tvCinemaName != null && cinemaName != null) {
            tvCinemaName.setText(cinemaName);
        }
        
        if (tvShowtime != null) {
            String showtimeText = showtime != null ? showtime : "";
            if (showDate != null) {
                showtimeText += " | " + showDate;
            }
            tvShowtime.setText(showtimeText);
        }
        
        if (tvRoom != null && room != null) {
            tvRoom.setText(room);
        }
    }
    
    /**
     * Chuẩn bị container cho seat map - KHÔNG tạo ghế sẵn
     * Ghế sẽ được tạo động từ API response
     */
    private void setupSeatMap() {
        LinearLayout seatMapContainer = findViewById(R.id.layout_seat_map);
        if (seatMapContainer == null) return;
        
        // Xóa các row cũ (nếu có)
        seatMapContainer.removeAllViews();
        seats.clear();
        seatButtonMap.clear();
        
        // Hiển thị loading text
        TextView loadingText = new TextView(this);
        loadingText.setText("Đang tải sơ đồ ghế...");
        loadingText.setTextColor(getColor(R.color.movie_text_secondary));
        loadingText.setTextSize(14);
        loadingText.setPadding(16, 32, 16, 32);
        seatMapContainer.addView(loadingText);
    }
    
    /**
     * Tạo sơ đồ ghế hoàn toàn từ dữ liệu API
     */
    private void buildSeatMapFromAPI(List<SeatListResponse.SeatItem> seatItems) {
        LinearLayout seatMapContainer = findViewById(R.id.layout_seat_map);
        if (seatMapContainer == null || seatItems == null) return;
        
        // Xóa loading và tất cả ghế cũ
        seatMapContainer.removeAllViews();
        seats.clear();
        seatButtonMap.clear();
        
        float density = getResources().getDisplayMetrics().density;
        
        // Nhóm ghế theo hàng (rowLabel)
        Map<String, List<SeatListResponse.SeatItem>> rowsMap = new TreeMap<>();
        for (SeatListResponse.SeatItem seat : seatItems) {
            String rowLabel = seat.getRowLabel();
            if (rowLabel == null) continue;
            
            if (!rowsMap.containsKey(rowLabel)) {
                rowsMap.put(rowLabel, new ArrayList<>());
            }
            rowsMap.get(rowLabel).add(seat);
        }
        
        // Tạo từng hàng ghế
        for (Map.Entry<String, List<SeatListResponse.SeatItem>> entry : rowsMap.entrySet()) {
            String rowLabel = entry.getKey();
            List<SeatListResponse.SeatItem> rowSeats = entry.getValue();
            
            // Sắp xếp ghế theo số ghế giảm dần (14, 13, 12, ...)
            rowSeats.sort((a, b) -> {
                int numA = a.getSeatNumber() != null ? a.getSeatNumber() : 0;
                int numB = b.getSeatNumber() != null ? b.getSeatNumber() : 0;
                return numB - numA; // Giảm dần
            });
            
            // Kiểm tra xem có phải hàng ghế đôi không
            boolean isCoupleRow = rowSeats.stream()
                    .anyMatch(s -> "COUPLE".equalsIgnoreCase(s.getSeatType()));
            
            if (isCoupleRow) {
                // Tạo hàng ghế đôi
                LinearLayout coupleRowLayout = createCoupleRowFromAPI(rowLabel, rowSeats);
                seatMapContainer.addView(coupleRowLayout);
            } else {
                // Tạo hàng ghế thường/VIP
                LinearLayout rowLayout = createSeatRowFromAPI(rowLabel, rowSeats);
                seatMapContainer.addView(rowLayout);
            }
        }
    }
    
    /**
     * Tạo một hàng ghế từ dữ liệu API
     */
    private LinearLayout createSeatRowFromAPI(String rowLabel, List<SeatListResponse.SeatItem> rowSeats) {
        float density = getResources().getDisplayMetrics().density;
        
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, (int)(4 * density), 0, 0);
        rowLayout.setLayoutParams(rowParams);
        
        // Row label
        TextView tvRowLabel = new TextView(this);
        tvRowLabel.setText(rowLabel);
        tvRowLabel.setTextColor(getColor(R.color.movie_text_secondary));
        tvRowLabel.setTextSize(11);
        tvRowLabel.setGravity(android.view.Gravity.CENTER);
        tvRowLabel.setLayoutParams(new LinearLayout.LayoutParams(
                (int)(20 * density), LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        rowLayout.addView(tvRowLabel);
        
        // Tạo button cho mỗi ghế
        for (SeatListResponse.SeatItem seat : rowSeats) {
            String seatLabel = seat.getSeatLabel();
            String seatTypeStr = seat.getSeatType();
            String status = seat.getStatus() != null ? seat.getStatus() : "AVAILABLE";
            double price = seat.getFinalPrice() != null ? seat.getFinalPrice() : 
                          (seat.getBasePrice() != null ? seat.getBasePrice() : 0);
            Long seatId = seat.getSeatId();
            
            // Xác định loại ghế
            SeatType seatType = SeatType.STANDARD;
            if ("VIP".equalsIgnoreCase(seatTypeStr)) {
                seatType = SeatType.VIP;
            }
            
            // Tạo button ghế
            Button seatButton = createSeatButtonForAPI(seatLabel, seatType, status);
            rowLayout.addView(seatButton);
            
            // Lưu vào map
            seatButtonMap.put(seatLabel, seatButton);
            
            // Tạo SeatInfo
            SeatInfo info = new SeatInfo(seatLabel, seatType, status, price, seatId);
            seats.put(seatButton, info);
            
            // Click listener
            seatButton.setOnClickListener(v -> {
                SeatInfo currentInfo = seats.get(seatButton);
                if (currentInfo != null && "AVAILABLE".equalsIgnoreCase(currentInfo.status)) {
                    currentInfo.selected = !currentInfo.selected;
                    updateSeatVisual(seatButton, currentInfo);
                    updateSummaryAndButtonState();
                }
            });
        }
        
        return rowLayout;
    }
    
    /**
     * Tạo hàng ghế đôi từ API
     * Mỗi ghế có seatType = COUPLE đã là 1 ghế đôi, không cần ghép 2 ghế
     */
    private LinearLayout createCoupleRowFromAPI(String rowLabel, List<SeatListResponse.SeatItem> rowSeats) {
        float density = getResources().getDisplayMetrics().density;
        
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, (int)(8 * density), 0, 0);
        rowLayout.setLayoutParams(rowParams);
        
        // Row label
        TextView tvRowLabel = new TextView(this);
        tvRowLabel.setText(rowLabel);
        tvRowLabel.setTextColor(getColor(R.color.movie_text_secondary));
        tvRowLabel.setTextSize(11);
        tvRowLabel.setGravity(android.view.Gravity.CENTER);
        tvRowLabel.setLayoutParams(new LinearLayout.LayoutParams(
                (int)(20 * density), LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        rowLayout.addView(tvRowLabel);
        
        // Mỗi ghế COUPLE từ API đã là 1 ghế đôi hoàn chỉnh
        for (SeatListResponse.SeatItem seat : rowSeats) {
            String seatLabel = seat.getSeatLabel();
            String status = seat.getStatus() != null ? seat.getStatus() : "AVAILABLE";
            double price = seat.getFinalPrice() != null ? seat.getFinalPrice() : 
                          (seat.getBasePrice() != null ? seat.getBasePrice() : 0);
            Long seatId = seat.getSeatId();
            
            // Tạo button ghế đôi
            Button coupleButton = createCoupleSeatButtonForAPI(seatLabel, status);
            rowLayout.addView(coupleButton);
            
            // Lưu vào map
            seatButtonMap.put(seatLabel, coupleButton);
            
            // Tạo SeatInfo
            SeatInfo info = new SeatInfo(seatLabel, SeatType.COUPLE, status, price, seatId);
            seats.put(coupleButton, info);
            
            // Click listener
            coupleButton.setOnClickListener(v -> {
                SeatInfo currentInfo = seats.get(coupleButton);
                if (currentInfo != null && "AVAILABLE".equalsIgnoreCase(currentInfo.status)) {
                    currentInfo.selected = !currentInfo.selected;
                    updateCoupleSeatVisual(coupleButton, currentInfo);
                    updateSummaryAndButtonState();
                }
            });
        }
        
        return rowLayout;
    }
    
    /**
     * Tạo button ghế từ API data
     */
    private Button createSeatButtonForAPI(String seatLabel, SeatType seatType, String status) {
        float density = getResources().getDisplayMetrics().density;
        
        Button button = new Button(this);
        String seatNumber = seatLabel.replaceAll("[A-Z]", "");
        button.setText(seatNumber);
        button.setTextSize(9);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setPadding(0, 0, 0, 0);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int)(22 * density),
                (int)(22 * density)
        );
        params.setMargins((int)(2 * density), 0, (int)(2 * density), 0);
        button.setLayoutParams(params);
        
        // Set background và enable dựa trên status
        if ("BOOKED".equalsIgnoreCase(status) || "RESERVED".equalsIgnoreCase(status)) {
            button.setBackgroundResource(R.drawable.bg_seat_booked_gray);
            button.setEnabled(false);
        } else {
            // Màu theo loại ghế
            if (seatType == SeatType.VIP) {
                button.setBackgroundResource(R.drawable.bg_seat_vip_orange);
            } else {
                button.setBackgroundResource(R.drawable.bg_seat_standard);
            }
        }
        
        return button;
    }
    
    /**
     * Tạo button ghế đôi từ API
     */
    private Button createCoupleSeatButtonForAPI(String coupleCode, String status) {
        float density = getResources().getDisplayMetrics().density;
        
        Button button = new Button(this);
        button.setText(coupleCode);
        button.setTextSize(8);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setPadding((int)(4 * density), 0, (int)(4 * density), 0);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int)(48 * density),
                (int)(24 * density)
        );
        params.setMargins((int)(2 * density), 0, (int)(2 * density), 0);
        button.setLayoutParams(params);
        
        // Set background dựa trên status
        if ("BOOKED".equalsIgnoreCase(status) || "RESERVED".equalsIgnoreCase(status)) {
            button.setBackgroundResource(R.drawable.bg_seat_booked_gray);
            button.setEnabled(false);
        } else {
            button.setBackgroundResource(R.drawable.bg_seat_legend_couple);
        }
        
        return button;
    }
    
    /**
     * Cập nhật visual cho ghế đôi
     */
    private void updateCoupleSeatVisual(Button button, SeatInfo info) {
        if (info.selected) {
            button.setBackgroundResource(R.drawable.bg_seat_selected);
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundResource(R.drawable.bg_seat_legend_couple);
            button.setTextColor(Color.WHITE);
        }
    }
    
    /**
     * Gọi API để lấy danh sách ghế của suất chiếu
     */
    private void fetchSeats(Long screeningId) {
        MovieApiService movieApiService = ApiClient.getMovieApiService();
        Call<SeatListResponse> call = movieApiService.getSeatsByScreening(screeningId);
        
        call.enqueue(new Callback<SeatListResponse>() {
            @Override
            public void onResponse(Call<SeatListResponse> call, Response<SeatListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SeatListResponse seatResponse = response.body();
                    if (seatResponse.getSuccess() != null && seatResponse.getSuccess() 
                            && seatResponse.getData() != null) {
                        List<SeatListResponse.SeatItem> seatItems = seatResponse.getData().getSeats();
                        // Update price legend từ API
                        updatePriceLegendFromAPI(seatItems);
                        // Tạo sơ đồ ghế hoàn toàn từ API
                        buildSeatMapFromAPI(seatItems);
                        // Cập nhật UI
                        updateSummaryAndButtonState();
                    } else {
                        showError("Không thể lấy danh sách ghế");
                    }
                } else {
                    showError("Không thể lấy danh sách ghế");
                }
            }
            
            @Override
            public void onFailure(Call<SeatListResponse> call, Throwable t) {
                showError("Không thể kết nối đến server");
                // Dynamic seats đã được tạo trong setupSeatMap()
                t.printStackTrace();
            }
        });
    }
    
    /**
     * Cập nhật giá trong legend từ API
     */
    private void updatePriceLegendFromAPI(List<SeatListResponse.SeatItem> seatItems) {
        if (seatItems == null || seatItems.isEmpty()) return;
        
        double standardPrice = 0;
        double vipPrice = 0;
        double couplePrice = 0;
        
        // Tìm giá của từng loại ghế từ API
        for (SeatListResponse.SeatItem seat : seatItems) {
            String seatType = seat.getSeatType();
            double price = seat.getFinalPrice() != null ? seat.getFinalPrice() : 0;
            if (price == 0 && seat.getBasePrice() != null) {
                price = seat.getBasePrice();
            }
            
            if ("STANDARD".equalsIgnoreCase(seatType) && standardPrice == 0) {
                standardPrice = price;
            } else if ("VIP".equalsIgnoreCase(seatType) && vipPrice == 0) {
                vipPrice = price;
            } else if ("COUPLE".equalsIgnoreCase(seatType) && couplePrice == 0) {
                couplePrice = price;
            }
            
            // Nếu đã tìm được cả 3 loại, thoát sớm
            if (standardPrice > 0 && vipPrice > 0 && couplePrice > 0) {
                break;
            }
        }
        
        // Cập nhật TextView hiển thị giá
        TextView tvPriceStandard = findViewById(R.id.tv_price_standard);
        TextView tvPriceVip = findViewById(R.id.tv_price_vip);
        TextView tvPriceCouple = findViewById(R.id.tv_price_couple);
        
        if (tvPriceStandard != null && standardPrice > 0) {
            tvPriceStandard.setText(formatPriceForLegend(standardPrice));
        }
        if (tvPriceVip != null && vipPrice > 0) {
            tvPriceVip.setText(formatPriceForLegend(vipPrice));
        }
        if (tvPriceCouple != null && couplePrice > 0) {
            tvPriceCouple.setText(formatPriceForLegend(couplePrice));
        }
    }
    
    /**
     * Format giá cho legend (không có đơn vị đ)
     */
    private String formatPriceForLegend(double amount) {
        long amountLong = Math.round(amount);
        String raw = String.valueOf(amountLong);
        StringBuilder sb = new StringBuilder();
        
        int len = raw.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(",");
            }
            sb.append(raw.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Cập nhật màu sắc ghế từ API
     */
    private void updateSeatVisualFromAPI(Button button, SeatInfo info, String status) {
        if (button == null || info == null) return;
        
        // Nếu ghế đã được đặt
        if ("BOOKED".equalsIgnoreCase(status) || "RESERVED".equalsIgnoreCase(status)) {
            button.setBackgroundResource(R.drawable.bg_seat_booked);
            button.setTextColor(Color.WHITE);
            button.setEnabled(false);
            return;
        }
        
        // Nếu ghế còn trống, áp dụng màu theo loại ghế
        if ("AVAILABLE".equalsIgnoreCase(status)) {
            if (info.selected) {
                // Ghế đã chọn: màu đỏ cam với checkmark
                button.setBackgroundResource(R.drawable.bg_seat_selected);
                button.setTextColor(Color.WHITE);
                button.setText("");
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_check_seat);
            } else {
                // Ghế chưa chọn: màu theo loại
                if (info.type == SeatType.STANDARD) {
                    button.setBackgroundResource(R.drawable.bg_seat_available); // Green
                } else if (info.type == SeatType.VIP) {
                    button.setBackgroundResource(R.drawable.bg_seat_vip); // Yellow
                } else if (info.type == SeatType.COUPLE) {
                    button.setBackgroundResource(R.drawable.bg_seat_couple); // Red
                }
                button.setTextColor(Color.WHITE);
                button.setText(extractSeatNumber(info.code));
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        }
        
        button.invalidate();
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void bindHeaderFromExtras() {
        TextView tvHeaderTitle = findViewById(R.id.tv_header_title);

        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);

        if (movieTitle != null && tvHeaderTitle != null) {
            tvHeaderTitle.setText(movieTitle);
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupBottomBar() {
        TextView tvSelectedSeats = findViewById(R.id.tv_selected_seats);
        TextView tvTotalPrice = findViewById(R.id.tv_total_price);
        Button btnContinue = findViewById(R.id.btn_continue);

        // Ban đầu chưa chọn ghế
        tvSelectedSeats.setText("Ghế đã chọn: Chưa có");
        tvTotalPrice.setText("Tổng tiền: 0đ");

        btnContinue.setEnabled(false);
        btnContinue.setBackgroundColor(0xFF26313A);
        btnContinue.setTextColor(0xFF6B747F);

        btnContinue.setOnClickListener(v -> {
            // Chuyển sang màn Thanh toán vé phim
            String selectedSeats = buildSelectedSeatString();
            String totalAmount = formatTotalAmount();
            
            // Lấy danh sách seatIds đã chọn
            long[] seatIds = getSelectedSeatIds();

            Intent intent = new Intent(this, MoviePaymentActivity.class);
            intent.putExtra(MoviePaymentActivity.EXTRA_SCREENING_ID, screeningId);
            intent.putExtra(MoviePaymentActivity.EXTRA_SEAT_IDS, seatIds);
            intent.putExtra(MoviePaymentActivity.EXTRA_MOVIE_TITLE,
                    getIntent().getStringExtra(EXTRA_MOVIE_TITLE));
            intent.putExtra(MoviePaymentActivity.EXTRA_CINEMA_NAME,
                    getIntent().getStringExtra(EXTRA_CINEMA_NAME));
            intent.putExtra(MoviePaymentActivity.EXTRA_CINEMA_ADDRESS,
                    getIntent().getStringExtra(EXTRA_CINEMA_ADDRESS));
            intent.putExtra(MoviePaymentActivity.EXTRA_SHOWTIME,
                    getIntent().getStringExtra(EXTRA_SHOWTIME));
            intent.putExtra(MoviePaymentActivity.EXTRA_SHOWDATE,
                    getIntent().getStringExtra(EXTRA_SHOWDATE));
            intent.putExtra(MoviePaymentActivity.EXTRA_ROOM,
                    getIntent().getStringExtra(EXTRA_ROOM));
            intent.putExtra(MoviePaymentActivity.EXTRA_SEATS, selectedSeats);
            intent.putExtra(MoviePaymentActivity.EXTRA_TOTAL_AMOUNT, totalAmount);
            startActivity(intent);
        });
    }

    private void updateSeatVisual(Button button, SeatInfo info) {
        if (info.selected) {
            // Trạng thái được chọn: nền cam đỏ sáng với border trắng dày, hiển thị dấu tích
            button.setBackgroundResource(R.drawable.bg_seat_selected);
            button.setText(""); // Xóa text số
            button.setTextColor(Color.WHITE);
            // Thêm icon checkmark
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_check_seat);
            button.setCompoundDrawablePadding(0);
            // Đảm bảo button được refresh
            button.invalidate();
        } else {
            // Trạng thái chưa chọn: màu theo loại ghế
            if (info.type == SeatType.STANDARD) {
                button.setBackgroundResource(R.drawable.bg_seat_available); // Green
            } else if (info.type == SeatType.VIP) {
                button.setBackgroundResource(R.drawable.bg_seat_vip); // Yellow
            } else if (info.type == SeatType.COUPLE) {
                button.setBackgroundResource(R.drawable.bg_seat_couple); // Red
            }
            // Khôi phục text số từ mã ghế (ví dụ "A1" -> "1", "B10" -> "10")
            String seatNumber = extractSeatNumber(info.code);
            button.setText(seatNumber);
            button.setTextColor(Color.WHITE);
            // Xóa icon checkmark
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            // Đảm bảo button được refresh
            button.invalidate();
        }
    }
    
    /**
     * Trích xuất số ghế từ mã ghế (ví dụ "A1" -> "1", "B10" -> "10", "K1-K2" -> "1-2")
     */
    private String extractSeatNumber(String seatCode) {
        if (seatCode == null || seatCode.isEmpty()) return "";
        // Nếu có dấu gạch ngang (ghế đôi), giữ nguyên
        if (seatCode.contains("-")) {
            // "K1-K2" -> "1-2"
            String[] parts = seatCode.split("-");
            if (parts.length >= 2) {
                String num1 = parts[0].replaceAll("[A-Z]", "");
                String num2 = parts[1].replaceAll("[A-Z]", "");
                return num1 + "-" + num2;
            }
        }
        // Loại bỏ chữ cái, chỉ giữ số
        return seatCode.replaceAll("[A-Z]", "");
    }

    private void updateSummaryAndButtonState() {
        TextView tvSelectedSeats = findViewById(R.id.tv_selected_seats);
        TextView tvTotalPrice = findViewById(R.id.tv_total_price);
        TextView tvSeatHint = findViewById(R.id.tv_seat_hint);
        Button btnContinue = findViewById(R.id.btn_continue);

        String seatsText = buildSelectedSeatString();
        double total = calculateTotal();

        if (seatsText.isEmpty()) {
            tvSelectedSeats.setText("Ghế đã chọn: Chưa có");
            tvTotalPrice.setText("Tổng tiền: 0đ");
            if (tvSeatHint != null) {
                tvSeatHint.setText("Quý khách vui lòng chọn vị trí ghế ngồi để tiếp tục");
            }
            btnContinue.setEnabled(false);
            btnContinue.setBackgroundResource(R.drawable.bg_movie_tab_light_inactive);
            btnContinue.setTextColor(getColor(R.color.movie_text_hint));
        } else {
            tvSelectedSeats.setText("Ghế đã chọn: " + seatsText);
            tvTotalPrice.setText("Tổng tiền: " + formatCurrency(total));
            if (tvSeatHint != null) {
                tvSeatHint.setText("Bạn đã chọn " + countSelectedSeats() + " ghế");
            }
            btnContinue.setEnabled(true);
            btnContinue.setBackgroundResource(R.drawable.bg_movie_tab_light_active);
            btnContinue.setTextColor(0xFFFFFFFF);
        }
    }
    
    /**
     * Đếm số ghế đã chọn
     */
    private int countSelectedSeats() {
        int count = 0;
        for (SeatInfo info : seats.values()) {
            if (info.selected) {
                count++;
            }
        }
        return count;
    }

    private String buildSelectedSeatString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (SeatInfo info : seats.values()) {
            if (info.selected) {
                joiner.add(info.code);
            }
        }
        return joiner.toString();
    }
    
    /**
     * Lấy danh sách seatIds đã chọn để gửi lên API
     */
    private long[] getSelectedSeatIds() {
        List<Long> selectedIds = new ArrayList<>();
        for (SeatInfo info : seats.values()) {
            if (info.selected && info.seatId != null) {
                selectedIds.add(info.seatId);
            }
        }
        long[] result = new long[selectedIds.size()];
        for (int i = 0; i < selectedIds.size(); i++) {
            result[i] = selectedIds.get(i);
        }
        return result;
    }

    private double calculateTotal() {
        double total = 0;
        for (SeatInfo info : seats.values()) {
            if (!info.selected) continue;
            // Sử dụng giá từ API (đã lưu trong SeatInfo.price)
            total += info.price;
        }
        return total;
    }

    private String formatTotalAmount() {
        double total = calculateTotal();
        return formatCurrency(total);
    }

    private String formatCurrency(double amount) {
        // Định dạng: 110.500đ
        long amountLong = Math.round(amount);
        String raw = String.valueOf(amountLong);
        StringBuilder sb = new StringBuilder();
        
        // Thêm dấu chấm phân cách hàng nghìn
        int len = raw.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(".");
            }
            sb.append(raw.charAt(i));
        }
        sb.append("đ");
        return sb.toString();
    }
}



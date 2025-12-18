package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * SeatSelectionActivity
 * FRONTEND ONLY: Hiển thị sơ đồ ghế và thông tin suất chiếu (không xử lý logic chọn ghế phức tạp).
 */
public class SeatSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_CINEMA_NAME = "extra_cinema_name";
    public static final String EXTRA_CINEMA_ADDRESS = "extra_cinema_address";
    public static final String EXTRA_SHOWTIME = "extra_showtime";
    public static final String EXTRA_SHOWDATE = "extra_showdate";
    public static final String EXTRA_ROOM = "extra_room";

    private static final int PRICE_STANDARD = 105000;
    private static final int PRICE_VIP = 110500;
    private static final int PRICE_COUPLE = 270000;

    private enum SeatType {
        STANDARD,
        VIP,
        COUPLE
    }

    private static class SeatInfo {
        final String code;
        final SeatType type;
        boolean selected;

        SeatInfo(String code, SeatType type) {
            this.code = code;
            this.type = type;
        }
    }

    private final Map<Button, SeatInfo> seats = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection_dark);

        bindHeaderFromExtras();
        setupSeatSelection();
        setupBottomBar();
    }

    private void bindHeaderFromExtras() {
        TextView tvHeaderTitle = findViewById(R.id.tv_header_title);
        TextView tvHeaderSub = findViewById(R.id.tv_header_sub);

        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        String cinemaName = getIntent().getStringExtra(EXTRA_CINEMA_NAME);
        String showtime = getIntent().getStringExtra(EXTRA_SHOWTIME);
        String room = getIntent().getStringExtra(EXTRA_ROOM);

        if (movieTitle != null) {
            tvHeaderTitle.setText(movieTitle);
        }

        StringBuilder sub = new StringBuilder();
        if (cinemaName != null) {
            sub.append(cinemaName);
        }
        if (showtime != null) {
            if (sub.length() > 0) sub.append(" • ");
            sub.append(showtime);
        }
        if (room != null) {
            if (sub.length() > 0) sub.append(" • ");
            sub.append(room);
        }
        if (sub.length() > 0) {
            tvHeaderSub.setText(sub.toString());
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
            // Chuyển sang màn Thanh toán vé phim, truyền dữ liệu mock qua Intent.
            String selectedSeats = buildSelectedSeatString();
            String totalAmount = formatTotalAmount();

            Intent intent = new Intent(this, MoviePaymentActivity.class);
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

    private void setupSeatSelection() {
        // Map từng Button với mã ghế và loại ghế để tính giá
        // Hàng A
        addSeat(R.id.seat_A1, "A1", SeatType.STANDARD);
        addSeat(R.id.seat_A2, "A2", SeatType.STANDARD);
        addSeat(R.id.seat_A3, "A3", SeatType.STANDARD);
        addSeat(R.id.seat_A4, "A4", SeatType.STANDARD);
        addSeat(R.id.seat_A5, "A5", SeatType.STANDARD);
        addSeat(R.id.seat_A6, "A6", SeatType.STANDARD);
        addSeat(R.id.seat_A7, "A7", SeatType.STANDARD);
        addSeat(R.id.seat_A8, "A8", SeatType.STANDARD);
        addSeat(R.id.seat_A9, "A9", SeatType.STANDARD);
        addSeat(R.id.seat_A10, "A10", SeatType.STANDARD);

        // Hàng B
        addSeat(R.id.seat_B1, "B1", SeatType.STANDARD);
        addSeat(R.id.seat_B2, "B2", SeatType.STANDARD);
        addSeat(R.id.seat_B3, "B3", SeatType.STANDARD);
        addSeat(R.id.seat_B4, "B4", SeatType.STANDARD);
        addSeat(R.id.seat_B5, "B5", SeatType.STANDARD);
        addSeat(R.id.seat_B6, "B6", SeatType.STANDARD);
        addSeat(R.id.seat_B7, "B7", SeatType.STANDARD);
        addSeat(R.id.seat_B8, "B8", SeatType.STANDARD);
        addSeat(R.id.seat_B9, "B9", SeatType.STANDARD);
        addSeat(R.id.seat_B10, "B10", SeatType.STANDARD);

        // Hàng C
        addSeat(R.id.seat_C1, "C1", SeatType.STANDARD);
        addSeat(R.id.seat_C2, "C2", SeatType.STANDARD);
        addSeat(R.id.seat_C3, "C3", SeatType.STANDARD);
        addSeat(R.id.seat_C4, "C4", SeatType.STANDARD);
        addSeat(R.id.seat_C5, "C5", SeatType.STANDARD);
        addSeat(R.id.seat_C6, "C6", SeatType.STANDARD);
        addSeat(R.id.seat_C7, "C7", SeatType.STANDARD);
        addSeat(R.id.seat_C8, "C8", SeatType.STANDARD);
        addSeat(R.id.seat_C9, "C9", SeatType.STANDARD);
        addSeat(R.id.seat_C10, "C10", SeatType.STANDARD);

        // Hàng D
        addSeat(R.id.seat_D1, "D1", SeatType.STANDARD);
        addSeat(R.id.seat_D2, "D2", SeatType.STANDARD);
        addSeat(R.id.seat_D3, "D3", SeatType.STANDARD);
        addSeat(R.id.seat_D4, "D4", SeatType.STANDARD);
        addSeat(R.id.seat_D5, "D5", SeatType.STANDARD);
        addSeat(R.id.seat_D6, "D6", SeatType.STANDARD);
        addSeat(R.id.seat_D7, "D7", SeatType.STANDARD);
        addSeat(R.id.seat_D8, "D8", SeatType.STANDARD);
        addSeat(R.id.seat_D9, "D9", SeatType.STANDARD);
        addSeat(R.id.seat_D10, "D10", SeatType.STANDARD);
    }

    private void addSeat(int buttonId, String code, SeatType type) {
        Button button = findViewById(buttonId);
        if (button == null) return;

        SeatInfo info = new SeatInfo(code, type);
        seats.put(button, info);

        // Set màu ban đầu cho ghế (xanh lá)
        updateSeatVisual(button, info);

        button.setOnClickListener(v -> {
            info.selected = !info.selected;
            updateSeatVisual(button, info);
            updateSummaryAndButtonState();
        });
    }

    private void updateSeatVisual(Button button, SeatInfo info) {
        if (info.selected) {
            // Trạng thái được chọn: nền cam đỏ sáng với border trắng dày, hiển thị dấu tích
            button.setBackgroundResource(R.drawable.bg_seat_selected);
            button.setText(""); // Xóa text số
            // Thêm icon checkmark
            Drawable checkIcon = getResources().getDrawable(R.drawable.ic_check_seat, null);
            if (checkIcon != null) {
                checkIcon.setBounds(0, 0, checkIcon.getIntrinsicWidth(), checkIcon.getIntrinsicHeight());
                button.setCompoundDrawables(checkIcon, null, null, null);
            }
            button.setCompoundDrawablePadding(0);
            // Đảm bảo button được refresh
            button.invalidate();
        } else {
            // Trạng thái chưa chọn: tất cả ghế đều xanh lá, hiển thị số ghế
            button.setBackgroundResource(R.drawable.bg_seat_available);
            // Khôi phục text số từ mã ghế (ví dụ "A1" -> "1", "B10" -> "10")
            String seatNumber = extractSeatNumber(info.code);
            button.setText(seatNumber);
                    button.setTextColor(Color.WHITE);
            // Xóa icon checkmark
            button.setCompoundDrawables(null, null, null, null);
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
        Button btnContinue = findViewById(R.id.btn_continue);

        String seatsText = buildSelectedSeatString();
        int total = calculateTotal();

        if (seatsText.isEmpty()) {
            tvSelectedSeats.setText("Ghế đã chọn: Chưa có");
            tvTotalPrice.setText("Tổng tiền: 0đ");
            btnContinue.setEnabled(false);
            btnContinue.setBackgroundColor(0xFF26313A);
            btnContinue.setTextColor(0xFF6B747F);
        } else {
            tvSelectedSeats.setText("Ghế đã chọn: " + seatsText);
            tvTotalPrice.setText("Tổng tiền: " + formatCurrency(total));
            btnContinue.setEnabled(true);
            btnContinue.setBackgroundColor(0xFF0EB378);
            btnContinue.setTextColor(0xFFFFFFFF);
        }
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

    private int calculateTotal() {
        int total = 0;
        for (SeatInfo info : seats.values()) {
            if (!info.selected) continue;
            switch (info.type) {
                case STANDARD:
                    total += PRICE_STANDARD;
                    break;
                case VIP:
                    total += PRICE_VIP;
                    break;
                case COUPLE:
                    total += PRICE_COUPLE;
                    break;
            }
        }
        return total;
    }

    private String formatTotalAmount() {
        int total = calculateTotal();
        return formatCurrency(total);
    }

    private String formatCurrency(int amount) {
        // Định dạng đơn giản: 110.500đ
        String raw = String.valueOf(amount);
        StringBuilder sb = new StringBuilder(raw);
        int insertPos = sb.length() - 3;
        if (insertPos > 0) {
            sb.insert(insertPos, ".");
        }
        sb.append("đ");
        return sb.toString();
    }
}



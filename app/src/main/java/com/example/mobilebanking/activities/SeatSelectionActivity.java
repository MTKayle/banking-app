package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
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
        addSeat(R.id.seat_A1, "A1", SeatType.STANDARD);
        addSeat(R.id.seat_A2, "A2", SeatType.STANDARD);
        addSeat(R.id.seat_A3, "A3", SeatType.STANDARD);
        addSeat(R.id.seat_A4, "A4", SeatType.STANDARD);
        addSeat(R.id.seat_A5, "A5", SeatType.STANDARD);
        addSeat(R.id.seat_A6, "A6", SeatType.STANDARD);

        addSeat(R.id.seat_H10, "H10", SeatType.VIP);
        addSeat(R.id.seat_H11, "H11", SeatType.VIP);
        addSeat(R.id.seat_H12, "H12", SeatType.VIP);
        addSeat(R.id.seat_H13, "H13", SeatType.VIP);

        addSeat(R.id.seat_K1K2, "K1–K2", SeatType.COUPLE);
        addSeat(R.id.seat_K3K4, "K3–K4", SeatType.COUPLE);
        addSeat(R.id.seat_K5K6, "K5–K6", SeatType.COUPLE);
    }

    private void addSeat(int buttonId, String code, SeatType type) {
        Button button = findViewById(buttonId);
        if (button == null) return;

        SeatInfo info = new SeatInfo(code, type);
        seats.put(button, info);

        button.setOnClickListener(v -> {
            info.selected = !info.selected;
            updateSeatVisual(button, info);
            updateSummaryAndButtonState();
        });
    }

    private void updateSeatVisual(Button button, SeatInfo info) {
        if (info.selected) {
            // Trạng thái được chọn: nền trắng, chữ xanh
            button.setBackgroundColor(Color.WHITE);
            button.setTextColor(0xFF0EB378);
        } else {
            // Trạng thái chưa chọn: theo loại ghế
            switch (info.type) {
                case STANDARD:
                    button.setBackgroundColor(0xFF0EB378);
                    button.setTextColor(Color.WHITE);
                    break;
                case VIP:
                    button.setBackgroundColor(0xFFF2C94C);
                    button.setTextColor(0xFF1A1A1A);
                    break;
                case COUPLE:
                    button.setBackgroundColor(0xFFEB5757);
                    button.setTextColor(Color.WHITE);
                    break;
            }
        }
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



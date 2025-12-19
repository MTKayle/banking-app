package com.example.mobilebanking.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.TicketDetailActivity;
import com.example.mobilebanking.api.dto.MyBookingsResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<MyBookingsResponse.BookingItem> ticketList;
    private Context context;

    public TicketAdapter(Context context, List<MyBookingsResponse.BookingItem> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        MyBookingsResponse.BookingItem ticket = ticketList.get(position);
        holder.bind(ticket);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            intent.putExtra(TicketDetailActivity.EXTRA_BOOKING_ITEM, (Serializable) ticket);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMoviePoster;
        TextView tvMovieTitle, tvCinemaName, tvStatus, tvDateTime, tvTotalAmount, tvSeats, tvBookingCode;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMoviePoster = itemView.findViewById(R.id.iv_movie_poster);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvCinemaName = itemView.findViewById(R.id.tv_cinema_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvBookingCode = itemView.findViewById(R.id.tv_booking_code);
        }

        public void bind(MyBookingsResponse.BookingItem ticket) {
            Glide.with(itemView.getContext())
                    .load(ticket.getPosterUrl())
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(ivMoviePoster);

            tvMovieTitle.setText(ticket.getMovieTitle());
            tvCinemaName.setText(ticket.getCinemaName());
            
            if (ticket.getStatus() != null && ticket.getStatus().equals("CONFIRMED")) {
                tvStatus.setText("Đã xác nhận");
                tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
            } else {
                tvStatus.setText("Đã hủy");
                tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed); // You can create bg_status_cancelled if needed
            }

            // Format date and time
            String dateTime = ticket.getScreeningDate() + " - " + 
                    (ticket.getStartTime() != null && ticket.getStartTime().length() >= 5 
                            ? ticket.getStartTime().substring(0, 5) 
                            : ticket.getStartTime());
            tvDateTime.setText(dateTime);

            // Format total amount
            if (ticket.getTotalAmount() != null) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvTotalAmount.setText(formatter.format(ticket.getTotalAmount()) + " VND");
            }

            // Join seat labels
            if (ticket.getSeats() != null && !ticket.getSeats().isEmpty()) {
                String seatLabels = ticket.getSeats().stream()
                        .map(MyBookingsResponse.SeatInfo::getSeatLabel)
                        .collect(Collectors.joining(", "));
                tvSeats.setText(String.format(Locale.getDefault(), "%d ghế: %s", 
                        ticket.getTotalSeats() != null ? ticket.getTotalSeats() : 0, seatLabels));
            }

            tvBookingCode.setText("Mã: " + ticket.getBookingCode());
        }
    }
}


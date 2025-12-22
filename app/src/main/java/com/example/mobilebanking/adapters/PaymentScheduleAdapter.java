package com.example.mobilebanking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.PaymentScheduleResponse;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying payment schedules in RecyclerView
 */
public class PaymentScheduleAdapter extends RecyclerView.Adapter<PaymentScheduleAdapter.ViewHolder> {

    private List<PaymentScheduleResponse> schedules = new ArrayList<>();
    private Context context;
    private NumberFormat currencyFormat;
    private SimpleDateFormat inputDateFormat;
    private SimpleDateFormat displayDateFormat;

    public PaymentScheduleAdapter(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        this.inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    public void setSchedules(List<PaymentScheduleResponse> schedules) {
        this.schedules = schedules != null ? schedules : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentScheduleResponse schedule = schedules.get(position);
        holder.bind(schedule);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layoutContainer;
        private TextView tvPeriodNumber, tvDueDate, tvTotalPayment;
        private TextView tvPrincipal, tvInterest, tvRemaining;
        private TextView tvStatus, tvPenalty, tvPaidDate;
        private LinearLayout layoutPenalty, layoutPaidDate;
        private ImageView ivCurrentIndicator;
        private View viewCurrentBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContainer = itemView.findViewById(R.id.layout_container);
            tvPeriodNumber = itemView.findViewById(R.id.tv_period_number);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvTotalPayment = itemView.findViewById(R.id.tv_total_payment);
            tvPrincipal = itemView.findViewById(R.id.tv_principal);
            tvInterest = itemView.findViewById(R.id.tv_interest);
            tvRemaining = itemView.findViewById(R.id.tv_remaining);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvPenalty = itemView.findViewById(R.id.tv_penalty);
            tvPaidDate = itemView.findViewById(R.id.tv_paid_date);
            layoutPenalty = itemView.findViewById(R.id.layout_penalty);
            layoutPaidDate = itemView.findViewById(R.id.layout_paid_date);
            ivCurrentIndicator = itemView.findViewById(R.id.iv_current_indicator);
            viewCurrentBadge = itemView.findViewById(R.id.view_current_badge);
        }

        void bind(PaymentScheduleResponse schedule) {
            // Period number
            tvPeriodNumber.setText("Kỳ " + schedule.getPeriodNumber());

            // Due date
            String dueDate = formatDate(schedule.getDueDate());
            tvDueDate.setText("Hạn: " + dueDate);

            // Total payment (gốc + lãi, không bao gồm phạt trong hiển thị chính)
            Double principal = schedule.getPrincipalAmount();
            Double interest = schedule.getInterestAmount();
            double baseTotal = (principal != null ? principal : 0) + (interest != null ? interest : 0);
            tvTotalPayment.setText(currencyFormat.format(baseTotal) + " đ");

            // Principal
            tvPrincipal.setText(principal != null ? currencyFormat.format(principal) + " đ" : "0 đ");

            // Interest
            tvInterest.setText(interest != null ? currencyFormat.format(interest) + " đ" : "0 đ");

            // Remaining balance
            Double remaining = schedule.getRemainingBalance();
            tvRemaining.setText(remaining != null ? currencyFormat.format(remaining) + " đ" : "0 đ");

            // Determine status and styling
            Boolean isPaid = schedule.getIsPaid();
            Boolean isOverdue = schedule.getIsOverdue();
            Boolean isCurrentPeriod = schedule.getIsCurrentPeriod();

            // Reset default styling
            layoutContainer.setBackgroundResource(R.drawable.bg_card_white);
            tvPeriodNumber.setTextColor(ContextCompat.getColor(context, R.color.uihome_text_primary));
            tvTotalPayment.setTextColor(ContextCompat.getColor(context, R.color.primary));

            // Hide penalty and paid date by default
            layoutPenalty.setVisibility(View.GONE);
            layoutPaidDate.setVisibility(View.GONE);
            ivCurrentIndicator.setVisibility(View.GONE);
            viewCurrentBadge.setVisibility(View.GONE);

            if (isPaid != null && isPaid) {
                // Paid status
                tvStatus.setText("ĐÃ THANH TOÁN");
                tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
                tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                layoutContainer.setBackgroundResource(R.drawable.bg_card_paid);

                // Show paid date if available
                if (schedule.getPaidDate() != null && !schedule.getPaidDate().isEmpty()) {
                    layoutPaidDate.setVisibility(View.VISIBLE);
                    tvPaidDate.setText(formatDate(schedule.getPaidDate()));
                }
            } else if (isOverdue != null && isOverdue) {
                // Overdue status - RED
                tvStatus.setText("QUÁ HẠN");
                tvStatus.setBackgroundResource(R.drawable.bg_status_overdue);
                tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                layoutContainer.setBackgroundResource(R.drawable.bg_card_overdue);
                tvPeriodNumber.setTextColor(ContextCompat.getColor(context, R.color.red));
                tvTotalPayment.setTextColor(ContextCompat.getColor(context, R.color.red));

                // Show penalty if available
                Double penalty = schedule.getPenaltyAmount();
                if (penalty != null && penalty > 0) {
                    layoutPenalty.setVisibility(View.VISIBLE);
                    tvPenalty.setText("+" + currencyFormat.format(penalty) + " đ");
                }
            } else if (isCurrentPeriod != null && isCurrentPeriod) {
                // Current period - highlight
                tvStatus.setText("KỲ HIỆN TẠI");
                tvStatus.setBackgroundResource(R.drawable.bg_status_current);
                tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                layoutContainer.setBackgroundResource(R.drawable.bg_card_current);
                ivCurrentIndicator.setVisibility(View.VISIBLE);
                viewCurrentBadge.setVisibility(View.VISIBLE);
            } else {
                // Pending status
                tvStatus.setText("CHỜ THANH TOÁN");
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
            }
        }

        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "N/A";
            try {
                Date date = inputDateFormat.parse(dateStr);
                return displayDateFormat.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
        }
    }
}

package com.example.mobilebanking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.PaymentScheduleDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách lịch thanh toán
 */
public class PaymentScheduleAdapter extends RecyclerView.Adapter<PaymentScheduleAdapter.ViewHolder> {
    
    private Context context;
    private List<PaymentScheduleDTO> schedules;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    
    public PaymentScheduleAdapter(Context context, List<PaymentScheduleDTO> schedules) {
        this.context = context;
        this.schedules = schedules;
        this.currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_schedule, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentScheduleDTO schedule = schedules.get(position);
        
        // Period Number
        holder.tvPeriodNumber.setText("Kỳ " + schedule.getPeriodNumber());
        
        // Due Date
        holder.tvDueDate.setText("Hạn: " + formatDate(schedule.getDueDate()));
        
        // Total Amount
        holder.tvTotalAmount.setText(formatCurrency(schedule.getTotalAmount()));
        
        // Principal, Interest, Remaining Balance
        holder.tvPrincipalAmount.setText(formatCurrency(schedule.getPrincipalAmount()));
        holder.tvInterestAmount.setText(formatCurrency(schedule.getInterestAmount()));
        holder.tvRemainingBalance.setText(formatCurrency(schedule.getRemainingBalance()));
        
        // Status and styling
        String status = schedule.getStatus();
        boolean isOverdue = schedule.getOverdue() != null && schedule.getOverdue();
        boolean isCurrent = schedule.getCurrentPeriod() != null && schedule.getCurrentPeriod();
        
        if ("PAID".equals(status)) {
            // Paid - Green with light green background
            holder.viewStatusIndicator.setBackgroundColor(context.getResources().getColor(R.color.bidv_success));
            holder.tvStatusBadge.setText("ĐÃ TRẢ");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_paid_green);
            holder.tvStatusBadge.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            holder.layoutContainer.setBackgroundColor(context.getResources().getColor(R.color.green_background_light));
        } else if (isOverdue) {
            // Overdue - Red
            holder.viewStatusIndicator.setBackgroundColor(context.getResources().getColor(R.color.red_negative));
            holder.tvStatusBadge.setText("QUÁ HẠN");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_rejected);
            holder.tvStatusBadge.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            holder.layoutContainer.setBackgroundColor(context.getResources().getColor(R.color.bidv_bg_light));
            
            // Show penalty if exists
            if (schedule.getPenaltyAmount() != null && schedule.getPenaltyAmount() > 0) {
                holder.layoutPenaltyWarning.setVisibility(View.VISIBLE);
                holder.tvPenaltyAmount.setText("+" + formatCurrency(schedule.getPenaltyAmount()));
            } else {
                holder.layoutPenaltyWarning.setVisibility(View.GONE);
            }
        } else if (isCurrent) {
            // Current Period - Blue with light blue background
            holder.viewStatusIndicator.setBackgroundColor(context.getResources().getColor(R.color.bidv_primary));
            holder.tvStatusBadge.setText("KỲ HIỆN TẠI");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_current_blue);
            holder.tvStatusBadge.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            holder.layoutContainer.setBackgroundColor(context.getResources().getColor(R.color.blue_background_light));
            holder.layoutPenaltyWarning.setVisibility(View.GONE);
        } else {
            // Pending - Gray
            holder.viewStatusIndicator.setBackgroundColor(context.getResources().getColor(R.color.bidv_text_gray));
            holder.tvStatusBadge.setText("CHỜ THANH TOÁN");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            holder.tvStatusBadge.setTextColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            holder.layoutContainer.setBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.layoutPenaltyWarning.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return schedules.size();
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "0 đ";
        return currencyFormatter.format(amount) + " đ";
    }
    
    private String formatDate(String isoDate) {
        if (isoDate == null) return "";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormatter.format(isoFormat.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        TextView tvPeriodNumber, tvDueDate, tvStatusBadge;
        TextView tvTotalAmount, tvPrincipalAmount, tvInterestAmount, tvRemainingBalance;
        TextView tvPenaltyAmount;
        LinearLayout layoutPenaltyWarning, layoutContainer;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvPeriodNumber = itemView.findViewById(R.id.tv_period_number);
            tvDueDate = itemView.findViewById(R.id.tv_due_date);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvPrincipalAmount = itemView.findViewById(R.id.tv_principal_amount);
            tvInterestAmount = itemView.findViewById(R.id.tv_interest_amount);
            tvRemainingBalance = itemView.findViewById(R.id.tv_remaining_balance);
            tvPenaltyAmount = itemView.findViewById(R.id.tv_penalty_amount);
            layoutPenaltyWarning = itemView.findViewById(R.id.layout_penalty_warning);
            layoutContainer = itemView.findViewById(R.id.layout_container);
        }
    }
}

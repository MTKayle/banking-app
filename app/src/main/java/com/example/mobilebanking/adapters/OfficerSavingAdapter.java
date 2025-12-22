package com.example.mobilebanking.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.SavingModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * OfficerSavingAdapter - Adapter cho danh sách sổ tiết kiệm (Officer view)
 */
public class OfficerSavingAdapter extends RecyclerView.Adapter<OfficerSavingAdapter.SavingViewHolder> {

    private List<SavingModel> savingList;
    private OnSavingClickListener clickListener;

    public interface OnSavingClickListener {
        void onSavingClick(SavingModel saving);
    }

    public OfficerSavingAdapter(OnSavingClickListener clickListener) {
        this.savingList = new ArrayList<>();
        this.clickListener = clickListener;
    }

    public void setSavingList(List<SavingModel> savingList) {
        this.savingList = savingList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_officer_saving, parent, false);
        return new SavingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingViewHolder holder, int position) {
        SavingModel saving = savingList.get(position);
        holder.bind(saving);
    }

    @Override
    public int getItemCount() {
        return savingList.size();
    }

    class SavingViewHolder extends RecyclerView.ViewHolder {
        private View statusBar;
        private TextView tvSavingBookNumber;
        private TextView tvStatus;
        private TextView tvBalance;
        private TextView tvTerm;
        private TextView tvInterestRate;
        private TextView tvOpenedDate;
        private TextView tvMaturityDate;
        private TextView tvCustomerName;

        public SavingViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.status_bar);
            tvSavingBookNumber = itemView.findViewById(R.id.tv_saving_book_number);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            tvTerm = itemView.findViewById(R.id.tv_term);
            tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);
            tvOpenedDate = itemView.findViewById(R.id.tv_opened_date);
            tvMaturityDate = itemView.findViewById(R.id.tv_maturity_date);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onSavingClick(savingList.get(position));
                }
            });
        }

        public void bind(SavingModel saving) {
            // Saving book number
            tvSavingBookNumber.setText(saving.getSavingBookNumber() != null ? 
                    saving.getSavingBookNumber() : "");

            // Balance - format as VND
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            Double balance = saving.getBalance() != null ? saving.getBalance() : 0.0;
            tvBalance.setText(formatter.format(balance) + " VNĐ");

            // Term
            tvTerm.setText(saving.getTerm() != null ? saving.getTerm() : "");

            // Interest rate
            Double interestRate = saving.getInterestRate() != null ? saving.getInterestRate() : 0.0;
            tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%/năm", interestRate));

            // Format dates
            tvOpenedDate.setText(formatDate(saving.getOpenedDate()));
            tvMaturityDate.setText(formatDate(saving.getMaturityDate()));

            // Customer name
            tvCustomerName.setText(saving.getUserFullName() != null ? 
                    saving.getUserFullName() : "");

            // Status badge and bar color
            String status = saving.getStatus();
            tvStatus.setText(saving.getStatusText());

            int statusColor;
            int statusBgRes;
            if ("ACTIVE".equals(status)) {
                statusColor = Color.parseColor("#4CAF50"); // Green
                statusBgRes = R.drawable.bg_rounded_primary;
                tvBalance.setTextColor(Color.parseColor("#4CAF50"));
            } else if ("CLOSED".equals(status)) {
                statusColor = Color.parseColor("#FF9800"); // Orange
                statusBgRes = R.drawable.bg_rounded_orange;
                tvBalance.setTextColor(Color.parseColor("#757575"));
            } else {
                statusColor = Color.GRAY;
                statusBgRes = R.drawable.bg_rounded_light;
                tvBalance.setTextColor(Color.parseColor("#757575"));
            }

            tvStatus.setBackgroundResource(statusBgRes);
            if (statusBar != null) {
                statusBar.setBackgroundColor(statusColor);
            }
        }

        /**
         * Format date from yyyy-MM-dd to dd/MM/yyyy
         */
        private String formatDate(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return "";
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                return date != null ? outputFormat.format(date) : dateStr;
            } catch (ParseException e) {
                return dateStr;
            }
        }
    }
}

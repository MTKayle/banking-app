package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.SavingTermDTO;

import java.util.List;
import java.util.Locale;

public class SavingTermAdapter extends RecyclerView.Adapter<SavingTermAdapter.ViewHolder> {

    private List<SavingTermDTO> termList;
    private OnTermClickListener listener;

    public interface OnTermClickListener {
        void onTermClick(SavingTermDTO term);
    }

    public SavingTermAdapter(List<SavingTermDTO> termList, OnTermClickListener listener) {
        this.termList = termList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saving_term, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingTermDTO term = termList.get(position);
        
        int months = term.getTermMonths();
        holder.tvMonthNumber.setText(String.format(Locale.getDefault(), "%02d", months));
        holder.tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%", term.getInterestRate()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTermClick(term);
            }
        });
    }

    @Override
    public int getItemCount() {
        return termList != null ? termList.size() : 0;
    }

    public void updateData(List<SavingTermDTO> newList) {
        this.termList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthNumber;
        TextView tvInterestRate;

        ViewHolder(View itemView) {
            super(itemView);
            tvMonthNumber = itemView.findViewById(R.id.tv_month_number);
            tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);
        }
    }
}


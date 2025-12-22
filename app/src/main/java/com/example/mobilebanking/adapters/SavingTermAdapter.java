package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.SavingTermDTO;
import com.example.mobilebanking.api.dto.SavingTermResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for Saving Term Interest Rate list
 * Supports both SavingTermResponse and SavingTermDTO
 */
public class SavingTermAdapter extends RecyclerView.Adapter<SavingTermAdapter.TermViewHolder> {

    private List<SavingTermResponse> termList;
    private List<SavingTermDTO> termDTOList;
    private OnTermClickListener clickListener;
    private OnTermDTOClickListener dtoClickListener;
    private boolean useDTOMode = false;

    public interface OnTermClickListener {
        void onTermClick(SavingTermResponse term);
    }

    public interface OnTermDTOClickListener {
        void onTermClick(SavingTermDTO term);
    }

    // Constructor for SavingTermResponse (new)
    public SavingTermAdapter(OnTermClickListener clickListener) {
        this.termList = new ArrayList<>();
        this.clickListener = clickListener;
        this.useDTOMode = false;
    }

    // Constructor for SavingTermDTO (legacy - used by OfficerInterestRateActivity)
    public SavingTermAdapter(List<SavingTermDTO> termDTOList, OnTermDTOClickListener dtoClickListener) {
        this.termDTOList = termDTOList;
        this.dtoClickListener = dtoClickListener;
        this.useDTOMode = true;
    }

    public void setTermList(List<SavingTermResponse> termList) {
        this.termList = termList;
        this.useDTOMode = false;
        notifyDataSetChanged();
    }

    public void setTermDTOList(List<SavingTermDTO> termDTOList) {
        this.termDTOList = termDTOList;
        this.useDTOMode = true;
        notifyDataSetChanged();
    }

    // Alias for setTermDTOList - used by SavingTermListActivity
    public void updateData(List<SavingTermDTO> termDTOList) {
        setTermDTOList(termDTOList);
    }

    @NonNull
    @Override
    public TermViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saving_interest_rate, parent, false);
        return new TermViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TermViewHolder holder, int position) {
        if (useDTOMode && termDTOList != null) {
            SavingTermDTO term = termDTOList.get(position);
            holder.bindDTO(term);
        } else if (termList != null) {
            SavingTermResponse term = termList.get(position);
            holder.bind(term);
        }
    }

    @Override
    public int getItemCount() {
        if (useDTOMode && termDTOList != null) {
            return termDTOList.size();
        } else if (termList != null) {
            return termList.size();
        }
        return 0;
    }

    class TermViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTermName;
        private TextView tvTermType;
        private TextView tvInterestRate;

        public TermViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTermName = itemView.findViewById(R.id.tv_term_name);
            tvTermType = itemView.findViewById(R.id.tv_term_type);
            tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (useDTOMode && dtoClickListener != null && termDTOList != null) {
                        dtoClickListener.onTermClick(termDTOList.get(position));
                    } else if (clickListener != null && termList != null) {
                        clickListener.onTermClick(termList.get(position));
                    }
                }
            });
        }

        public void bind(SavingTermResponse term) {
            tvTermName.setText(term.getDisplayName() != null ? term.getDisplayName() : "");
            tvTermType.setText(term.getTermType() != null ? term.getTermType() : "");
            
            Double rate = term.getInterestRate() != null ? term.getInterestRate() : 0.0;
            tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%", rate));
        }

        public void bindDTO(SavingTermDTO term) {
            tvTermName.setText(term.getDisplayName() != null ? term.getDisplayName() : "");
            tvTermType.setText(term.getTermType() != null ? term.getTermType() : "");
            
            Double rate = term.getInterestRate() != null ? term.getInterestRate() : 0.0;
            tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%", rate));
        }
    }
}

package com.example.mobilebanking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.SavingAccountDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách sổ tiết kiệm
 */
public class SavingAccountAdapter extends RecyclerView.Adapter<SavingAccountAdapter.ViewHolder> {
    
    private Context context;
    private List<SavingAccountDTO> savingAccounts;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    
    public SavingAccountAdapter(Context context, List<SavingAccountDTO> savingAccounts) {
        this.context = context;
        this.savingAccounts = savingAccounts;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saving_account, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingAccountDTO account = savingAccounts.get(position);
        
        holder.tvSavingNumber.setText(account.getSavingAccountNumber());
        holder.tvStatus.setText(account.getStatus());
        holder.tvAmount.setText(currencyFormatter.format(account.getPrincipalAmount()));
        holder.tvTerm.setText(formatTerm(account.getTerm()));
        holder.tvInterestRate.setText(String.format("%.2f%%/năm", account.getInterestRate()));
        
        // Format dates
        try {
            if (account.getStartDate() != null) {
                holder.tvStartDate.setText(formatDate(account.getStartDate()));
            }
            if (account.getMaturityDate() != null) {
                holder.tvMaturityDate.setText(formatDate(account.getMaturityDate()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (account.getEstimatedInterest() != null) {
            holder.tvEstimatedInterest.setText(currencyFormatter.format(account.getEstimatedInterest()));
        }
    }
    
    @Override
    public int getItemCount() {
        return savingAccounts.size();
    }
    
    private String formatTerm(String term) {
        switch (term) {
            case "NON_TERM":
                return "Không kỳ hạn";
            case "ONE_MONTH":
                return "1 tháng";
            case "TWO_MONTHS":
                return "2 tháng";
            case "THREE_MONTHS":
                return "3 tháng";
            case "SIX_MONTHS":
                return "6 tháng";
            case "NINE_MONTHS":
                return "9 tháng";
            case "TWELVE_MONTHS":
                return "12 tháng";
            case "EIGHTEEN_MONTHS":
                return "18 tháng";
            case "TWENTY_FOUR_MONTHS":
                return "24 tháng";
            case "THIRTY_SIX_MONTHS":
                return "36 tháng";
            default:
                return term;
        }
    }
    
    private String formatDate(String isoDate) {
        try {
            // Parse ISO date format (YYYY-MM-DD) and format to dd/MM/yyyy
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormatter.format(isoFormat.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSavingNumber, tvStatus, tvAmount, tvTerm, tvInterestRate;
        TextView tvStartDate, tvMaturityDate, tvEstimatedInterest;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSavingNumber = itemView.findViewById(R.id.tv_saving_number);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTerm = itemView.findViewById(R.id.tv_term);
            tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
            tvMaturityDate = itemView.findViewById(R.id.tv_maturity_date);
            tvEstimatedInterest = itemView.findViewById(R.id.tv_estimated_interest);
        }
    }
}


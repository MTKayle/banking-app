package com.example.mobilebanking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.MortgageAccountDTO;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách khoản vay
 */
public class MortgageAccountAdapter extends RecyclerView.Adapter<MortgageAccountAdapter.ViewHolder> {
    
    private Context context;
    private List<MortgageAccountDTO> mortgageAccounts;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    
    public MortgageAccountAdapter(Context context, List<MortgageAccountDTO> mortgageAccounts) {
        this.context = context;
        this.mortgageAccounts = mortgageAccounts;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mortgage_account, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MortgageAccountDTO account = mortgageAccounts.get(position);
        
        holder.tvMortgageNumber.setText(account.getMortgageAccountNumber());
        holder.tvStatus.setText(account.getStatus());
        holder.tvLoanAmount.setText(currencyFormatter.format(account.getLoanAmount()));
        holder.tvRemainingBalance.setText(currencyFormatter.format(account.getRemainingBalance()));
        holder.tvTerm.setText(String.format("%d tháng", account.getTermMonths()));
        holder.tvInterestRate.setText(String.format("%.2f%%/năm", account.getInterestRate()));
        holder.tvCollateralType.setText(formatCollateralType(account.getCollateralType()));
        
        // Format date
        try {
            if (account.getStartDate() != null) {
                holder.tvStartDate.setText(formatDate(account.getStartDate()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public int getItemCount() {
        return mortgageAccounts.size();
    }
    
    private String formatCollateralType(String type) {
        switch (type) {
            case "NHA":
                return "Nhà";
            case "DAT":
                return "Đất";
            case "XE":
                return "Xe";
            default:
                return type;
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
        TextView tvMortgageNumber, tvStatus, tvLoanAmount, tvRemainingBalance;
        TextView tvTerm, tvInterestRate, tvCollateralType, tvStartDate;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMortgageNumber = itemView.findViewById(R.id.tv_mortgage_number);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvLoanAmount = itemView.findViewById(R.id.tv_loan_amount);
            tvRemainingBalance = itemView.findViewById(R.id.tv_remaining_balance);
            tvTerm = itemView.findViewById(R.id.tv_term);
            tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);
            tvCollateralType = itemView.findViewById(R.id.tv_collateral_type);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
        }
    }
}


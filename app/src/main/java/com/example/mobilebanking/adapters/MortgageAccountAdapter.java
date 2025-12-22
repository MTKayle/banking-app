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
    private OnItemClickListener onItemClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(MortgageAccountDTO mortgage);
    }
    
    public MortgageAccountAdapter(Context context, List<MortgageAccountDTO> mortgageAccounts) {
        this.context = context;
        this.mortgageAccounts = mortgageAccounts;
        this.currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
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
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(account);
            }
        });
        
        // Account Number
        holder.tvAccountNumber.setText(account.getAccountNumber());
        
        // Created Date
        holder.tvCreatedDate.setText(formatDate(account.getCreatedDate()));
        
        // Customer Info
        holder.tvCustomerName.setText(account.getCustomerName());
        holder.tvCustomerPhone.setText(account.getCustomerPhone());
        
        // Principal Amount
        String status = account.getStatus();
        if ("REJECTED".equals(status)) {
            holder.tvPrincipalAmount.setText("Từ chối");
            holder.tvPrincipalAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (account.getPrincipalAmount() != null && account.getPrincipalAmount() > 0) {
            holder.tvPrincipalAmount.setText(formatCurrency(account.getPrincipalAmount()));
            holder.tvPrincipalAmount.setTextColor(context.getResources().getColor(R.color.bidv_primary));
        } else {
            holder.tvPrincipalAmount.setText("Chờ thỏa thuận");
            holder.tvPrincipalAmount.setTextColor(context.getResources().getColor(R.color.bidv_primary));
        }
        
        // Collateral Type
        holder.tvCollateralType.setText(formatCollateralType(account.getCollateralType()));
        
        // Status Badge
        holder.tvStatusBadge.setText(formatStatus(status));
        holder.tvStatusBadge.setBackgroundResource(getStatusBackground(status));
        
        // Rejection Reason (only for REJECTED status)
        if ("REJECTED".equals(status) && account.getRejectionReason() != null) {
            holder.layoutRejectionReason.setVisibility(View.VISIBLE);
            holder.tvRejectionReason.setText(account.getRejectionReason());
        } else {
            holder.layoutRejectionReason.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return mortgageAccounts.size();
    }
    
    private String formatCurrency(Double amount) {
        return currencyFormatter.format(amount) + " đ";
    }
    
    private String formatCollateralType(String type) {
        if (type == null) return "";
        switch (type) {
            case "HOUSE":
                return "Nhà ở";
            case "CAR":
                return "Xe";
            case "LAND":
                return "Đất";
            default:
                return type;
        }
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING_APPRAISAL":
                return "Chờ duyệt";
            case "ACTIVE":
                return "Đang vay";
            case "REJECTED":
                return "Từ chối";
            case "COMPLETED":
                return "Hoàn thành";
            default:
                return status;
        }
    }
    
    private int getStatusBackground(String status) {
        if (status == null) return R.drawable.bg_status_pending;
        switch (status) {
            case "PENDING_APPRAISAL":
                return R.drawable.bg_status_pending;
            case "ACTIVE":
                return R.drawable.bg_status_active;
            case "REJECTED":
                return R.drawable.bg_status_rejected;
            case "COMPLETED":
                return R.drawable.bg_status_completed;
            default:
                return R.drawable.bg_status_pending;
        }
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
    
    public void updateList(List<MortgageAccountDTO> newList) {
        this.mortgageAccounts = newList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountNumber, tvStatusBadge, tvCreatedDate;
        TextView tvCustomerName, tvCustomerPhone;
        TextView tvPrincipalAmount, tvCollateralType;
        TextView tvRejectionReason;
        LinearLayout layoutRejectionReason;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountNumber = itemView.findViewById(R.id.tv_account_number);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvCustomerPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvPrincipalAmount = itemView.findViewById(R.id.tv_principal_amount);
            tvCollateralType = itemView.findViewById(R.id.tv_collateral_type);
            tvRejectionReason = itemView.findViewById(R.id.tv_rejection_reason);
            layoutRejectionReason = itemView.findViewById(R.id.layout_rejection_reason);
        }
    }
}


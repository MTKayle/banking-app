package com.example.mobilebanking.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.MortgageModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * OfficerMortgageAdapter - Adapter cho danh sách khoản vay (Officer view)
 */
public class OfficerMortgageAdapter extends RecyclerView.Adapter<OfficerMortgageAdapter.MortgageViewHolder> {
    
    private List<MortgageModel> mortgageList;
    private OnMortgageClickListener clickListener;
    
    public interface OnMortgageClickListener {
        void onMortgageClick(MortgageModel mortgage);
    }
    
    public OfficerMortgageAdapter(OnMortgageClickListener clickListener) {
        this.mortgageList = new ArrayList<>();
        this.clickListener = clickListener;
    }
    
    public void setMortgageList(List<MortgageModel> mortgageList) {
        this.mortgageList = mortgageList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MortgageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_officer_mortgage, parent, false);
        return new MortgageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MortgageViewHolder holder, int position) {
        MortgageModel mortgage = mortgageList.get(position);
        holder.bind(mortgage);
    }
    
    @Override
    public int getItemCount() {
        return mortgageList.size();
    }
    
    class MortgageViewHolder extends RecyclerView.ViewHolder {
        private View statusBar;
        private TextView tvAccountNumber;
        private TextView tvCreatedDate;
        private TextView tvStatus;
        private TextView tvCustomerName;
        private TextView tvCustomerPhone;
        private TextView tvPrincipalAmount;
        private TextView tvCollateral;
        private LinearLayout layoutRejection;
        private TextView tvRejectionReason;
        
        public MortgageViewHolder(@NonNull View itemView) {
            super(itemView);
            statusBar = itemView.findViewById(R.id.status_bar);
            tvAccountNumber = itemView.findViewById(R.id.tv_account_number);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvCustomerPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvPrincipalAmount = itemView.findViewById(R.id.tv_principal_amount);
            tvCollateral = itemView.findViewById(R.id.tv_collateral);
            layoutRejection = itemView.findViewById(R.id.layout_rejection);
            tvRejectionReason = itemView.findViewById(R.id.tv_rejection_reason);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onMortgageClick(mortgageList.get(position));
                }
            });
        }
        
        public void bind(MortgageModel mortgage) {
            tvAccountNumber.setText(mortgage.getAccountNumber());
            tvCustomerName.setText(mortgage.getCustomerName());
            tvCustomerPhone.setText(mortgage.getCustomerPhone() != null ? mortgage.getCustomerPhone() : "");
            tvCreatedDate.setText(mortgage.getCreatedDate());
            
            // Format amount based on status
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String status = mortgage.getStatus();
            
            if ("REJECTED".equals(status)) {
                // Show "Từ chối" for rejected mortgages
                tvPrincipalAmount.setText("Từ chối");
                tvPrincipalAmount.setTextColor(Color.parseColor("#F44336"));
            } else if ("PENDING_APPRAISAL".equals(status) || mortgage.getPrincipalAmount() == 0) {
                tvPrincipalAmount.setText("Chờ thỏa thuận");
                tvPrincipalAmount.setTextColor(Color.parseColor("#757575"));
            } else {
                String amountText = formatter.format(mortgage.getPrincipalAmount()) + " đ";
                tvPrincipalAmount.setText(amountText);
                tvPrincipalAmount.setTextColor(Color.parseColor("#1976D2"));
            }
            
            // Collateral display name
            String collateralText = getCollateralDisplayName(mortgage.getCollateralType());
            tvCollateral.setText(collateralText);
            
            // Status badge and bar color
            tvStatus.setText(mortgage.getStatusText());
            
            int statusColor;
            int statusBgRes;
            switch (status) {
                case "PENDING_APPRAISAL":
                    statusColor = Color.parseColor("#FF9800");
                    statusBgRes = R.drawable.bg_rounded_orange;
                    break;
                case "APPROVED":
                case "ACTIVE":
                    statusColor = Color.parseColor("#4CAF50");
                    statusBgRes = R.drawable.bg_rounded_primary;
                    break;
                case "COMPLETED":
                    statusColor = Color.parseColor("#2196F3");
                    statusBgRes = R.drawable.bg_rounded_blue;
                    break;
                case "REJECTED":
                    statusColor = Color.parseColor("#F44336");
                    statusBgRes = R.drawable.bg_rounded_red;
                    break;
                default:
                    statusColor = Color.GRAY;
                    statusBgRes = R.drawable.bg_rounded_light;
            }
            
            tvStatus.setBackgroundResource(statusBgRes);
            if (statusBar != null) {
                statusBar.setBackgroundColor(statusColor);
            }
            
            // Show rejection reason if REJECTED
            if ("REJECTED".equals(status) && mortgage.getRejectionReason() != null 
                && !mortgage.getRejectionReason().isEmpty()) {
                if (layoutRejection != null) {
                    layoutRejection.setVisibility(View.VISIBLE);
                    tvRejectionReason.setText(mortgage.getRejectionReason());
                }
            } else {
                if (layoutRejection != null) {
                    layoutRejection.setVisibility(View.GONE);
                }
            }
        }
        
        private String getCollateralDisplayName(String type) {
            if (type == null) return "Không xác định";
            switch (type) {
                case "HOUSE": return "Nhà ở";
                case "LAND": return "Đất";
                case "VEHICLE": return "Xe";
                case "CAR": return "Xe";
                case "OTHER": return "Khác";
                // Legacy values
                case "NHA": return "Nhà ở";
                case "DAT": return "Đất";
                case "XE": return "Xe";
                default: return type;
            }
        }
    }
}

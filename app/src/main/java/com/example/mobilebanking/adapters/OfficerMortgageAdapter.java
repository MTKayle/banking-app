package com.example.mobilebanking.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        private TextView tvAccountNumber;
        private TextView tvStatus;
        private TextView tvCustomerName;
        private TextView tvCustomerPhone;
        private TextView tvPrincipalAmount;
        private TextView tvCreatedDate;
        private TextView tvCollateral;
        
        public MortgageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountNumber = itemView.findViewById(R.id.tv_account_number);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvCustomerPhone = itemView.findViewById(R.id.tv_customer_phone);
            tvPrincipalAmount = itemView.findViewById(R.id.tv_principal_amount);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            tvCollateral = itemView.findViewById(R.id.tv_collateral);
            
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
            
            // Format amount
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            String amountText = formatter.format(mortgage.getPrincipalAmount()) + " đ";
            tvPrincipalAmount.setText(amountText);
            
            // Collateral
            String collateralText = "Tài sản thế chấp: " + mortgage.getCollateralType();
            tvCollateral.setText(collateralText);
            
            // Status badge
            tvStatus.setText(mortgage.getStatusText());
            
            // Status color
            int statusColor = mortgage.getStatusColor();
            tvStatus.setBackgroundColor(statusColor);
            
            // Fallback to drawable if needed
            switch (mortgage.getStatus()) {
                case "PENDING_APPRAISAL":
                    tvStatus.setBackgroundResource(R.drawable.bg_rounded_orange);
                    break;
                case "ACTIVE":
                    tvStatus.setBackgroundResource(R.drawable.bg_rounded_primary);
                    break;
                case "COMPLETED":
                    tvStatus.setBackgroundResource(R.drawable.bg_rounded_blue);
                    break;
                case "REJECTED":
                    tvStatus.setBackgroundResource(R.drawable.bg_rounded_red);
                    break;
                default:
                    tvStatus.setBackgroundColor(Color.GRAY);
            }
        }
    }
}


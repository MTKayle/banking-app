package com.example.mobilebanking.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.BranchDTO;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách chi nhánh ngân hàng
 */
public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {
    
    private List<BranchDTO> branches = new ArrayList<>();
    private OnBranchClickListener listener;
    
    public interface OnBranchClickListener {
        void onBranchClick(BranchDTO branch);
    }
    
    public interface OnDirectionsClickListener extends OnBranchClickListener {
        void onDirectionsClick(BranchDTO branch);
    }
    
    public void setBranches(List<BranchDTO> branches) {
        this.branches = branches != null ? branches : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setOnBranchClickListener(OnBranchClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        BranchDTO branch = branches.get(position);
        holder.bind(branch, position);
    }
    
    @Override
    public int getItemCount() {
        return branches.size();
    }
    
    class BranchViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBranchName;
        private TextView tvBranchAddress;
        private TextView tvBranchDistance;
        private TextView tvNearestBadge;
        private LinearLayout layoutDistance;
        private MaterialButton btnDirections;
        
        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranchName = itemView.findViewById(R.id.tv_branch_name);
            tvBranchAddress = itemView.findViewById(R.id.tv_branch_address);
            tvBranchDistance = itemView.findViewById(R.id.tv_branch_distance);
            tvNearestBadge = itemView.findViewById(R.id.tv_nearest_badge);
            layoutDistance = itemView.findViewById(R.id.layout_distance);
            btnDirections = itemView.findViewById(R.id.btn_directions);
        }
        
        public void bind(BranchDTO branch, int position) {
            tvBranchName.setText(branch.getName());
            tvBranchAddress.setText(branch.getAddress());
            
            // Hiển thị khoảng cách nếu có
            if (branch.getDistance() != null && branch.getDistance() > 0) {
                layoutDistance.setVisibility(View.VISIBLE);
                tvBranchDistance.setText(String.format(Locale.getDefault(), "%.2f km", branch.getDistance()));
                
                // Chỉ chi nhánh đầu tiên (gần nhất) mới có badge
                if (position == 0) {
                    tvNearestBadge.setVisibility(View.VISIBLE);
                } else {
                    tvNearestBadge.setVisibility(View.GONE);
                }
            } else {
                layoutDistance.setVisibility(View.GONE);
                tvNearestBadge.setVisibility(View.GONE);
            }
            
            // Click vào card để focus trên map
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBranchClick(branch);
                }
            });
            
            // Click nút chỉ đường -> vẽ route trên map trong app
            btnDirections.setOnClickListener(v -> {
                if (listener != null) {
                    BranchAdapter.OnDirectionsClickListener directionsListener = 
                        (BranchAdapter.OnDirectionsClickListener) listener;
                    directionsListener.onDirectionsClick(branch);
                }
            });
        }
    }
    
    public List<BranchDTO> getBranches() {
        return branches;
    }
}

package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.UserModel;

import java.util.ArrayList;
import java.util.List;

/**
 * OfficerUserAdapter - Adapter cho danh sách người dùng (Officer view)
 */
public class OfficerUserAdapter extends RecyclerView.Adapter<OfficerUserAdapter.UserViewHolder> {
    
    private List<UserModel> userList;
    private OnUserClickListener clickListener;
    
    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }
    
    public OfficerUserAdapter(OnUserClickListener clickListener) {
        this.userList = new ArrayList<>();
        this.clickListener = clickListener;
    }
    
    public void setUserList(List<UserModel> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_officer_user, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return userList.size();
    }
    
    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFullName;
        private TextView tvPhone;
        private TextView tvEmail;
        private TextView tvRole;
        private TextView tvLockStatus;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
            tvLockStatus = itemView.findViewById(R.id.tv_lock_status);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onUserClick(userList.get(position));
                }
            });
        }
        
        public void bind(UserModel user) {
            tvFullName.setText(user.getFullName());
            tvPhone.setText(user.getPhone());
            tvEmail.setText(user.getEmail());
            
            // Role badge
            if ("officer".equalsIgnoreCase(user.getRole())) {
                tvRole.setText("Nhân viên");
                tvRole.setBackgroundResource(R.drawable.bg_rounded_orange);
            } else {
                tvRole.setText("Khách hàng");
                tvRole.setBackgroundResource(R.drawable.bg_rounded_primary);
            }
            
            // Lock status
            if (user.isLocked()) {
                tvLockStatus.setVisibility(View.VISIBLE);
                tvLockStatus.setText("Đã khóa");
            } else {
                tvLockStatus.setVisibility(View.GONE);
            }
        }
    }
}


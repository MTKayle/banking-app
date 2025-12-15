package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.QuickAction;

import java.util.List;

/**
 * Adapter for displaying quick actions in horizontal RecyclerView
 */
public class QuickActionAdapter extends RecyclerView.Adapter<QuickActionAdapter.QuickActionViewHolder> {
    private List<QuickAction> quickActions;
    private OnQuickActionClickListener listener;

    public interface OnQuickActionClickListener {
        void onQuickActionClick(QuickAction action);
    }

    public QuickActionAdapter(List<QuickAction> quickActions, OnQuickActionClickListener listener) {
        this.quickActions = quickActions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuickActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_action, parent, false);
        return new QuickActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickActionViewHolder holder, int position) {
        QuickAction action = quickActions.get(position);
        holder.bind(action, listener);
    }

    @Override
    public int getItemCount() {
        return quickActions.size();
    }

    static class QuickActionViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvTitle;

        public QuickActionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        public void bind(QuickAction action, OnQuickActionClickListener listener) {
            ivIcon.setImageResource(action.getIconResId());
            tvTitle.setText(action.getTitle());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuickActionClick(action);
                }
            });
        }
    }
}

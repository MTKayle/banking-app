package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;

import java.util.List;

/**
 * Adapter for banner ViewPager2 in home screen
 * Supports 4 banners that can be swiped horizontally
 */
public class HomeBannerAdapter extends RecyclerView.Adapter<HomeBannerAdapter.BannerViewHolder> {
    private List<Integer> bannerImages;
    private OnBannerClickListener clickListener;

    public interface OnBannerClickListener {
        void onBannerClick(int position);
    }

    public HomeBannerAdapter(List<Integer> bannerImages) {
        this.bannerImages = bannerImages;
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner_home, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int imageRes = bannerImages.get(position);
        holder.imageView.setImageResource(imageRes);
        
        // Setup click listener for banner
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBannerClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerImages != null ? bannerImages.size() : 0;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_banner);
        }
    }
}
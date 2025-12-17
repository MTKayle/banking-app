package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;

import java.util.ArrayList;
import java.util.List;

public class WelcomeBannerActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BannerAdapter bannerAdapter;
    private Handler autoSlideHandler;
    private Runnable autoSlideRunnable;
    private int currentPage = 0;
    private LinearLayout indicatorContainer;
    private List<BannerItem> bannerItems; // logical 5 banners

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_banner);

        viewPager = findViewById(R.id.view_pager_banner);
        indicatorContainer = findViewById(R.id.indicator_container);
        ImageView btnBack = findViewById(R.id.btn_back);
        View btnRegister = findViewById(R.id.btn_register_account);

        // Logical list of 5 banners
        bannerItems = new ArrayList<>();
        bannerItems.add(new BannerItem(R.drawable.banner_1));
        bannerItems.add(new BannerItem(R.drawable.banner_2));
        bannerItems.add(new BannerItem(R.drawable.banner_3));
        bannerItems.add(new BannerItem(R.drawable.banner_4));
        bannerItems.add(new BannerItem(R.drawable.banner_5));

        // Create looped list: [last, 1, 2, 3, 4, 5, first]
        List<BannerItem> loopItems = new ArrayList<>();
        loopItems.add(new BannerItem(bannerItems.get(bannerItems.size() - 1).imageResId)); // fake first = last
        loopItems.addAll(bannerItems);                                                    // real 1..5
        loopItems.add(new BannerItem(bannerItems.get(0).imageResId));                     // fake last = first

        bannerAdapter = new BannerAdapter(loopItems);
        viewPager.setAdapter(bannerAdapter);

        // Start at first real banner (index 1)
        currentPage = 1;
        viewPager.setCurrentItem(currentPage, false);
        updateIndicators(0); // logical index 0

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;

                int logicalIndex;
                int lastIndex = bannerAdapter.getItemCount() - 1;
                if (position == 0) {
                    logicalIndex = bannerItems.size() - 1; // fake first -> logical last
                } else if (position == lastIndex) {
                    logicalIndex = 0; // fake last -> logical first
                } else {
                    logicalIndex = position - 1; // shift because of leading fake item
                }
                updateIndicators(logicalIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int lastIndex = bannerAdapter.getItemCount() - 1;
                    if (currentPage == 0) {
                        // Swiped left from first real -> jump to last real
                        viewPager.setCurrentItem(lastIndex - 1, false);
                    } else if (currentPage == lastIndex) {
                        // Swiped right from last real -> jump to first real
                        viewPager.setCurrentItem(1, false);
                    }
                }
            }
        });

        // Khi người dùng chạm/kéo banner: tạm dừng auto slide, thả tay: chạy lại
        viewPager.setOnTouchListener((v, event) -> {
            if (autoSlideHandler == null || autoSlideRunnable == null) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    stopAutoSlide();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    startAutoSlide();
                    break;
            }
            return false; // vẫn cho ViewPager xử lý vuốt
        });

        btnBack.setOnClickListener(v -> onBackPressed());

        btnRegister.setOnClickListener(v -> {
            // Điều hướng sang flow đăng ký (bắt đầu ở fragment_step1_basic_info)
            startActivity(new android.content.Intent(WelcomeBannerActivity.this, MainRegistrationActivity.class));
        });

        setupAutoSlide();
    }

    private void setupAutoSlide() {
        autoSlideHandler = new Handler(Looper.getMainLooper());
        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                int itemCount = bannerAdapter.getItemCount();
                if (itemCount == 0) return;

                int next = viewPager.getCurrentItem() + 1;
                viewPager.setCurrentItem(next, true);

                autoSlideHandler.postDelayed(this, 3000); // 3s
            }
        };
    }

    private void startAutoSlide() {
        if (autoSlideHandler != null && autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
            autoSlideHandler.postDelayed(autoSlideRunnable, 3000); // 3s
        }
    }

    private void stopAutoSlide() {
        if (autoSlideHandler != null && autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
        }
    }

    private void updateIndicators(int position) {
        int childCount = indicatorContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View dot = indicatorContainer.getChildAt(i);
            dot.setBackgroundResource(
                    i == position ? R.drawable.indicator_dot_selected : R.drawable.indicator_dot_unselected
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoSlide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    // Simple data holder
    private static class BannerItem {
        final int imageResId;

        BannerItem(int imageResId) {
            this.imageResId = imageResId;
        }
    }

    // Adapter for ViewPager2
    private static class BannerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

        private final List<BannerItem> items;

        BannerAdapter(List<BannerItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(parent.getContext());
            android.view.View view = inflater.inflate(R.layout.item_welcome_banner, parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class BannerViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            final ImageView imageView;

            BannerViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.iv_banner);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            BannerItem item = items.get(position);
            holder.imageView.setImageResource(item.imageResId);
        }
    }
}



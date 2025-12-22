package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.HomeBannerAdapter;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Officer Dashboard Activity - Main screen for officers
 * Modern design similar to customer home but with officer functions
 */
public class OfficerDashboardActivity extends BaseActivity {
    private TextView tvTotalBalance;
    private ImageView ivToggleBalance, ivAvatar;
    private CardView cvTransfer, cvMyQr, cvMortgage, cvSearch;
    private ViewPager2 viewPagerCarousel;
    private LinearLayout navHome, navQr, navCard, navProfile;
    private DataManager dataManager;
    private HomeBannerAdapter bannerAdapter;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private Handler resumeScrollHandler;
    private Runnable resumeScrollRunnable;
    private int currentBannerPage = 0;
    private List<Integer> originalBannerImages;
    private boolean isBalanceMasked = true;
    private double mockTotalBalance = 50000000.0; // Mock balance for officer
    private static final long AUTO_SCROLL_DELAY_MS = 3000;
    private static final long RESUME_SCROLL_DELAY_MS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_dashboard);

        dataManager = DataManager.getInstance(this);

        initializeViews();
        setupBalanceToggle();
        setupBannerCarousel();
        setupQuickActions();
        setupBottomNavigation();
        setupShoppingActions();
    }

    private void initializeViews() {
        // Header views
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        ivToggleBalance = findViewById(R.id.iv_toggle_balance);
        ivAvatar = findViewById(R.id.iv_avatar);
        
        // Quick Actions
        cvTransfer = findViewById(R.id.officer_action_transfer);
        cvMyQr = findViewById(R.id.officer_action_my_qr);
        cvMortgage = findViewById(R.id.officer_action_mortgage);
        cvSearch = findViewById(R.id.officer_action_search);
        
        // Carousel
        viewPagerCarousel = findViewById(R.id.viewpager_carousel);
        
        // Bottom Navigation
        navHome = findViewById(R.id.officer_nav_home);
        navQr = findViewById(R.id.officer_nav_qr);
        navCard = findViewById(R.id.officer_nav_card);
        navProfile = findViewById(R.id.officer_nav_profile);
        
        // Setup avatar click
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }
        
        // Display masked balance initially
        updateBalanceDisplay();
    }
    
    private void setupBalanceToggle() {
        if (ivToggleBalance != null) {
            ivToggleBalance.setOnClickListener(v -> {
                isBalanceMasked = !isBalanceMasked;
                updateBalanceDisplay();
            });
        }
        
        // Click on balance card to navigate to account details (if needed)
        View balanceCard = findViewById(R.id.balance_card_container);
        if (balanceCard != null) {
            balanceCard.setOnClickListener(v -> {
                // Could navigate to account summary or do nothing
            });
        }
    }
    
    private void updateBalanceDisplay() {
        if (tvTotalBalance == null) return;
        
        if (isBalanceMasked) {
            tvTotalBalance.setText("*** *** VNĐ");
        } else {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String balanceText = formatter.format(mockTotalBalance);
            tvTotalBalance.setText(balanceText);
        }
    }
    
    private void setupBannerCarousel() {
        // Create list of original banner images (4 ảnh trong drawable)
        originalBannerImages = Arrays.asList(
            R.drawable.home_banner_1,
            R.drawable.home_banner_2,
            R.drawable.home_banner_3,
            R.drawable.home_banner_4
        );
        
        // Tạo looped list: [banner4, banner1, banner2, banner3, banner4, banner1]
        List<Integer> loopedBannerImages = new ArrayList<>();
        loopedBannerImages.add(originalBannerImages.get(originalBannerImages.size() - 1));
        loopedBannerImages.addAll(originalBannerImages);
        loopedBannerImages.add(originalBannerImages.get(0));
        
        bannerAdapter = new HomeBannerAdapter(loopedBannerImages);
        viewPagerCarousel.setAdapter(bannerAdapter);
        
        // Setup click listener
        bannerAdapter.setOnBannerClickListener(position -> {
            int logicalPosition = getLogicalBannerPosition(position);
            Toast.makeText(this, "Banner " + (logicalPosition + 1), Toast.LENGTH_SHORT).show();
        });
        
        // Set offscreen page limit
        viewPagerCarousel.setOffscreenPageLimit(1);
        
        // Setup peek effect
        viewPagerCarousel.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewPagerCarousel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                
                androidx.recyclerview.widget.RecyclerView recyclerView = 
                    (androidx.recyclerview.widget.RecyclerView) viewPagerCarousel.getChildAt(0);
                if (recyclerView != null) {
                    float density = getResources().getDisplayMetrics().density;
                    int paddingPx = (int) (60 * density);
                    
                    recyclerView.setPadding(paddingPx, 0, paddingPx, 0);
                    recyclerView.setClipToPadding(false);
                    recyclerView.setClipChildren(false);
                    
                    recyclerView.postDelayed(() -> {
                        currentBannerPage = 1;
                        viewPagerCarousel.setCurrentItem(currentBannerPage, false);
                    }, 50);
                }
            }
        });
        
        // Setup page change callback
        viewPagerCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentBannerPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopAutoScroll();
                    cancelResumeAutoScroll();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int lastIndex = bannerAdapter.getItemCount() - 1;
                    if (currentBannerPage == 0) {
                        viewPagerCarousel.setCurrentItem(lastIndex - 1, false);
                    } else if (currentBannerPage == lastIndex) {
                        viewPagerCarousel.setCurrentItem(1, false);
                    }
                    scheduleResumeAutoScroll();
                }
            }
        });
        
        viewPagerCarousel.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    stopAutoScroll();
                    cancelResumeAutoScroll();
                    break;
                case MotionEvent.ACTION_MOVE:
                    stopAutoScroll();
                    cancelResumeAutoScroll();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    scheduleResumeAutoScroll();
                    break;
            }
            return false;
        });
        
        autoScrollHandler = new Handler(Looper.getMainLooper());
        resumeScrollHandler = new Handler(Looper.getMainLooper());
        startAutoScroll();
    }
    
    private int getLogicalBannerPosition(int physicalPosition) {
        if (physicalPosition == 0) {
            return originalBannerImages.size() - 1;
        } else if (physicalPosition == bannerAdapter.getItemCount() - 1) {
            return 0;
        } else {
            return physicalPosition - 1;
        }
    }
    
    private void startAutoScroll() {
        stopAutoScroll();

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerCarousel == null || bannerAdapter == null) {
                    return;
                }

                int totalItems = bannerAdapter.getItemCount();
                if (totalItems <= 1) {
                    return;
                }

                int nextItem = viewPagerCarousel.getCurrentItem() + 1;
                viewPagerCarousel.setCurrentItem(nextItem, true);

                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
    }
    
    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }
    
    private void scheduleResumeAutoScroll() {
        if (resumeScrollHandler == null) {
            resumeScrollHandler = new Handler(Looper.getMainLooper());
        }
        if (resumeScrollRunnable == null) {
            resumeScrollRunnable = () -> {
                startAutoScroll();
            };
        }
        
        resumeScrollHandler.removeCallbacks(resumeScrollRunnable);
        resumeScrollHandler.postDelayed(resumeScrollRunnable, RESUME_SCROLL_DELAY_MS);
    }
    
    private void cancelResumeAutoScroll() {
        if (resumeScrollHandler != null && resumeScrollRunnable != null) {
            resumeScrollHandler.removeCallbacks(resumeScrollRunnable);
        }
    }

    private void setupQuickActions() {
        // Chuyển tiền
        if (cvTransfer != null) {
            cvTransfer.setOnClickListener(v -> {
                startActivity(new Intent(this, TransferActivity.class));
            });
        }
        
        // QR của tôi
        if (cvMyQr != null) {
            cvMyQr.setOnClickListener(v -> {
                startActivity(new Intent(this, MyQRActivity.class));
            });
        }
        
        // Vay nhanh (Quản lý Vay thế chấp)
        if (cvMortgage != null) {
            cvMortgage.setOnClickListener(v -> {
                startActivity(new Intent(this, OfficerMortgageListActivity.class));
            });
        }
        
        // Tìm Kiếm (Quản lý User)
        if (cvSearch != null) {
            cvSearch.setOnClickListener(v -> {
                startActivity(new Intent(this, OfficerUserListActivity.class));
            });
        }
    }
    
    private void setupShoppingActions() {
        // Setup các actions trong section "Tiện ích - Mua sắm - Giải trí"
        // Dùng lại các actions từ customer home
        setupShoppingAction(R.id.uihome_action_data, MobileTopUpActivity.class);
        setupShoppingAction(R.id.uihome_action_movie_tickets, MovieListActivity.class);
        setupShoppingAction(R.id.uihome_action_taxi, ServicesActivity.class);
        setupShoppingAction(R.id.uihome_action_hotel, HotelBookingActivity.class);
        setupShoppingAction(R.id.uihome_action_locations, BranchLocatorActivity.class);
        setupShoppingAction(R.id.uihome_action_bill, BillPaymentActivity.class);
        setupShoppingAction(R.id.uihome_action_topup, MobileTopUpActivity.class);
        setupShoppingAction(R.id.uihome_action_flight_tickets, TicketBookingActivity.class);
    }
    
    private void setupShoppingAction(int viewId, Class<?> activityClass) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                startActivity(new Intent(this, activityClass));
            });
        }
    }
    
    private void setupBottomNavigation() {
        // Trang chủ - Quay lại Officer Dashboard
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on home, do nothing or scroll to top
            });
        }
        
        // Quét QR
        if (navQr != null) {
            navQr.setOnClickListener(v -> {
                startActivity(new Intent(this, QrScanPaymentActivity.class));
            });
        }
        
        // Thẻ
        if (navCard != null) {
            navCard.setOnClickListener(v -> {
                Toast.makeText(this, "Tính năng Thẻ đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Hồ sơ
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
        cancelResumeAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleResumeAutoScroll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
        cancelResumeAutoScroll();
    }
}

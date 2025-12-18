package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.AccountAdapter;
import com.example.mobilebanking.adapters.CarouselAdapter;
import com.example.mobilebanking.adapters.QuickActionAdapter;
import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.models.QuickAction;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Customer Dashboard Activity - Main screen for customers
 * Modern BIDV-style design
 */
public class CustomerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome, tvUserNameHeader, tvTotalBalance;
    private RecyclerView rvAccounts;
    private CardView cvTransfer, cvBillPay, cvMore, cvSavings, cvTopup, cvLocations, cvQr;
    private ImageView ivNotification, ivAvatar;
    private ViewPager2 viewPagerCarousel;
    private LinearLayout llDotsIndicator;
    private DataManager dataManager;
    private AccountAdapter accountAdapter;
    private CarouselAdapter carouselAdapter;
    private Handler carouselHandler;
    private Runnable carouselRunnable;
    private Handler resumeCarouselHandler; // Handler để delay resume auto-scroll
    private Runnable resumeCarouselRunnable;
    private int currentCarouselPage = 0;
    private List<Integer> originalCarouselImages; // 4 banners gốc
    private static final long AUTO_SCROLL_DELAY_MS = 3000; // 3 giây giữa các banner
    private static final long RESUME_SCROLL_DELAY_MS = 5000; // 5 giây sau khi user thả tay mới tiếp tục auto-scroll

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use modern v2 layout (still kept for compatibility, even if not used as main entry)
        setContentView(R.layout.activity_customer_dashboard_v2);

        dataManager = DataManager.getInstance(this);
        
        initializeViews();
        setupCarousel();
        setupRecyclerViews();
        loadData();
        setupClickListeners();
        setupAnimations();
    }

    private void initializeViews() {
        // Header views
        tvWelcome = findViewById(R.id.tv_welcome_header);
        tvUserNameHeader = findViewById(R.id.tv_user_name_header);
        ivNotification = findViewById(R.id.iv_notification);
        ivAvatar = findViewById(R.id.iv_avatar);
        
        // Balance
        tvTotalBalance = findViewById(R.id.tv_total_balance);
        
        // Accounts
        rvAccounts = findViewById(R.id.rv_accounts);
        
        // Quick Actions
        cvTransfer = findViewById(R.id.cv_transfer);
        cvBillPay = findViewById(R.id.cv_bill_pay);
        cvSavings = findViewById(R.id.cv_savings);
        cvTopup = findViewById(R.id.cv_topup);
        cvLocations = findViewById(R.id.cv_locations);
        cvQr = findViewById(R.id.cv_qr);
        cvMore = findViewById(R.id.cv_more);
        
        // Carousel
        viewPagerCarousel = findViewById(R.id.viewpager_carousel);
        llDotsIndicator = findViewById(R.id.ll_dots_indicator);

        // Set welcome message
        String username = dataManager.getLoggedInUser();
        String fullName = dataManager.getLastFullName();
        if (fullName != null && !fullName.isEmpty()) {
            tvUserNameHeader.setText(fullName);
        } else if (username != null) {
            tvUserNameHeader.setText(username);
        }
    }
    
    private void setupCarousel() {
        // Create list of original carousel images (4 ảnh trong drawable)
        originalCarouselImages = Arrays.asList(
            R.drawable.home_banner_1,
            R.drawable.home_banner_2,
            R.drawable.home_banner_3,
            R.drawable.home_banner_4
        );
        
        // Tạo looped list: [banner4, banner1, banner2, banner3, banner4, banner1]
        // Fake đầu = banner4 (cuối), Fake cuối = banner1 (đầu)
        List<Integer> loopedCarouselImages = new ArrayList<>();
        loopedCarouselImages.add(originalCarouselImages.get(originalCarouselImages.size() - 1)); // fake first = banner4
        loopedCarouselImages.addAll(originalCarouselImages); // real banners: 1, 2, 3, 4
        loopedCarouselImages.add(originalCarouselImages.get(0)); // fake last = banner1
        
        carouselAdapter = new CarouselAdapter(loopedCarouselImages);
        viewPagerCarousel.setAdapter(carouselAdapter);
        
        // Bắt đầu ở banner đầu tiên thật (index 1)
        currentCarouselPage = 1;
        viewPagerCarousel.setCurrentItem(currentCarouselPage, false);
        
        // Setup dots indicator với số lượng banners thật (4)
        setupDotsIndicator(originalCarouselImages.size());
        
        // Update dots when page changes - sử dụng logical position
        viewPagerCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentCarouselPage = position;
                // Cập nhật dots dựa trên logical position
                int logicalPosition = getLogicalCarouselPosition(position);
                updateDotsIndicator(logicalPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                
                // Dừng auto-scroll khi người dùng đang swipe banner
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // Người dùng đang kéo/swipe -> dừng auto-scroll ngay
                    stopAutoScroll();
                    cancelResumeAutoScroll();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // Xử lý loop khi scroll xong
                    int lastIndex = carouselAdapter.getItemCount() - 1;
                    if (currentCarouselPage == 0) {
                        // Swiped left từ banner1 -> jump đến banner4 (last real)
                        viewPagerCarousel.setCurrentItem(lastIndex - 1, false);
                    } else if (currentCarouselPage == lastIndex) {
                        // Swiped right từ banner4 -> jump đến banner1 (first real)
                        viewPagerCarousel.setCurrentItem(1, false);
                    }
                    
                    // Sau khi scroll xong, đợi một khoảng thời gian rồi mới tiếp tục auto-scroll
                    scheduleResumeAutoScroll();
                }
            }
        });
        
        // Setup touch listener để tạm dừng auto-scroll khi user chạm/giữ banner
        viewPagerCarousel.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Người dùng chạm vào banner -> dừng auto-scroll ngay lập tức
                    stopAutoScroll();
                    cancelResumeAutoScroll();
                    break;
                case MotionEvent.ACTION_MOVE:
                    // Người dùng đang giữ/kéo -> tiếp tục dừng
                    stopAutoScroll();
                    cancelResumeAutoScroll();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Người dùng thả tay -> đợi một khoảng thời gian rồi mới tiếp tục auto-scroll
                    // Không restart ngay để người dùng có thời gian đọc nội dung
                    scheduleResumeAutoScroll();
                    break;
            }
            return false; // Vẫn cho ViewPager xử lý swipe
        });
        
        // Auto-scroll carousel với loop vô hạn
        carouselHandler = new Handler(Looper.getMainLooper());
        resumeCarouselHandler = new Handler(Looper.getMainLooper());
        startAutoScroll();
    }
    
    /**
     * Chuyển đổi physical position (có fake items) sang logical position (0-3)
     */
    private int getLogicalCarouselPosition(int physicalPosition) {
        if (physicalPosition == 0) {
            // Fake first -> logical last (banner 4)
            return originalCarouselImages.size() - 1;
        } else if (physicalPosition == carouselAdapter.getItemCount() - 1) {
            // Fake last -> logical first (banner 1)
            return 0;
        } else {
            // Real banners: shift by 1 (vì có fake item ở đầu)
            return physicalPosition - 1;
        }
    }
    
    private void setupDotsIndicator(int count) {
        llDotsIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int size = 8;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_card_rounded);
            if (i == 0) {
                dot.setAlpha(1.0f);
                dot.getBackground().setTint(getResources().getColor(R.color.bidv_blue_primary, null));
            } else {
                dot.setAlpha(0.3f);
                dot.getBackground().setTint(getResources().getColor(R.color.bidv_text_secondary, null));
            }
            llDotsIndicator.addView(dot);
        }
    }
    
    private void updateDotsIndicator(int position) {
        for (int i = 0; i < llDotsIndicator.getChildCount(); i++) {
            View dot = llDotsIndicator.getChildAt(i);
            if (i == position) {
                dot.setAlpha(1.0f);
                dot.getBackground().setTint(getResources().getColor(R.color.bidv_blue_primary, null));
            } else {
                dot.setAlpha(0.3f);
                dot.getBackground().setTint(getResources().getColor(R.color.bidv_text_secondary, null));
            }
        }
    }
    
    private void startAutoScroll() {
        // Xóa runnable cũ (nếu có) để tránh bị nhân đôi animation
        stopAutoScroll();

        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerCarousel == null || carouselAdapter == null) {
                    return;
                }

                int totalItems = carouselAdapter.getItemCount();
                if (totalItems <= 1) {
                    return;
                }

                int nextItem = viewPagerCarousel.getCurrentItem() + 1;
                viewPagerCarousel.setCurrentItem(nextItem, true);

                carouselHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS);
            }
        };
        carouselHandler.postDelayed(carouselRunnable, AUTO_SCROLL_DELAY_MS);
    }
    
    private void stopAutoScroll() {
        if (carouselHandler != null && carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
        }
    }
    
    /**
     * Lên lịch tiếp tục auto-scroll sau một khoảng thời gian
     * Được gọi khi người dùng thả tay khỏi banner
     */
    private void scheduleResumeAutoScroll() {
        if (resumeCarouselHandler == null) {
            resumeCarouselHandler = new Handler(Looper.getMainLooper());
        }
        if (resumeCarouselRunnable == null) {
            resumeCarouselRunnable = () -> {
                // Sau khi đợi một khoảng thời gian, tiếp tục auto-scroll
                startAutoScroll();
            };
        }
        
        // Hủy bỏ callback cũ nếu có
        resumeCarouselHandler.removeCallbacks(resumeCarouselRunnable);
        // Lên lịch resume sau RESUME_SCROLL_DELAY_MS (5 giây)
        resumeCarouselHandler.postDelayed(resumeCarouselRunnable, RESUME_SCROLL_DELAY_MS);
    }
    
    /**
     * Hủy bỏ lịch tiếp tục auto-scroll
     * Được gọi khi người dùng lại chạm vào banner
     */
    private void cancelResumeAutoScroll() {
        if (resumeCarouselHandler != null && resumeCarouselRunnable != null) {
            resumeCarouselHandler.removeCallbacks(resumeCarouselRunnable);
        }
    }

    private void setupRecyclerViews() {
        // Accounts RecyclerView
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        accountAdapter = new AccountAdapter(new ArrayList<>(), this::onAccountClick);
        rvAccounts.setAdapter(accountAdapter);
    }

    private void loadData() {
        // Load user accounts
        List<Account> accounts = dataManager.getMockAccounts("U001");
        accountAdapter.updateAccounts(accounts);

        // Calculate total balance
        double totalBalance = accounts.stream()
                .filter(account -> account.getType() != Account.AccountType.MORTGAGE)
                .mapToDouble(Account::getBalance)
                .sum();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalBalance.setText(formatter.format(totalBalance));
    }

    private void setupClickListeners() {
        // Header actions
        if (ivNotification != null) {
            ivNotification.setOnClickListener(v -> {
                // TODO: Open notifications
            });
        }
        
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }
        
        // Quick Actions
        if (cvTransfer != null) {
        cvTransfer.setOnClickListener(v -> {
                startActivity(new Intent(this, TransferActivity.class));
        });
        }

        if (cvBillPay != null) {
        cvBillPay.setOnClickListener(v -> {
                startActivity(new Intent(this, BillPaymentActivity.class));
            });
        }
        
        if (cvSavings != null) {
            cvSavings.setOnClickListener(v -> {
                // TODO: Open savings
            });
        }
        
        if (cvTopup != null) {
            cvTopup.setOnClickListener(v -> {
                startActivity(new Intent(this, MobileTopUpActivity.class));
            });
        }
        
        if (cvLocations != null) {
            cvLocations.setOnClickListener(v -> {
                startActivity(new Intent(this, BranchLocatorActivity.class));
            });
        }
        
        if (cvQr != null) {
            cvQr.setOnClickListener(v -> {
                startActivity(new Intent(this, QrScannerActivity.class));
            });
        }

        if (cvMore != null) {
        cvMore.setOnClickListener(v -> {
                startActivity(new Intent(this, ServicesActivity.class));
            });
        }
    }
    
    private void setupAnimations() {
        // Fade in animation for header
        if (tvWelcome != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            fadeIn.setDuration(500);
            tvWelcome.startAnimation(fadeIn);
        }
        
        // Slide up animation for cards
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(300);
        if (cvTransfer != null) cvTransfer.startAnimation(slideUp);
    }

    private void onAccountClick(Account account) {
        Intent intent = new Intent(this, AccountDetailActivity.class);
        intent.putExtra("account_id", account.getAccountId());
        intent.putExtra("account_type", account.getType().name());
        startActivity(intent);
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
        // Chỉ start auto-scroll nếu không có tương tác gần đây
        scheduleResumeAutoScroll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
        cancelResumeAutoScroll();
    }
}

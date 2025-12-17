package com.example.mobilebanking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use new modern layout
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
        // Create list of carousel images (4 ảnh trong drawable)
        List<Integer> carouselImages = Arrays.asList(
            R.drawable.home_banner_1,
            R.drawable.home_banner_2,
            R.drawable.home_banner_3,
            R.drawable.home_banner_4
        );
        
        carouselAdapter = new CarouselAdapter(carouselImages);
        viewPagerCarousel.setAdapter(carouselAdapter);
        
        // Setup dots indicator
        setupDotsIndicator(carouselAdapter.getItemCount());
        
        // Auto-scroll carousel (ping-pong: tiến tới cuối rồi lùi lại)
        carouselHandler = new Handler(Looper.getMainLooper());
        startAutoScroll();
        
        // Update dots when page changes
        viewPagerCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDotsIndicator(position);
            }
        });
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

                int currentItem = viewPagerCarousel.getCurrentItem();
                int nextItem = (currentItem + 1) % totalItems; // luôn tiến 1 ảnh, loop vòng tròn

                viewPagerCarousel.setCurrentItem(nextItem, true);
                carouselHandler.postDelayed(this, 3000); // 3s mỗi ảnh
            }
        };
        carouselHandler.postDelayed(carouselRunnable, 3000);
    }
    
    private void stopAutoScroll() {
        if (carouselHandler != null && carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoScroll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
    }
}

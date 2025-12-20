package com.example.mobilebanking.ui_home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.HomeBannerAdapter;
import com.example.mobilebanking.activities.BillPaymentActivity;
import com.example.mobilebanking.activities.BranchLocatorActivity;
import com.example.mobilebanking.activities.HotelBookingActivity;
import com.example.mobilebanking.activities.MobileTopUpActivity;
import com.example.mobilebanking.activities.MovieListActivity;
import com.example.mobilebanking.activities.QrScannerActivity;
import com.example.mobilebanking.activities.ServicesActivity;
import com.example.mobilebanking.activities.TicketBookingActivity;
import com.example.mobilebanking.activities.TransferActivity;
import com.example.mobilebanking.activities.LoginActivity;
import com.example.mobilebanking.activities.ProfileActivity;
import com.example.mobilebanking.activities.SettingsActivity;
import com.example.mobilebanking.utils.DataManager;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.CheckingAccountInfoResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.mobilebanking.models.Account;

/**
 * New Home screen fragment (BIDV-inspired). Frontend-only UI.
 */
public class HomeFragment extends Fragment {
    private TextView tvMaskedBalance;
    private ImageView ivToggleMask;
    private boolean masked = true;
    private ViewPager2 viewPagerBanner;
    private HomeBannerAdapter bannerAdapter;
    private Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private Handler resumeScrollHandler; // Handler để delay resume auto-scroll
    private Runnable resumeScrollRunnable;
    private int currentBannerPage = 0;
    private List<Integer> originalBannerImages; // 4 banners gốc
    private static final long AUTO_SCROLL_DELAY_MS = 3000; // 3 giây giữa các banner
    private static final long RESUME_SCROLL_DELAY_MS = 5000; // 5 giây sau khi user thả tay mới tiếp tục auto-scroll

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ui_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DataManager dm = DataManager.getInstance(requireContext());

        // New compact header views (MB Bank style)
        tvMaskedBalance = view.findViewById(R.id.tv_total_balance);
        ivToggleMask = view.findViewById(R.id.iv_toggle_balance);
        ImageView ivAvatar = view.findViewById(R.id.iv_avatar);
        ImageView ivNotification = view.findViewById(R.id.iv_notification);
        ImageView ivSearch = view.findViewById(R.id.iv_search);
        ImageView ivMenu = view.findViewById(R.id.iv_menu);
        LinearLayout balanceCardContainer = view.findViewById(R.id.balance_card_container);

        // Calculate mock total balance
        List<com.example.mobilebanking.models.Account> accounts = dm.getMockAccounts("U001");
        double total = accounts.stream()
                .filter(a -> a.getType() != com.example.mobilebanking.models.Account.AccountType.MORTGAGE)
                .mapToDouble(com.example.mobilebanking.models.Account::getBalance)
                .sum();
        String balanceText = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(total);
        
        // Setup balance toggle
        if (tvMaskedBalance != null && ivToggleMask != null) {
            applyMask(balanceText);
            ivToggleMask.setOnClickListener(v -> {
                if (masked) {
                    // Đang ẩn, khi click sẽ hiện -> gọi API để lấy số dư
                    masked = false;
                    fetchCheckingAccountBalance();
                } else {
                    // Đang hiện, khi click sẽ ẩn -> chỉ toggle mask
                    masked = true;
                    applyMask(balanceText);
                }
            });
        }
        
        // Balance card click -> AccountActivity
        if (balanceCardContainer != null) {
            balanceCardContainer.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.mobilebanking.activities.AccountActivity.class);
                startActivity(intent);
            });
        }

        // Avatar click -> Settings (combined Profile + Settings)
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), SettingsActivity.class));
            });
        }

        // Top bar icons
        if (ivSearch != null) {
            ivSearch.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Tìm kiếm", Toast.LENGTH_SHORT).show();
            });
        }

        if (ivNotification != null) {
            ivNotification.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Thông báo", Toast.LENGTH_SHORT).show();
            });
        }

        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), SettingsActivity.class));
            });
        }

        // Quick actions
        setupQuickAction(view, R.id.uihome_action_transfer, new Intent(requireContext(), TransferActivity.class));
        setupQuickAction(view, R.id.uihome_action_data, new Intent(requireContext(), MobileTopUpActivity.class));
        setupQuickAction(view, R.id.uihome_action_topup, new Intent(requireContext(), MobileTopUpActivity.class));
        setupQuickAction(view, R.id.uihome_action_bill, new Intent(requireContext(), BillPaymentActivity.class));
        setupQuickAction(view, R.id.uihome_action_saving, new Intent(requireContext(), ServicesActivity.class));
        setupQuickAction(view, R.id.uihome_action_loan, new Intent(requireContext(), ServicesActivity.class));

        // Services shortcuts
        setupQuickAction(view, R.id.uihome_action_locations, new Intent(requireContext(), BranchLocatorActivity.class));
        
        // Tiện ích - Mua sắm - Giải trí
        setupQuickAction(view, R.id.uihome_action_movie_tickets, new Intent(requireContext(), MovieListActivity.class));
        setupQuickAction(view, R.id.uihome_action_flight_tickets, new Intent(requireContext(), TicketBookingActivity.class));
        setupQuickAction(view, R.id.uihome_action_taxi, new Intent(requireContext(), ServicesActivity.class));
        setupQuickAction(view, R.id.uihome_action_hotel, new Intent(requireContext(), HotelBookingActivity.class));

        // Bottom navigation actions
        View navHome = view.findViewById(R.id.uihome_nav_home);
        View navQr = view.findViewById(R.id.uihome_nav_qr);
        View navPromo = view.findViewById(R.id.uihome_nav_promo);
        View navMore = view.findViewById(R.id.uihome_nav_more);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Hiện tại đã ở Home, có thể scroll to top nếu cần
            });
        }
        if (navQr != null) {
            navQr.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), QrScannerActivity.class)));
        }
        if (navPromo != null) {
            navPromo.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), ServicesActivity.class)));
        }
        if (navMore != null) {
            navMore.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SettingsActivity.class)));
        }

        // Setup banner ViewPager2 với 4 banners có thể swipe
        setupBannerViewPager(view);
    }

    private void setupBannerViewPager(View view) {
        viewPagerBanner = view.findViewById(R.id.viewpager_banner);
        
        // Danh sách 4 banner images gốc
        originalBannerImages = Arrays.asList(
            R.drawable.home_banner_1,
            R.drawable.home_banner_2,
            R.drawable.home_banner_3,
            R.drawable.home_banner_4
        );
        
        // Tạo looped list: [banner4, banner1, banner2, banner3, banner4, banner1]
        // Fake đầu = banner4 (cuối), Fake cuối = banner1 (đầu)
        List<Integer> loopedBannerImages = new ArrayList<>();
        loopedBannerImages.add(originalBannerImages.get(originalBannerImages.size() - 1)); // fake first = banner4
        loopedBannerImages.addAll(originalBannerImages); // real banners: 1, 2, 3, 4
        loopedBannerImages.add(originalBannerImages.get(0)); // fake last = banner1
        
        // Tạo adapter và setup ViewPager2
        bannerAdapter = new HomeBannerAdapter(loopedBannerImages);
        viewPagerBanner.setAdapter(bannerAdapter);
        
        // Setup click listener cho banner - trả về logical position
        bannerAdapter.setOnBannerClickListener(position -> {
            // Convert physical position sang logical position
            int logicalPosition = getLogicalPosition(position);
            Toast.makeText(requireContext(), "Banner " + (logicalPosition + 1), Toast.LENGTH_SHORT).show();
        });
        
        // Set offscreen page limit để cải thiện performance
        viewPagerBanner.setOffscreenPageLimit(1);
        
        // Setup peek effect: hiển thị một phần banner bên cạnh
        // Sử dụng ViewTreeObserver để đảm bảo ViewPager2 đã được layout xong
        viewPagerBanner.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Chỉ chạy 1 lần
                viewPagerBanner.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                
                // Tìm RecyclerView bên trong ViewPager2
                androidx.recyclerview.widget.RecyclerView recyclerView = (androidx.recyclerview.widget.RecyclerView) viewPagerBanner.getChildAt(0);
                if (recyclerView != null) {
                    // Convert dp to pixels - 60dp padding để hiển thị một phần banner bên cạnh
                    float density = getResources().getDisplayMetrics().density;
                    int paddingPx = (int) (60 * density);
                    
                    recyclerView.setPadding(paddingPx, 0, paddingPx, 0);
                    recyclerView.setClipToPadding(false);
                    recyclerView.setClipChildren(false);
                    
                    // Đợi RecyclerView layout lại với padding mới, sau đó mới set currentItem
                    // Sử dụng post với delay nhỏ để đảm bảo padding đã được apply
                    recyclerView.postDelayed(() -> {
                        currentBannerPage = 1;
                        viewPagerBanner.setCurrentItem(currentBannerPage, false);
                        // Cập nhật elevation sau khi set current item
                        recyclerView.postDelayed(() -> updateBannerElevations(), 100);
                    }, 50); // Delay 50ms để đảm bảo layout đã hoàn tất
                } else {
                    // Fallback: nếu không tìm thấy RecyclerView, thử lại sau một chút
                    viewPagerBanner.postDelayed(() -> {
                        androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) viewPagerBanner.getChildAt(0);
                        if (rv != null) {
                            float density = getResources().getDisplayMetrics().density;
                            int paddingPx = (int) (60 * density);
                            rv.setPadding(paddingPx, 0, paddingPx, 0);
                            rv.setClipToPadding(false);
                            rv.setClipChildren(false);
                            rv.postDelayed(() -> {
                                currentBannerPage = 1;
                                viewPagerBanner.setCurrentItem(currentBannerPage, false);
                                // Cập nhật elevation sau khi set current item
                                rv.postDelayed(() -> updateBannerElevations(), 100);
                            }, 50);
                        } else {
                            // Nếu vẫn không tìm thấy, set currentItem ngay
                            currentBannerPage = 1;
                            viewPagerBanner.setCurrentItem(currentBannerPage, false);
                            // Cập nhật elevation sau khi set current item
                            viewPagerBanner.postDelayed(() -> updateBannerElevations(), 100);
                        }
                    }, 100);
                }
            }
        });
        
        // Setup PageTransformer để banner chính nổi lên trên hơn so với 2 banner bên cạnh
        viewPagerBanner.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                // Tìm CardView trong page
                CardView cardView = page.findViewById(R.id.cv_banner_card);
                if (cardView == null) {
                    // Nếu không tìm thấy với id, tìm bằng cách duyệt children
                    cardView = findCardView(page);
                }
                
                if (cardView != null) {
                    // position = 0: banner chính (đang hiển thị ở giữa)
                    // position < 0: banner bên trái (đã scroll qua)
                    // position > 0: banner bên phải (sắp scroll đến)
                    float absPosition = Math.abs(position);
                    
                    if (absPosition <= 0.5f) {
                        // Banner chính hoặc gần chính: elevation cao để nổi bật
                        float elevation = 12f - (absPosition * 10f); // Từ 12dp xuống 7dp
                        cardView.setCardElevation(dpToPx(Math.max(elevation, 7f)));
                        // Scale gần như bình thường
                        float scale = 1f - (absPosition * 0.03f);
                        page.setScaleX(scale);
                        page.setScaleY(scale);
                    } else {
                        // Banner bên cạnh: elevation thấp hơn nhiều
                        cardView.setCardElevation(dpToPx(2f));
                        // Scale nhỏ hơn một chút để tạo hiệu ứng depth
                        float scale = 0.92f;
                        page.setScaleX(scale);
                        page.setScaleY(scale);
                    }
                }
            }
        });
        
        // Setup page change callback để xử lý loop
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentBannerPage = position;
                // Cập nhật elevation sau khi chọn page
                updateBannerElevations();
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
                    int lastIndex = bannerAdapter.getItemCount() - 1;
                    if (currentBannerPage == 0) {
                        // Swiped left từ banner1 -> jump đến banner4 (last real)
                        viewPagerBanner.setCurrentItem(lastIndex - 1, false);
                    } else if (currentBannerPage == lastIndex) {
                        // Swiped right từ banner4 -> jump đến banner1 (first real)
                        viewPagerBanner.setCurrentItem(1, false);
                    }
                    
                    // Sau khi scroll xong, đợi một khoảng thời gian rồi mới tiếp tục auto-scroll
                    scheduleResumeAutoScroll();
                }
            }
        });
        
        // Setup touch listener để tạm dừng auto-scroll khi user chạm/giữ banner
        viewPagerBanner.setOnTouchListener((v, event) -> {
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
        
        // Setup auto-scroll
        setupAutoScroll();
    }
    
    /**
     * Tìm CardView trong view hierarchy
     */
    private CardView findCardView(View view) {
        if (view instanceof CardView) {
            return (CardView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                CardView found = findCardView(group.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    /**
     * Convert dp to pixels
     */
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
    
    /**
     * Cập nhật elevation của các banner dựa trên vị trí hiện tại
     */
    private void updateBannerElevations() {
        if (viewPagerBanner == null) return;
        
        androidx.recyclerview.widget.RecyclerView recyclerView = 
            (androidx.recyclerview.widget.RecyclerView) viewPagerBanner.getChildAt(0);
        if (recyclerView == null) return;
        
        // Duyệt qua các view trong RecyclerView
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            CardView cardView = findCardView(child);
            
            if (cardView != null) {
                // Lấy vị trí của view trong adapter
                androidx.recyclerview.widget.RecyclerView.ViewHolder holder = 
                    recyclerView.getChildViewHolder(child);
                if (holder != null) {
                    int position = holder.getAdapterPosition();
                    if (position == currentBannerPage) {
                        // Banner chính: elevation cao
                        cardView.setCardElevation(dpToPx(12f));
                    } else {
                        // Banner bên cạnh: elevation thấp
                        cardView.setCardElevation(dpToPx(2f));
                    }
                }
            }
        }
    }
    
    /**
     * Chuyển đổi physical position (có fake items) sang logical position (0-3)
     */
    private int getLogicalPosition(int physicalPosition) {
        if (physicalPosition == 0) {
            // Fake first -> logical last (banner 4)
            return originalBannerImages.size() - 1;
        } else if (physicalPosition == bannerAdapter.getItemCount() - 1) {
            // Fake last -> logical first (banner 1)
            return 0;
        } else {
            // Real banners: shift by 1 (vì có fake item ở đầu)
            return physicalPosition - 1;
        }
    }
    
    /**
     * Setup auto-scroll mỗi 3 giây
     */
    private void setupAutoScroll() {
        autoScrollHandler = new Handler(Looper.getMainLooper());
        resumeScrollHandler = new Handler(Looper.getMainLooper());
        
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerBanner == null || bannerAdapter == null) {
                    return;
                }

                int itemCount = bannerAdapter.getItemCount();
                if (itemCount == 0) return;

                int next = viewPagerBanner.getCurrentItem() + 1;
                viewPagerBanner.setCurrentItem(next, true);

                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS);
            }
        };
        
        resumeScrollRunnable = new Runnable() {
            @Override
            public void run() {
                // Sau khi đợi một khoảng thời gian, tiếp tục auto-scroll
                startAutoScroll();
            }
        };
        
        startAutoScroll();
    }
    
    /**
     * Bắt đầu auto-scroll
     */
    private void startAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            // Hủy bỏ các callback cũ trước khi bắt đầu mới
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
        }
    }
    
    /**
     * Dừng auto-scroll ngay lập tức
     */
    private void stopAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }
    
    /**
     * Lên lịch tiếp tục auto-scroll sau một khoảng thời gian
     * Được gọi khi người dùng thả tay khỏi banner
     */
    private void scheduleResumeAutoScroll() {
        if (resumeScrollHandler != null && resumeScrollRunnable != null) {
            // Hủy bỏ callback cũ nếu có
            resumeScrollHandler.removeCallbacks(resumeScrollRunnable);
            // Lên lịch resume sau RESUME_SCROLL_DELAY_MS (5 giây)
            resumeScrollHandler.postDelayed(resumeScrollRunnable, RESUME_SCROLL_DELAY_MS);
        }
    }
    
    /**
     * Hủy bỏ lịch tiếp tục auto-scroll
     * Được gọi khi người dùng lại chạm vào banner
     */
    private void cancelResumeAutoScroll() {
        if (resumeScrollHandler != null && resumeScrollRunnable != null) {
            resumeScrollHandler.removeCallbacks(resumeScrollRunnable);
        }
    }

    private void setupQuickAction(View root, int cardId, Intent intent) {
        CardView cv = root.findViewById(cardId);
        if (cv != null) {
            cv.setOnClickListener(v -> startActivity(intent));
        }
    }

    private void applyMask(String balance) {
        if (tvMaskedBalance == null) return;
        
        // Crossfade the balance text for smooth toggle
        tvMaskedBalance.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            String display = masked ? "**** *** VNĐ" : balance;
            tvMaskedBalance.setText(display);
            tvMaskedBalance.animate().alpha(1f).setDuration(120).start();
        }).start();

        // Update eye icon (using default Android icons if custom ones don't exist)
        if (ivToggleMask != null) {
            // Use default Android drawable for eye icon
            // ivToggleMask.setImageResource(masked ? android.R.drawable.ic_lock_lock : android.R.drawable.ic_menu_view);
        }
    }
    
    /**
     * Gọi API để lấy thông tin checking account (số dư, checkingId, accountNumber)
     */
    private void fetchCheckingAccountBalance() {
        DataManager dm = DataManager.getInstance(requireContext());
        Long userId = dm.getUserId();
        
        if (userId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            masked = true;
            // Fallback to mock balance
            List<com.example.mobilebanking.models.Account> accounts = dm.getMockAccounts("U001");
            double total = accounts.stream()
                    .filter(a -> a.getType() != com.example.mobilebanking.models.Account.AccountType.MORTGAGE)
                    .mapToDouble(com.example.mobilebanking.models.Account::getBalance)
                    .sum();
            String balanceText = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(total);
            applyMask(balanceText);
            return;
        }
        
        // Disable eye icon while loading
        if (ivToggleMask != null) {
            ivToggleMask.setEnabled(false);
        }
        
        AccountApiService accountApiService = ApiClient.getAccountApiService();
        Call<CheckingAccountInfoResponse> call = accountApiService.getCheckingAccountInfo(userId);
        
        call.enqueue(new Callback<CheckingAccountInfoResponse>() {
            @Override
            public void onResponse(Call<CheckingAccountInfoResponse> call, Response<CheckingAccountInfoResponse> response) {
                // Re-enable eye icon
                if (ivToggleMask != null) {
                    ivToggleMask.setEnabled(true);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    CheckingAccountInfoResponse accountInfo = response.body();
                    
                    // Lưu checkingId và accountNumber
                    dm.saveCheckingAccountInfo(accountInfo.getCheckingId(), accountInfo.getAccountNumber());
                    
                    // Hiển thị số dư
                    BigDecimal balance = accountInfo.getBalance();
                    if (balance != null) {
                        String balanceText = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(balance.doubleValue());
                        applyMask(balanceText);
                    } else {
                        Toast.makeText(requireContext(), "Không thể lấy số dư", Toast.LENGTH_SHORT).show();
                        masked = true;
                        // Fallback to mock balance
                        List<com.example.mobilebanking.models.Account> accounts = dm.getMockAccounts("U001");
                        double total = accounts.stream()
                                .filter(a -> a.getType() != com.example.mobilebanking.models.Account.AccountType.MORTGAGE)
                                .mapToDouble(com.example.mobilebanking.models.Account::getBalance)
                                .sum();
                        String fallbackBalance = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(total);
                        applyMask(fallbackBalance);
                    }
                } else {
                    // API error
                    String errorMessage = "Không thể lấy thông tin tài khoản";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("message")) {
                                // Try to parse error message if available
                                errorMessage = "Lỗi: " + errorBody;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    masked = true;
                    // Fallback to mock balance
                    List<com.example.mobilebanking.models.Account> accounts = dm.getMockAccounts("U001");
                    double total = accounts.stream()
                            .filter(a -> a.getType() != com.example.mobilebanking.models.Account.AccountType.MORTGAGE)
                            .mapToDouble(com.example.mobilebanking.models.Account::getBalance)
                            .sum();
                    String fallbackBalance = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(total);
                    applyMask(fallbackBalance);
                }
            }
            
            @Override
            public void onFailure(Call<CheckingAccountInfoResponse> call, Throwable t) {
                // Re-enable eye icon
                if (ivToggleMask != null) {
                    ivToggleMask.setEnabled(true);
                }
                
                String errorMessage = "Không thể kết nối đến server";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Failed to connect")) {
                        errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.";
                    } else {
                        errorMessage = "Lỗi: " + t.getMessage();
                    }
                }
                
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                masked = true;
                // Fallback to mock balance
                DataManager dm = DataManager.getInstance(requireContext());
                List<com.example.mobilebanking.models.Account> accounts = dm.getMockAccounts("U001");
                double total = accounts.stream()
                        .filter(a -> a.getType() != com.example.mobilebanking.models.Account.AccountType.MORTGAGE)
                        .mapToDouble(com.example.mobilebanking.models.Account::getBalance)
                        .sum();
                String fallbackBalance = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(total);
                applyMask(fallbackBalance);
                t.printStackTrace();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Chỉ start auto-scroll nếu không có tương tác gần đây
        scheduleResumeAutoScroll();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
        cancelResumeAutoScroll();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
        cancelResumeAutoScroll();
    }

}


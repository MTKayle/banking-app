package com.example.mobilebanking.ui_home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.BillPaymentActivity;
import com.example.mobilebanking.activities.BranchLocatorActivity;
import com.example.mobilebanking.activities.MobileTopUpActivity;
import com.example.mobilebanking.activities.QrScannerActivity;
import com.example.mobilebanking.activities.ServicesActivity;
import com.example.mobilebanking.activities.TransferActivity;
import com.example.mobilebanking.activities.LoginActivity;
import com.example.mobilebanking.adapters.CarouselAdapter;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * New Home screen fragment (BIDV-inspired). Frontend-only UI.
 */
public class HomeFragment extends Fragment {
    private TextView tvUserName, tvMaskedBalance;
    private ImageView ivToggleMask;
    private boolean masked = true;

    // Carousel (promo banners)
    private ViewPager2 viewPagerCarousel;
    private LinearLayout dotsContainer;
    private CarouselAdapter carouselAdapter;
    private Handler carouselHandler;
    private Runnable carouselRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ui_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DataManager dm = DataManager.getInstance(requireContext());

        tvUserName = view.findViewById(R.id.uihome_tv_user_name);
        tvMaskedBalance = view.findViewById(R.id.uihome_tv_balance);
        ivToggleMask = view.findViewById(R.id.uihome_iv_eye);

        String fullName = dm.getLastFullName();
        if (fullName == null || fullName.isEmpty()) fullName = dm.getLoggedInUser();
        if (fullName == null) fullName = getString(R.string.welcome_user);
        tvUserName.setText(fullName);

        // Calculate mock total balance
        double total = dm.getMockAccounts("U001").stream()
                .filter(a -> a.getType() != com.example.mobilebanking.models.Account.AccountType.MORTGAGE)
                .mapToDouble(com.example.mobilebanking.models.Account::getBalance)
                .sum();
        String balanceText = NumberFormat.getCurrencyInstance(new Locale("vi","VN")).format(total);
        applyMask(balanceText);

        ivToggleMask.setOnClickListener(v -> {
            masked = !masked;
            applyMask(balanceText);
        });

        // Logout button: về màn hình Login
        TextView tvLogout = view.findViewById(R.id.uihome_tv_logout);
        if (tvLogout != null) {
            tvLogout.setOnClickListener(v -> {
                dm.logout();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }

        // Quick actions
        setupQuickAction(view, R.id.uihome_action_transfer, new Intent(requireContext(), TransferActivity.class));
        setupQuickAction(view, R.id.uihome_action_qr, new Intent(requireContext(), QrScannerActivity.class));
        setupQuickAction(view, R.id.uihome_action_topup, new Intent(requireContext(), MobileTopUpActivity.class));
        setupQuickAction(view, R.id.uihome_action_bill, new Intent(requireContext(), BillPaymentActivity.class));
        setupQuickAction(view, R.id.uihome_action_card, new Intent(requireContext(), ServicesActivity.class));
        setupQuickAction(view, R.id.uihome_action_saving, new Intent(requireContext(), ServicesActivity.class));
        setupQuickAction(view, R.id.uihome_action_loan, new Intent(requireContext(), ServicesActivity.class));
        setupQuickAction(view, R.id.uihome_action_insurance, new Intent(requireContext(), ServicesActivity.class));

        // Services shortcuts
        setupQuickAction(view, R.id.uihome_action_locations, new Intent(requireContext(), BranchLocatorActivity.class));
        setupQuickAction(view, R.id.uihome_action_more, new Intent(requireContext(), ServicesActivity.class));

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
                    startActivity(new Intent(requireContext(), ServicesActivity.class)));
        }

        // Carousel banners (promo)
        setupCarousel(view);
    }

    private void setupQuickAction(View root, int cardId, Intent intent) {
        CardView cv = root.findViewById(cardId);
        if (cv != null) {
            cv.setOnClickListener(v -> startActivity(intent));
        }
    }

    private void applyMask(String balance) {
        // Crossfade the balance text for smooth toggle
        tvMaskedBalance.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            String display = masked ? "**** *** VND" : balance + " VND";
            tvMaskedBalance.setText(display);
            tvMaskedBalance.animate().alpha(1f).setDuration(120).start();
        }).start();

        // Update eye icon
        ivToggleMask.setImageResource(masked ? R.drawable.ic_lock : R.drawable.ic_eye_open);
    }

    // FAB speed dial removed – replaced by bottom navigation bar

    private void setupCarousel(View root) {
        viewPagerCarousel = root.findViewById(R.id.viewpager_carousel);
        dotsContainer = root.findViewById(R.id.ll_dots_indicator);

        if (viewPagerCarousel == null || dotsContainer == null) {
            return;
        }

        // Use the 4 home banners already in drawable
        List<Integer> images = Arrays.asList(
                R.drawable.home_banner_1,
                R.drawable.home_banner_2,
                R.drawable.home_banner_3,
                R.drawable.home_banner_4
        );

        carouselAdapter = new CarouselAdapter(images);
        viewPagerCarousel.setAdapter(carouselAdapter);

        setupDotsIndicator(images.size());

        carouselHandler = new Handler(Looper.getMainLooper());
        startAutoScroll();

        viewPagerCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDotsIndicator(position);
            }
        });
    }

    private void setupDotsIndicator(int count) {
        if (dotsContainer == null) return;
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_card_rounded);
            dot.setAlpha(i == 0 ? 1f : 0.3f);
            int color = getResources().getColor(i == 0 ? R.color.uihome_primary : R.color.uihome_text_secondary, null);
            dot.getBackground().setTint(color);
            dotsContainer.addView(dot);
        }
    }

    private void updateDotsIndicator(int position) {
        if (dotsContainer == null) return;
        int count = dotsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = dotsContainer.getChildAt(i);
            boolean active = i == position;
            dot.setAlpha(active ? 1f : 0.3f);
            int color = getResources().getColor(active ? R.color.uihome_primary : R.color.uihome_text_secondary, null);
            dot.getBackground().setTint(color);
        }
    }

    private void startAutoScroll() {
        if (viewPagerCarousel == null || carouselAdapter == null) return;
        stopAutoScroll();
        carouselRunnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerCarousel == null || carouselAdapter == null) return;
                int count = carouselAdapter.getItemCount();
                if (count <= 1) return;
                int current = viewPagerCarousel.getCurrentItem();
                int next = (current + 1) % count;
                viewPagerCarousel.setCurrentItem(next, true);
                carouselHandler.postDelayed(this, 4000); // 4s mỗi banner
            }
        };
        carouselHandler.postDelayed(carouselRunnable, 4000);
    }

    private void stopAutoScroll() {
        if (carouselHandler != null && carouselRunnable != null) {
            carouselHandler.removeCallbacks(carouselRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoScroll();
    }
}


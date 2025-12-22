package com.example.mobilebanking.ui_home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.OfficerCustomerTransactionsActivity;
import com.example.mobilebanking.activities.OfficerDepositActivity;
import com.example.mobilebanking.activities.OfficerInterestRateActivity;
import com.example.mobilebanking.activities.OfficerMortgageCreateActivity;
import com.example.mobilebanking.activities.OfficerMortgageInterestRateActivity;
import com.example.mobilebanking.activities.OfficerMortgageListActivity;
import com.example.mobilebanking.activities.OfficerOpenAccountActivity;
import com.example.mobilebanking.activities.OfficerReportsActivity;
import com.example.mobilebanking.activities.OfficerSavingInterestRateActivity;
import com.example.mobilebanking.activities.OfficerSavingListActivity;
import com.example.mobilebanking.activities.OfficerUserListActivity;
import com.example.mobilebanking.activities.OfficerSettingsActivity;
import com.example.mobilebanking.utils.DataManager;

/**
 * Officer Home screen fragment.
 * Displays 12 officer-specific functions in 3 groups:
 * - User Management (4 functions)
 * - Mortgage Management (4 functions)
 * - System Management (4 functions)
 */
public class OfficerHomeFragment extends Fragment {

    private TextView tvOfficerName;
    private ImageView ivAvatar;
    private DataManager dataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.officer_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        dataManager = DataManager.getInstance(requireContext());

        // Setup Header
        setupHeader(view);

        // Setup Officer Actions (12 functions)
        setupOfficerActions(view);

        // Setup Bottom Navigation
        setupBottomNavigation(view);
    }

    /**
     * Setup header with officer name and avatar
     */
    private void setupHeader(View view) {
        tvOfficerName = view.findViewById(R.id.officer_tv_name);
        ivAvatar = view.findViewById(R.id.officer_iv_avatar);
        LinearLayout infoCardContainer = view.findViewById(R.id.officer_info_card_container);

        // Get officer name from DataManager
        String officerName = dataManager.getUserFullName();
        if (officerName == null || officerName.isEmpty()) {
            officerName = dataManager.getLoggedInUser();
        }
        if (officerName == null || officerName.isEmpty()) {
            officerName = "Nhân viên";
        }
        
        if (tvOfficerName != null) {
            tvOfficerName.setText(officerName);
        }

        // Avatar click -> OfficerSettings
        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), OfficerSettingsActivity.class));
            });
        }

        // Info card click -> OfficerSettings
        if (infoCardContainer != null) {
            infoCardContainer.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), OfficerSettingsActivity.class));
            });
        }
    }

    /**
     * Setup all officer action buttons
     */
    private void setupOfficerActions(View view) {
        // User Management - all go to User List except Open Account
        setupAction(view, R.id.officer_action_user_list, OfficerUserListActivity.class);
        setupAction(view, R.id.officer_action_user_search, OfficerUserListActivity.class);
        setupAction(view, R.id.officer_action_user_lock, OfficerUserListActivity.class);
        setupAction(view, R.id.officer_action_open_account, OfficerOpenAccountActivity.class);
        
        // Mortgage Management - updated IDs after merging pending+approve
        setupAction(view, R.id.officer_action_mortgage_create, OfficerMortgageCreateActivity.class);
        setupAction(view, R.id.officer_action_mortgage_pending_approve, OfficerMortgageListActivity.class);
        setupAction(view, R.id.officer_action_mortgage_interest_rate, OfficerMortgageInterestRateActivity.class);
        
        // Saving Management
        setupAction(view, R.id.officer_action_saving_list, OfficerSavingListActivity.class);
        setupAction(view, R.id.officer_action_saving_interest_rate, OfficerSavingInterestRateActivity.class);
        
        // System Management
        setupAction(view, R.id.officer_action_deposit, OfficerDepositActivity.class);
        setupAction(view, R.id.officer_action_interest_rate, OfficerInterestRateActivity.class);
        setupAction(view, R.id.officer_action_transactions, OfficerCustomerTransactionsActivity.class);
        setupAction(view, R.id.officer_action_reports, OfficerReportsActivity.class);
    }

    /**
     * Setup a single action button with click listener to navigate to activity
     */
    private void setupAction(View root, int cardId, Class<?> activityClass) {
        CardView cv = root.findViewById(cardId);
        if (cv != null) {
            cv.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), activityClass))
            );
        }
    }

    /**
     * Setup bottom navigation with 4 tabs
     */
    private void setupBottomNavigation(View view) {
        View navHome = view.findViewById(R.id.officer_nav_home);
        View navUser = view.findViewById(R.id.officer_nav_user);
        View navMortgage = view.findViewById(R.id.officer_nav_mortgage);
        View navProfile = view.findViewById(R.id.officer_nav_profile);

        // Home tab - Already on home, do nothing
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Already on home screen
                Toast.makeText(requireContext(), "Đang ở Trang chủ", Toast.LENGTH_SHORT).show();
            });
        }

        // User Management tab
        if (navUser != null) {
            navUser.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), OfficerUserListActivity.class))
            );
        }

        // Mortgage Management tab
        if (navMortgage != null) {
            navMortgage.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), OfficerMortgageListActivity.class))
            );
        }

        // Profile tab -> OfficerSettingsActivity
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> 
                startActivity(new Intent(requireContext(), OfficerSettingsActivity.class))
            );
        }
    }
}


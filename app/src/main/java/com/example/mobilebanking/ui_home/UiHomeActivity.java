package com.example.mobilebanking.ui_home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.BaseActivity;
import com.example.mobilebanking.models.User;
import com.example.mobilebanking.utils.DataManager;

/**
 * Host activity for the new BIDV-inspired Home screen.
 * Pure UI layer. No networking or API logic here.
 * Supports both CUSTOMER and OFFICER roles with different fragments.
 */
public class UiHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check role from DataManager
        DataManager dataManager = DataManager.getInstance(this);
        User.UserRole role = dataManager.getUserRole();
        
        // Set layout
        setContentView(R.layout.ui_home_activity);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            
            // Chọn fragment dựa trên role
            Fragment fragment;
            if (role == User.UserRole.OFFICER) {
                // OFFICER role - hiển thị OfficerHomeFragment
                fragment = new OfficerHomeFragment();
            } else {
                // CUSTOMER role - hiển thị HomeFragment (user)
                fragment = new HomeFragment();
            }
            
            ft.replace(R.id.ui_home_fragment_container, fragment);
            ft.commitNow();
        }
    }
}



package com.example.mobilebanking.ui_home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.BaseActivity;

/**
 * Host activity for Officer Home screen.
 * Similar to UiHomeActivity but for Officer role.
 */
public class OfficerHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.officer_home_activity);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.officer_home_fragment_container, new OfficerHomeFragment());
            ft.commitNow();
        }
    }
}


package com.example.mobilebanking.ui_home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.BaseActivity;

/**
 * Host activity for the new BIDV-inspired Home screen.
 * Pure UI layer. No networking or API logic here.
 */
public class UiHomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_home_activity);

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.ui_home_fragment_container, new HomeFragment());
            ft.commitNow();
        }
    }
}



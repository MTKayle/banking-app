package com.example.mobilebanking.ui_home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.AccountActivity;
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
        
        // Check if we need to open Account with specific tab
        handleAccountNavigation();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleAccountNavigation();
    }
    
    private void handleAccountNavigation() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("OPEN_ACCOUNT_TAB")) {
            int tabIndex = intent.getIntExtra("OPEN_ACCOUNT_TAB", 0);
            // Remove the extra to prevent reopening on rotation
            intent.removeExtra("OPEN_ACCOUNT_TAB");
            
            // Open AccountActivity with specified tab
            Intent accountIntent = new Intent(this, AccountActivity.class);
            accountIntent.putExtra("TAB_INDEX", tabIndex);
            startActivity(accountIntent);
        }
    }
}

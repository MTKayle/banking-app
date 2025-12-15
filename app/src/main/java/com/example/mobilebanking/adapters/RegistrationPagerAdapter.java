package com.example.mobilebanking.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mobilebanking.fragments.Step1BasicInfoFragment;
import com.example.mobilebanking.fragments.Step2QrScanFragment;
import com.example.mobilebanking.fragments.Step3FrontCardFragment;
import com.example.mobilebanking.fragments.Step4BackCardFragment;
import com.example.mobilebanking.models.RegistrationData;

/**
 * Adapter for Registration ViewPager2
 */
public class RegistrationPagerAdapter extends FragmentStateAdapter {
    private RegistrationData registrationData;
    
    public RegistrationPagerAdapter(@NonNull FragmentActivity fragmentActivity, RegistrationData registrationData) {
        super(fragmentActivity);
        this.registrationData = registrationData;
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return Step1BasicInfoFragment.newInstance(registrationData);
            case 1:
                return Step2QrScanFragment.newInstance(registrationData);
            case 2:
                return Step3FrontCardFragment.newInstance(registrationData);
            case 3:
                return Step4BackCardFragment.newInstance(registrationData);
            default:
                return Step1BasicInfoFragment.newInstance(registrationData);
        }
    }
    
    @Override
    public int getItemCount() {
        return 4;
    }
}


package com.example.mobilebanking.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mobilebanking.fragments.CccdBackCaptureFragment;
import com.example.mobilebanking.fragments.CccdFaceCaptureFragment;
import com.example.mobilebanking.fragments.CccdFrontCaptureFragment;
import com.example.mobilebanking.models.CccdCaptureData;

/**
 * Pager adapter for CCCD capture flow (3 steps)
 */
public class CccdCapturePagerAdapter extends FragmentStateAdapter {
    private CccdCaptureData captureData;
    
    public CccdCapturePagerAdapter(@NonNull FragmentActivity fragmentActivity, CccdCaptureData captureData) {
        super(fragmentActivity);
        this.captureData = captureData;
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return CccdFrontCaptureFragment.newInstance(captureData);
            case 1:
                return CccdBackCaptureFragment.newInstance(captureData);
            case 2:
                return CccdFaceCaptureFragment.newInstance(captureData);
            default:
                return CccdFrontCaptureFragment.newInstance(captureData);
        }
    }
    
    @Override
    public int getItemCount() {
        return 3;
    }
}







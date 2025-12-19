package com.example.mobilebanking.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mobilebanking.api.dto.MyBookingsResponse;
import com.example.mobilebanking.fragments.TicketListFragment;

import java.util.List;

public class TicketPagerAdapter extends FragmentStateAdapter {

    private List<MyBookingsResponse.BookingItem> currentTickets;
    private List<MyBookingsResponse.BookingItem> pastTickets;

    public TicketPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                              List<MyBookingsResponse.BookingItem> currentTickets,
                              List<MyBookingsResponse.BookingItem> pastTickets) {
        super(fragmentActivity);
        this.currentTickets = currentTickets;
        this.pastTickets = pastTickets;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return TicketListFragment.newInstance(currentTickets);
        } else {
            return TicketListFragment.newInstance(pastTickets);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}


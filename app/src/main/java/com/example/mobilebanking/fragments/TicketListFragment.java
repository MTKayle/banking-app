package com.example.mobilebanking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.adapters.TicketAdapter;
import com.example.mobilebanking.api.dto.MyBookingsResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TicketListFragment extends Fragment {

    private static final String ARG_TICKETS = "arg_tickets";
    private List<MyBookingsResponse.BookingItem> tickets;

    public static TicketListFragment newInstance(List<MyBookingsResponse.BookingItem> tickets) {
        TicketListFragment fragment = new TicketListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TICKETS, (Serializable) tickets);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tickets = (List<MyBookingsResponse.BookingItem>) getArguments().getSerializable(ARG_TICKETS);
            if (tickets == null) {
                tickets = new ArrayList<>();
            }
        } else {
            tickets = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket_list, container, false);
        
        RecyclerView recyclerView = view.findViewById(R.id.rv_tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TicketAdapter(getContext(), tickets));
        
        return view;
    }
}


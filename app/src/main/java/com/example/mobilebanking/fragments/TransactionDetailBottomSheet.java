package com.example.mobilebanking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.TransactionDTO;
import com.example.mobilebanking.utils.DataManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Bottom Sheet hiển thị chi tiết giao dịch
 */
public class TransactionDetailBottomSheet extends BottomSheetDialogFragment {
    
    private TransactionDTO transaction;
    
    private TextView tvTransactionDate, tvTransactionCode, tvTransactionType;
    private TextView tvSenderAccount, tvSenderName, tvReceiverAccount, tvReceiverName;
    private TextView tvAmount, tvDescription, tvStatus;
    private Button btnClose;
    
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateTimeFormatter;
    
    public static TransactionDetailBottomSheet newInstance(TransactionDTO transaction) {
        TransactionDetailBottomSheet fragment = new TransactionDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("transaction", transaction);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transaction = (TransactionDTO) getArguments().getSerializable("transaction");
        }
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_transaction_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        
        if (transaction != null) {
            populateData();
        }
    }
    
    private void initViews(View view) {
        tvTransactionDate = view.findViewById(R.id.tv_transaction_date);
        tvTransactionCode = view.findViewById(R.id.tv_transaction_code);
        tvTransactionType = view.findViewById(R.id.tv_transaction_type);
        tvSenderAccount = view.findViewById(R.id.tv_sender_account);
        tvSenderName = view.findViewById(R.id.tv_sender_name);
        tvReceiverAccount = view.findViewById(R.id.tv_receiver_account);
        tvReceiverName = view.findViewById(R.id.tv_receiver_name);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvDescription = view.findViewById(R.id.tv_description);
        tvStatus = view.findViewById(R.id.tv_status);
        btnClose = view.findViewById(R.id.btn_close);
    }
    
    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> dismiss());
    }
    
    private void populateData() {
        // Date
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(transaction.getCreatedAt().replace("Z", ""));
            tvTransactionDate.setText(dateTimeFormatter.format(date));
        } catch (Exception e) {
            tvTransactionDate.setText(transaction.getCreatedAt());
        }
        
        // Code
        tvTransactionCode.setText(transaction.getCode());
        
        // Type
        tvTransactionType.setText(formatTransactionType(transaction.getTransactionType()));
        
        // Sender
        tvSenderAccount.setText(transaction.getSenderAccountNumber());
        tvSenderName.setText(transaction.getSenderAccountName());
        
        // Receiver
        tvReceiverAccount.setText(transaction.getReceiverAccountNumber());
        tvReceiverName.setText(transaction.getReceiverAccountName());
        
        // Amount
        DataManager dm = DataManager.getInstance(requireContext());
        String myAccountNumber = dm.getAccountNumber();
        boolean isIncoming = myAccountNumber != null && 
                            myAccountNumber.equals(transaction.getReceiverAccountNumber());
        
        String amountStr = currencyFormatter.format(transaction.getAmount());
        if (isIncoming) {
            tvAmount.setText("+" + amountStr);
            tvAmount.setTextColor(getResources().getColor(R.color.green_positive));
        } else {
            tvAmount.setText("-" + amountStr);
            tvAmount.setTextColor(getResources().getColor(R.color.red_negative));
        }
        
        // Description
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            tvDescription.setText(transaction.getDescription());
        } else {
            tvDescription.setText("Không có nội dung");
        }
        
        // Status
        tvStatus.setText(formatStatus(transaction.getStatus()));
        if ("SUCCESS".equals(transaction.getStatus())) {
            tvStatus.setTextColor(getResources().getColor(R.color.green_positive));
        } else {
            tvStatus.setTextColor(getResources().getColor(R.color.red_negative));
        }
    }
    
    private String formatTransactionType(String type) {
        switch (type) {
            case "TRANSFER":
                return "Chuyển khoản";
            case "DEPOSIT":
                return "Nạp tiền";
            case "WITHDRAW":
                return "Rút tiền / Thanh toán";
            default:
                return type;
        }
    }
    
    private String formatStatus(String status) {
        switch (status) {
            case "SUCCESS":
                return "Thành công";
            case "PENDING":
                return "Đang xử lý";
            case "FAILED":
                return "Thất bại";
            default:
                return status;
        }
    }
}


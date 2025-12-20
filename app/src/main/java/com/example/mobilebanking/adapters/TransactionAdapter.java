package com.example.mobilebanking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.api.dto.TransactionDTO;
import com.example.mobilebanking.utils.DataManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho danh sách giao dịch với date header
 */
public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_TRANSACTION = 1;
    
    private Context context;
    private List<TransactionDTO> transactions;
    private OnTransactionClickListener listener;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;
    private SimpleDateFormat dateHeaderFormatter;
    
    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionDTO transaction);
    }
    
    public TransactionAdapter(Context context, List<TransactionDTO> transactions, 
                             OnTransactionClickListener listener) {
        this.context = context;
        this.transactions = transactions;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        this.dateHeaderFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        // Simple implementation without grouping for now
        return TYPE_TRANSACTION;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(context).inflate(
                    R.layout.item_transaction_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(
                    R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TransactionViewHolder) {
            TransactionDTO transaction = transactions.get(position);
            ((TransactionViewHolder) holder).bind(transaction);
        }
    }
    
    @Override
    public int getItemCount() {
        return transactions.size();
    }
    
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCode, tvAmount, tvTime;
        
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCode = itemView.findViewById(R.id.tv_code);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvTime = itemView.findViewById(R.id.tv_time);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionClick(transactions.get(position));
                }
            });
        }
        
        public void bind(TransactionDTO transaction) {
            // Description
            if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                tvDescription.setText(transaction.getDescription());
            } else {
                tvDescription.setText(formatTransactionType(transaction.getTransactionType()));
            }
            
            // Code
            tvCode.setText("Mã GD: " + transaction.getCode());
            
            // Amount (+ or - based on IN/OUT)
            DataManager dm = DataManager.getInstance(context);
            String myAccountNumber = dm.getAccountNumber();
            boolean isIncoming = myAccountNumber != null && 
                                myAccountNumber.equals(transaction.getReceiverAccountNumber());
            
            String amountStr;
            if (isIncoming) {
                amountStr = "+" + currencyFormatter.format(transaction.getAmount());
                tvAmount.setTextColor(context.getResources().getColor(R.color.green_positive));
            } else {
                amountStr = "-" + currencyFormatter.format(transaction.getAmount());
                tvAmount.setTextColor(context.getResources().getColor(R.color.red_negative));
            }
            tvAmount.setText(amountStr);
            
            // Time
            try {
                String time = formatTime(transaction.getCreatedAt());
                tvTime.setText(time);
            } catch (Exception e) {
                tvTime.setText("");
            }
        }
    }
    
    class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;
        
        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tv_date_header);
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
    
    private String formatTime(String isoDateTime) {
        try {
            // Parse ISO8601 format: 2024-12-19T10:30:00Z
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(isoDateTime.replace("Z", ""));
            return timeFormatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}

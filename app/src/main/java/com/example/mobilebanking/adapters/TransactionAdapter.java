package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying transactions in RecyclerView
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvType, tvDescription, tvAmount, tvDate, tvStatus;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_transaction_type);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        public void bind(Transaction transaction) {
            tvType.setText(getTransactionTypeName(transaction.getType()));
            tvDescription.setText(transaction.getDescription());
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvAmount.setText(formatter.format(transaction.getAmount()));
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDate.setText(dateFormat.format(transaction.getTransactionDate()));
            
            tvStatus.setText(transaction.getStatus().name());
            
            // Set color based on transaction type
            if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
                tvAmount.setTextColor(itemView.getContext().getColor(R.color.success_color));
            } else {
                tvAmount.setTextColor(itemView.getContext().getColor(R.color.error_color));
            }
        }

        private String getTransactionTypeName(Transaction.TransactionType type) {
            switch (type) {
                case TRANSFER:
                    return "Transfer";
                case DEPOSIT:
                    return "Deposit";
                case WITHDRAWAL:
                    return "Withdrawal";
                case BILL_PAYMENT:
                    return "Bill Payment";
                case MOBILE_TOPUP:
                    return "Mobile Top-up";
                case TICKET_BOOKING:
                    return "Ticket Booking";
                case HOTEL_BOOKING:
                    return "Hotel Booking";
                case ECOMMERCE_PAYMENT:
                    return "E-commerce";
                default:
                    return "Transaction";
            }
        }
    }
}


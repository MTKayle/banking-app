package com.example.mobilebanking.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.models.Account;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying accounts in RecyclerView
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accounts;
    private OnAccountClickListener listener;

    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public AccountAdapter(List<Account> accounts, OnAccountClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.bind(account, listener);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public void updateAccounts(List<Account> newAccounts) {
        this.accounts = newAccounts;
        notifyDataSetChanged();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAccountType, tvAccountNumber, tvBalance;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountType = itemView.findViewById(R.id.tv_account_type);
            tvAccountNumber = itemView.findViewById(R.id.tv_account_number);
            tvBalance = itemView.findViewById(R.id.tv_balance);
        }

        public void bind(Account account, OnAccountClickListener listener) {
            tvAccountType.setText(getAccountTypeName(account.getType()));
            tvAccountNumber.setText(formatAccountNumber(account.getAccountNumber()));
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvBalance.setText(formatter.format(Math.abs(account.getBalance())));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccountClick(account);
                }
            });
        }

        private String getAccountTypeName(Account.AccountType type) {
            switch (type) {
                case CHECKING:
                    return "Checking Account";
                case SAVINGS:
                    return "Savings Account";
                case MORTGAGE:
                    return "Mortgage Account";
                default:
                    return "Account";
            }
        }

        private String formatAccountNumber(String accountNumber) {
            if (accountNumber.length() >= 10) {
                return "**** **** " + accountNumber.substring(accountNumber.length() - 4);
            }
            return accountNumber;
        }
    }
}


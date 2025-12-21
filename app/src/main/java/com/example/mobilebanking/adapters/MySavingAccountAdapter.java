package com.example.mobilebanking.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;
import com.example.mobilebanking.activities.SavingWithdrawConfirmActivity;
import com.example.mobilebanking.api.AccountApiService;
import com.example.mobilebanking.api.ApiClient;
import com.example.mobilebanking.api.dto.MySavingAccountDTO;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adapter cho danh sách sổ tiết kiệm từ API /api/saving/my-accounts
 */
public class MySavingAccountAdapter extends RecyclerView.Adapter<MySavingAccountAdapter.ViewHolder> {
    
    private Context context;
    private List<MySavingAccountDTO> savingAccounts;
    private DecimalFormat numberFormatter;
    private SimpleDateFormat dateFormatter;
    
    public MySavingAccountAdapter(Context context, List<MySavingAccountDTO> savingAccounts) {
        this.context = context;
        this.savingAccounts = savingAccounts;
        this.numberFormatter = new DecimalFormat("#,###");
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_saving_account, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MySavingAccountDTO account = savingAccounts.get(position);
        
        // Số sổ tiết kiệm
        holder.tvSavingBookNumber.setText(account.getSavingBookNumber());
        
        // Số dư
        holder.tvBalance.setText(formatCurrency(account.getBalance()) + " VNĐ");
        
        // Kỳ hạn
        holder.tvTerm.setText(account.getTerm());
        
        // Lãi suất
        holder.tvInterestRate.setText(String.format(Locale.getDefault(), "%.1f%%/năm", account.getInterestRate()));
        
        // Trạng thái
        holder.tvStatus.setText(formatStatus(account.getStatus()));
        
        // Ngày mở
        holder.tvOpenedDate.setText("Ngày mở: " + formatDate(account.getOpenedDate()));
        
        // Ngày đáo hạn
        holder.tvMaturityDate.setText("Đáo hạn: " + formatDate(account.getMaturityDate()));
        
        // Thông tin chi tiết - hiển thị 0 nếu null
        Double estimatedInterest = account.getEstimatedInterestAtMaturity();
        holder.tvEstimatedInterest.setText(formatCurrency(estimatedInterest != null ? estimatedInterest : 0.0) + " VNĐ");
        
        Double totalAtMaturity = account.getEstimatedTotalAtMaturity();
        holder.tvTotalAtMaturity.setText(formatCurrency(totalAtMaturity != null ? totalAtMaturity : 0.0) + " VNĐ");
        
        Integer daysUntil = account.getDaysUntilMaturity();
        holder.tvDaysUntilMaturity.setText("Còn " + (daysUntil != null ? daysUntil : 0) + " ngày đến ngày đáo hạn");
        
        // Ẩn nút rút tiền nếu tài khoản đã đóng
        if ("CLOSED".equals(account.getStatus())) {
            holder.btnWithdraw.setVisibility(View.GONE);
        } else {
            holder.btnWithdraw.setVisibility(View.VISIBLE);
        }
        
        // Click vào item để load chi tiết
        holder.itemView.setOnClickListener(v -> loadDetailAndUpdate(account, holder));
        
        // Click nút rút tiền
        holder.btnWithdraw.setOnClickListener(v -> {
            Intent intent = new Intent(context, SavingWithdrawConfirmActivity.class);
            intent.putExtra("savingBookNumber", account.getSavingBookNumber());
            context.startActivity(intent);
        });
    }
    
    @Override
    public int getItemCount() {
        return savingAccounts.size();
    }
    
    /**
     * Load chi tiết tài khoản từ API và cập nhật UI
     */
    private void loadDetailAndUpdate(MySavingAccountDTO account, ViewHolder holder) {
        AccountApiService service = ApiClient.getAccountApiService();
        service.getSavingDetail(account.getSavingBookNumber()).enqueue(new Callback<MySavingAccountDTO>() {
            @Override
            public void onResponse(Call<MySavingAccountDTO> call, Response<MySavingAccountDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MySavingAccountDTO detail = response.body();
                    
                    // Cập nhật thông tin chi tiết - hiển thị 0 nếu null
                    Double estimatedInterest = detail.getEstimatedInterestAtMaturity();
                    holder.tvEstimatedInterest.setText(formatCurrency(estimatedInterest != null ? estimatedInterest : 0.0) + " VNĐ");
                    
                    Double totalAtMaturity = detail.getEstimatedTotalAtMaturity();
                    holder.tvTotalAtMaturity.setText(formatCurrency(totalAtMaturity != null ? totalAtMaturity : 0.0) + " VNĐ");
                    
                    Integer daysUntil = detail.getDaysUntilMaturity();
                    holder.tvDaysUntilMaturity.setText("Còn " + (daysUntil != null ? daysUntil : 0) + " ngày đến ngày đáo hạn");
                    
                    // Cập nhật object trong list
                    account.setEstimatedInterestAtMaturity(detail.getEstimatedInterestAtMaturity());
                    account.setEstimatedTotalAtMaturity(detail.getEstimatedTotalAtMaturity());
                    account.setDaysUntilMaturity(detail.getDaysUntilMaturity());
                    account.setTotalDaysOfTerm(detail.getTotalDaysOfTerm());
                    account.setStatus(detail.getStatus());
                    account.setBalance(detail.getBalance());
                    
                    // Cập nhật status và ẩn nút rút nếu đã đóng
                    holder.tvStatus.setText(formatStatus(detail.getStatus()));
                    if ("CLOSED".equals(detail.getStatus())) {
                        holder.btnWithdraw.setVisibility(View.GONE);
                    } else {
                        holder.btnWithdraw.setVisibility(View.VISIBLE);
                    }
                    
                    // Cập nhật số dư
                    holder.tvBalance.setText(formatCurrency(detail.getBalance()) + " VNĐ");
                }
            }
            
            @Override
            public void onFailure(Call<MySavingAccountDTO> call, Throwable t) {
                // Không hiển thị lỗi, chỉ log
                android.util.Log.e("MySavingAdapter", "Failed to load detail: " + t.getMessage());
            }
        });
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "0";
        return numberFormatter.format(amount);
    }
    
    private String formatStatus(String status) {
        if ("ACTIVE".equals(status)) {
            return "Đang hoạt động";
        } else if ("CLOSED".equals(status)) {
            return "Đã đóng";
        }
        return status;
    }
    
    private String formatDate(String isoDate) {
        if (isoDate == null) return "";
        try {
            // Nếu đã là format dd/MM/yyyy thì return luôn
            if (isoDate.contains("/")) {
                return isoDate;
            }
            // Parse ISO date format (YYYY-MM-DD) and format to dd/MM/yyyy
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return dateFormatter.format(isoFormat.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSavingBookNumber, tvBalance, tvTerm, tvInterestRate;
        TextView tvStatus, tvOpenedDate, tvMaturityDate;
        TextView tvEstimatedInterest, tvTotalAtMaturity, tvDaysUntilMaturity;
        Button btnWithdraw;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSavingBookNumber = itemView.findViewById(R.id.tv_saving_book_number);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            tvTerm = itemView.findViewById(R.id.tv_term);
            tvInterestRate = itemView.findViewById(R.id.tv_interest_rate);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvOpenedDate = itemView.findViewById(R.id.tv_opened_date);
            tvMaturityDate = itemView.findViewById(R.id.tv_maturity_date);
            tvEstimatedInterest = itemView.findViewById(R.id.tv_estimated_interest);
            tvTotalAtMaturity = itemView.findViewById(R.id.tv_total_at_maturity);
            tvDaysUntilMaturity = itemView.findViewById(R.id.tv_days_until_maturity);
            btnWithdraw = itemView.findViewById(R.id.btn_withdraw);
        }
    }
}

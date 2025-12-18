package com.example.mobilebanking.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilebanking.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Bill History Activity - hiển thị danh sách hóa đơn đã thanh toán (mock)
 * Frontend-only, không gọi API thật.
 */
public class BillHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BillHistoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_history_dark);

        setupHeader();
        setupList();
    }

    private void setupHeader() {
        ImageButton btnBack = findViewById(R.id.btn_history_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupList() {
        recyclerView = findViewById(R.id.rv_bill_history);
        if (recyclerView == null) return;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<BillHistoryItem> items = new ArrayList<>();
        // Mock data – bạn có thể chỉnh sửa cho phù hợp
        items.add(new BillHistoryItem(
                BillHistoryItem.TYPE_ELECTRICITY,
                "EVN Hà Nội",
                "KH123456789",
                "11/2024",
                "1.800.000 ₫",
                "20/11/2024 10:15",
                "Đã thanh toán"
        ));
        items.add(new BillHistoryItem(
                BillHistoryItem.TYPE_WATER,
                "Công ty Cấp nước Hà Nội",
                "KH987654321",
                "11/2024",
                "650.000 ₫",
                "18/11/2024 08:45",
                "Đã thanh toán"
        ));
        items.add(new BillHistoryItem(
                BillHistoryItem.TYPE_ELECTRICITY,
                "EVN Hà Nội",
                "KH111222333",
                "10/2024",
                "2.050.000 ₫",
                "21/10/2024 09:20",
                "Đã thanh toán"
        ));

        adapter = new BillHistoryAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    // ====== Model ======
    private static class BillHistoryItem {
        static final int TYPE_ELECTRICITY = 1;
        static final int TYPE_WATER = 2;

        final int type;
        final String providerName;
        final String customerCode;
        final String period;
        final String amount;
        final String paidDateTime;
        final String status;

        BillHistoryItem(int type,
                        String providerName,
                        String customerCode,
                        String period,
                        String amount,
                        String paidDateTime,
                        String status) {
            this.type = type;
            this.providerName = providerName;
            this.customerCode = customerCode;
            this.period = period;
            this.amount = amount;
            this.paidDateTime = paidDateTime;
            this.status = status;
        }
    }

    // ====== Adapter ======
    private static class BillHistoryAdapter extends RecyclerView.Adapter<BillHistoryViewHolder> {

        private final List<BillHistoryItem> items;

        BillHistoryAdapter(List<BillHistoryItem> items) {
            this.items = items;
        }

        @Override
        public BillHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bill_history, parent, false);
            return new BillHistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(BillHistoryViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }
    }

    // ====== ViewHolder ======
    private static class BillHistoryViewHolder extends RecyclerView.ViewHolder {

        private final View iconType;
        private final android.widget.TextView tvProvider;
        private final android.widget.TextView tvCustomerCode;
        private final android.widget.TextView tvPeriod;
        private final android.widget.TextView tvAmount;
        private final android.widget.TextView tvStatus;
        private final android.widget.TextView tvPaidDate;

        BillHistoryViewHolder(View itemView) {
            super(itemView);
            iconType = itemView.findViewById(R.id.view_bill_icon);
            tvProvider = itemView.findViewById(R.id.tv_bill_provider);
            tvCustomerCode = itemView.findViewById(R.id.tv_bill_customer_code);
            tvPeriod = itemView.findViewById(R.id.tv_bill_period);
            tvAmount = itemView.findViewById(R.id.tv_bill_amount);
            tvStatus = itemView.findViewById(R.id.tv_bill_status);
            tvPaidDate = itemView.findViewById(R.id.tv_bill_paid_date);
        }

        void bind(BillHistoryItem item) {
            tvProvider.setText(item.providerName);
            tvCustomerCode.setText("Mã KH: " + item.customerCode);
            tvPeriod.setText("Kỳ: " + item.period);
            tvAmount.setText(item.amount);
            tvStatus.setText(item.status);
            tvPaidDate.setText(item.paidDateTime);

            int bgRes = item.type == BillHistoryItem.TYPE_ELECTRICITY
                    ? R.drawable.bg_bill_type_pill
                    : R.drawable.bg_bill_type_pill;
            iconType.setBackgroundResource(bgRes);
        }
    }
}



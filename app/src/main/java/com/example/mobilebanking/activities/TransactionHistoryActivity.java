package com.example.mobilebanking.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobilebanking.R;

/**
 * Transaction History Activity - Professional dark mode transaction history screen
 * MB Bank style with filter options and transaction list
 */
public class TransactionHistoryActivity extends AppCompatActivity {
    
    // Filter types
    private static final int FILTER_ALL = 0;
    private static final int FILTER_INCOME = 1;
    private static final int FILTER_EXPENSE = 2;
    
    // Date filter types
    private static final int DATE_FILTER_ALL = 0;
    private static final int DATE_FILTER_TODAY = 1;
    private static final int DATE_FILTER_YESTERDAY = 2;
    private static final int DATE_FILTER_WEEK = 3;
    private static final int DATE_FILTER_MONTH = 4;
    private static final int DATE_FILTER_LAST_7_DAYS = 5;
    private static final int DATE_FILTER_LAST_MONTH = 6;
    private static final int DATE_FILTER_CUSTOM = 7;
    
    // Transaction types
    private static final int TYPE_INCOME = 1;
    private static final int TYPE_EXPENSE = 2;
    
    // Date groups: 0 = Today, 1 = Yesterday, 2 = 16/12/2024
    private static final int DATE_GROUP_TODAY = 0;
    private static final int DATE_GROUP_YESTERDAY = 1;
    private static final int DATE_GROUP_16DEC = 2;
    
    private ImageView ivBack;
    private LinearLayout llDateRange;
    private TextView tvDateRangeLabel;
    private TextView tvFilterAll, tvFilterIncome, tvFilterExpense;
    private LinearLayout llTransaction1, llTransaction2, llTransaction3, llTransaction4, llTransaction5, llTransaction6;
    
    // Date group views
    private TextView tvDateGroupToday, tvDateGroupYesterday, tvDateGroup16Dec;
    private View viewDivider1, viewDivider2;
    
    private int currentFilter = FILTER_ALL;
    private int currentDateFilter = DATE_FILTER_ALL;
    
    // Date filter dialog
    private AlertDialog dateFilterDialog;
    private int selectedQuickOption = -1; // Track selected quick option
    
    // Store transaction types: 1 = INCOME, 2 = EXPENSE
    private int[] transactionTypes = {
        TYPE_EXPENSE,  // Transaction 1: Chuyển tiền (Expense) - Today
        TYPE_EXPENSE,  // Transaction 2: Thanh toán hóa đơn (Expense) - Today
        TYPE_INCOME,   // Transaction 3: Nhận lương (Income) - Today
        TYPE_EXPENSE,  // Transaction 4: Nạp tiền điện thoại (Expense) - Yesterday
        TYPE_EXPENSE,  // Transaction 5: Chuyển tiền (Expense) - Yesterday
        TYPE_INCOME    // Transaction 6: Nhận chuyển khoản (Income) - 16/12/2024
    };
    
    // Store date groups for each transaction: 0 = Today, 1 = Yesterday, 2 = 16/12/2024
    private int[] transactionDateGroups = {
        DATE_GROUP_TODAY,     // Transaction 1
        DATE_GROUP_TODAY,     // Transaction 2
        DATE_GROUP_TODAY,     // Transaction 3
        DATE_GROUP_YESTERDAY, // Transaction 4
        DATE_GROUP_YESTERDAY, // Transaction 5
        DATE_GROUP_16DEC      // Transaction 6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history_dark);

        initializeViews();
        setupClickListeners();
        
        // Apply default filter (show all)
        applyFilter();
    }

    private void initializeViews() {
        // Back button
        ivBack = findViewById(R.id.iv_back);
        
        // Filter section
        llDateRange = findViewById(R.id.ll_date_range);
        tvDateRangeLabel = findViewById(R.id.tv_date_range_label);
        tvFilterAll = findViewById(R.id.tv_filter_all);
        tvFilterIncome = findViewById(R.id.tv_filter_income);
        tvFilterExpense = findViewById(R.id.tv_filter_expense);
        
        // Transaction items
        llTransaction1 = findViewById(R.id.ll_transaction_1);
        llTransaction2 = findViewById(R.id.ll_transaction_2);
        llTransaction3 = findViewById(R.id.ll_transaction_3);
        llTransaction4 = findViewById(R.id.ll_transaction_4);
        llTransaction5 = findViewById(R.id.ll_transaction_5);
        llTransaction6 = findViewById(R.id.ll_transaction_6);
        
        // Date group headers
        tvDateGroupToday = findViewById(R.id.tv_date_group_today);
        tvDateGroupYesterday = findViewById(R.id.tv_date_group_yesterday);
        tvDateGroup16Dec = findViewById(R.id.tv_date_group_16dec);
        
        // Dividers
        viewDivider1 = findViewById(R.id.view_divider_1);
        viewDivider2 = findViewById(R.id.view_divider_2);
    }

    private void setupClickListeners() {
        // Back button
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }
        
        // Date range selector
        if (llDateRange != null) {
            llDateRange.setOnClickListener(v -> showDateFilterDialog());
        }
        
        // Filter buttons
        if (tvFilterAll != null) {
            tvFilterAll.setOnClickListener(v -> {
                currentFilter = FILTER_ALL;
                applyFilter();
            });
        }
        
        if (tvFilterIncome != null) {
            tvFilterIncome.setOnClickListener(v -> {
                currentFilter = FILTER_INCOME;
                applyFilter();
            });
        }
        
        if (tvFilterExpense != null) {
            tvFilterExpense.setOnClickListener(v -> {
                currentFilter = FILTER_EXPENSE;
                applyFilter();
            });
        }
        
        // Transaction item clicks
        if (llTransaction1 != null) {
            llTransaction1.setOnClickListener(v -> {
                Toast.makeText(this, "Chi tiết giao dịch: Chuyển tiền đến Nguyễn Văn B", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llTransaction2 != null) {
            llTransaction2.setOnClickListener(v -> {
                Toast.makeText(this, "Chi tiết giao dịch: Thanh toán hóa đơn điện", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llTransaction3 != null) {
            llTransaction3.setOnClickListener(v -> {
                Toast.makeText(this, "Chi tiết giao dịch: Nhận lương tháng 12", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llTransaction4 != null) {
            llTransaction4.setOnClickListener(v -> {
                Toast.makeText(this, "Chi tiết giao dịch: Nạp tiền điện thoại", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llTransaction5 != null) {
            llTransaction5.setOnClickListener(v -> {
                Toast.makeText(this, "Chi tiết giao dịch: Chuyển tiền đến 9876543210", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llTransaction6 != null) {
            llTransaction6.setOnClickListener(v -> {
                Toast.makeText(this, "Chi tiết giao dịch: Nhận chuyển khoản", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    /**
     * Show date filter dialog with custom layout
     */
    private void showDateFilterDialog() {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View filterView = inflater.inflate(R.layout.layout_transaction_date_filter_dark, null);
        
        // Initialize views from filter layout
        ImageView ivClose = filterView.findViewById(R.id.iv_close_filter);
        LinearLayout llOptionToday = filterView.findViewById(R.id.ll_option_today);
        LinearLayout llOptionLast7Days = filterView.findViewById(R.id.ll_option_last_7_days);
        LinearLayout llOptionThisMonth = filterView.findViewById(R.id.ll_option_this_month);
        LinearLayout llOptionLastMonth = filterView.findViewById(R.id.ll_option_last_month);
        LinearLayout llFromDate = filterView.findViewById(R.id.ll_from_date);
        LinearLayout llToDate = filterView.findViewById(R.id.ll_to_date);
        TextView tvFromDate = filterView.findViewById(R.id.tv_from_date);
        TextView tvToDate = filterView.findViewById(R.id.tv_to_date);
        Button btnReset = filterView.findViewById(R.id.btn_reset_filter);
        Button btnApply = filterView.findViewById(R.id.btn_apply_filter);
        
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(filterView);
        dateFilterDialog = builder.create();
        dateFilterDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Close button
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dateFilterDialog.dismiss());
        }
        
        // Quick options click listeners
        setupQuickOptionClickListener(llOptionToday, DATE_FILTER_TODAY, filterView);
        setupQuickOptionClickListener(llOptionLast7Days, DATE_FILTER_LAST_7_DAYS, filterView);
        setupQuickOptionClickListener(llOptionThisMonth, DATE_FILTER_MONTH, filterView);
        setupQuickOptionClickListener(llOptionLastMonth, DATE_FILTER_LAST_MONTH, filterView);
        
        // Custom date fields (UI only - show toast)
        if (llFromDate != null) {
            llFromDate.setOnClickListener(v -> {
                Toast.makeText(this, "Chọn ngày bắt đầu (UI only)", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (llToDate != null) {
            llToDate.setOnClickListener(v -> {
                Toast.makeText(this, "Chọn ngày kết thúc (UI only)", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Reset button
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                selectedQuickOption = -1;
                resetQuickOptionStyles(filterView);
                if (tvFromDate != null) tvFromDate.setText("01/12/2024");
                if (tvToDate != null) tvToDate.setText("18/12/2024");
            });
        }
        
        // Apply button
        if (btnApply != null) {
            btnApply.setOnClickListener(v -> {
                if (selectedQuickOption != -1) {
                    currentDateFilter = selectedQuickOption;
                    updateDateRangeLabel(selectedQuickOption);
                    applyFilter();
                } else {
                    // Custom date range selected
                    currentDateFilter = DATE_FILTER_CUSTOM;
                    if (tvFromDate != null && tvToDate != null) {
                        String fromDate = tvFromDate.getText().toString();
                        String toDate = tvToDate.getText().toString();
                        tvDateRangeLabel.setText(fromDate + " - " + toDate);
                        applyFilter();
                    }
                }
                dateFilterDialog.dismiss();
            });
        }
        
        // Show dialog
        dateFilterDialog.show();
    }
    
    /**
     * Setup quick option click listener with visual feedback
     */
    private void setupQuickOptionClickListener(LinearLayout optionLayout, int filterType, View parentView) {
        if (optionLayout == null) return;
        
        optionLayout.setOnClickListener(v -> {
            selectedQuickOption = filterType;
            resetQuickOptionStyles(parentView);
            highlightQuickOption(optionLayout);
        });
    }
    
    /**
     * Highlight selected quick option
     */
    private void highlightQuickOption(LinearLayout optionLayout) {
        if (optionLayout == null) return;
        optionLayout.setBackgroundColor(Color.parseColor("#2E7D32")); // Darker green for selected
    }
    
    /**
     * Reset all quick option styles
     */
    private void resetQuickOptionStyles(View parentView) {
        if (parentView == null) return;
        
        LinearLayout llToday = parentView.findViewById(R.id.ll_option_today);
        LinearLayout llLast7Days = parentView.findViewById(R.id.ll_option_last_7_days);
        LinearLayout llThisMonth = parentView.findViewById(R.id.ll_option_this_month);
        LinearLayout llLastMonth = parentView.findViewById(R.id.ll_option_last_month);
        
        int defaultBgColor = Color.parseColor("#1A3A1A");
        
        if (llToday != null) llToday.setBackgroundColor(defaultBgColor);
        if (llLast7Days != null) llLast7Days.setBackgroundColor(defaultBgColor);
        if (llThisMonth != null) llThisMonth.setBackgroundColor(defaultBgColor);
        if (llLastMonth != null) llLastMonth.setBackgroundColor(defaultBgColor);
    }
    
    /**
     * Update date range label
     */
    private void updateDateRangeLabel(int dateFilter) {
        if (tvDateRangeLabel == null) return;
        
        switch (dateFilter) {
            case DATE_FILTER_ALL:
                tvDateRangeLabel.setText("Tất cả");
                break;
            case DATE_FILTER_TODAY:
                tvDateRangeLabel.setText("Hôm nay");
                break;
            case DATE_FILTER_YESTERDAY:
                tvDateRangeLabel.setText("Hôm qua");
                break;
            case DATE_FILTER_WEEK:
                tvDateRangeLabel.setText("Tuần này");
                break;
            case DATE_FILTER_MONTH:
                tvDateRangeLabel.setText("Tháng này");
                break;
            case DATE_FILTER_LAST_7_DAYS:
                tvDateRangeLabel.setText("7 ngày qua");
                break;
            case DATE_FILTER_LAST_MONTH:
                tvDateRangeLabel.setText("Tháng trước");
                break;
            case DATE_FILTER_CUSTOM:
                // Custom date range label is set in apply button click
                break;
        }
    }
    
    /**
     * Apply filter to transaction list (both type and date filters)
     */
    private void applyFilter() {
        // Update filter button styles
        updateFilterButtonStyles();
        
        // Get all transaction items
        LinearLayout[] transactions = {
            llTransaction1, llTransaction2, llTransaction3,
            llTransaction4, llTransaction5, llTransaction6
        };
        
        // Get date group headers and dividers
        TextView[] dateHeaders = {tvDateGroupToday, tvDateGroupYesterday, tvDateGroup16Dec};
        View[] dividers = {viewDivider1, viewDivider2};
        
        // Show/hide transactions based on both filters
        boolean[] dateGroupVisible = {false, false, false}; // Track which date groups have visible items
        
        for (int i = 0; i < transactions.length; i++) {
            if (transactions[i] != null) {
                boolean shouldShowByType = false;
                boolean shouldShowByDate = false;
                
                // Check type filter
                switch (currentFilter) {
                    case FILTER_ALL:
                        shouldShowByType = true;
                        break;
                    case FILTER_INCOME:
                        shouldShowByType = (transactionTypes[i] == TYPE_INCOME);
                        break;
                    case FILTER_EXPENSE:
                        shouldShowByType = (transactionTypes[i] == TYPE_EXPENSE);
                        break;
                }
                
                // Check date filter
                int transactionDateGroup = transactionDateGroups[i];
                switch (currentDateFilter) {
                    case DATE_FILTER_ALL:
                        shouldShowByDate = true;
                        break;
                    case DATE_FILTER_TODAY:
                        shouldShowByDate = (transactionDateGroup == DATE_GROUP_TODAY);
                        break;
                    case DATE_FILTER_YESTERDAY:
                        shouldShowByDate = (transactionDateGroup == DATE_GROUP_YESTERDAY);
                        break;
                    case DATE_FILTER_WEEK:
                        // Show today and yesterday for "this week"
                        shouldShowByDate = (transactionDateGroup == DATE_GROUP_TODAY || 
                                          transactionDateGroup == DATE_GROUP_YESTERDAY);
                        break;
                    case DATE_FILTER_MONTH:
                        // Show all for "this month"
                        shouldShowByDate = true;
                        break;
                    case DATE_FILTER_LAST_7_DAYS:
                        // Show today and yesterday for "last 7 days"
                        shouldShowByDate = (transactionDateGroup == DATE_GROUP_TODAY || 
                                          transactionDateGroup == DATE_GROUP_YESTERDAY);
                        break;
                    case DATE_FILTER_LAST_MONTH:
                        // Show 16/12/2024 for "last month" (mock data)
                        shouldShowByDate = (transactionDateGroup == DATE_GROUP_16DEC);
                        break;
                    case DATE_FILTER_CUSTOM:
                        // Show all for custom range (can be refined later)
                        shouldShowByDate = true;
                        break;
                }
                
                boolean shouldShow = shouldShowByType && shouldShowByDate;
                transactions[i].setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                
                // Track if this date group has any visible items
                if (shouldShow) {
                    dateGroupVisible[transactionDateGroup] = true;
                }
            }
        }
        
        // Show/hide date group headers and dividers based on visible items
        if (tvDateGroupToday != null) {
            tvDateGroupToday.setVisibility(dateGroupVisible[DATE_GROUP_TODAY] ? View.VISIBLE : View.GONE);
        }
        if (tvDateGroupYesterday != null) {
            tvDateGroupYesterday.setVisibility(dateGroupVisible[DATE_GROUP_YESTERDAY] ? View.VISIBLE : View.GONE);
        }
        if (tvDateGroup16Dec != null) {
            tvDateGroup16Dec.setVisibility(dateGroupVisible[DATE_GROUP_16DEC] ? View.VISIBLE : View.GONE);
        }
        
        // Show dividers only if both adjacent groups are visible
        if (viewDivider1 != null) {
            viewDivider1.setVisibility(
                (dateGroupVisible[DATE_GROUP_TODAY] && dateGroupVisible[DATE_GROUP_YESTERDAY]) 
                ? View.VISIBLE : View.GONE
            );
        }
        if (viewDivider2 != null) {
            viewDivider2.setVisibility(
                (dateGroupVisible[DATE_GROUP_YESTERDAY] && dateGroupVisible[DATE_GROUP_16DEC]) 
                ? View.VISIBLE : View.GONE
            );
        }
    }
    
    /**
     * Update filter button styles based on current filter
     */
    private void updateFilterButtonStyles() {
        // Active color: green (#4CAF50)
        int activeTextColor = Color.parseColor("#4CAF50");
        
        // Inactive color: light green (#A5D6A7)
        int inactiveTextColor = Color.parseColor("#A5D6A7");
        
        // Reset all buttons
        if (tvFilterAll != null) {
            if (currentFilter == FILTER_ALL) {
                tvFilterAll.setTextColor(activeTextColor);
                tvFilterAll.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvFilterAll.setTextColor(inactiveTextColor);
                tvFilterAll.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
        
        if (tvFilterIncome != null) {
            if (currentFilter == FILTER_INCOME) {
                tvFilterIncome.setTextColor(activeTextColor);
                tvFilterIncome.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvFilterIncome.setTextColor(inactiveTextColor);
                tvFilterIncome.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
        
        if (tvFilterExpense != null) {
            if (currentFilter == FILTER_EXPENSE) {
                tvFilterExpense.setTextColor(activeTextColor);
                tvFilterExpense.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tvFilterExpense.setTextColor(inactiveTextColor);
                tvFilterExpense.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}


package com.example.mobilebanking.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobilebanking.models.Account;
import com.example.mobilebanking.models.BankBranch;
import com.example.mobilebanking.models.Transaction;
import com.example.mobilebanking.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DataManager class for managing mock data and user sessions
 */
public class DataManager {
    private static DataManager instance;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MobileBankingPrefs";
    private static final String KEY_LOGGED_IN_USER = "loggedInUser";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_LAST_USERNAME = "last_username";
    private static final String KEY_LAST_FULL_NAME = "last_full_name";

    private DataManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // Mock Users
    public List<User> getMockUsers() {
        List<User> users = new ArrayList<>();
        
        // Customer 1
        User customer = new User("U001", "customer1", "Nguyen Van A", 
                "nguyenvana@email.com", "0901234567", User.UserRole.CUSTOMER);
        customer.setPassword("123456");
        customer.setIdNumber("001234567890");
        users.add(customer);

        // Customer 2
        User customer2 = new User("U003", "customer2", "Le Thi C", 
                "lethic@email.com", "0901111111", User.UserRole.CUSTOMER);
        customer2.setPassword("123456");
        customer2.setIdNumber("001111111111");
        users.add(customer2);

        // Officer 1
        User officer = new User("U002", "officer1", "Tran Thi B", 
                "tranthib@bank.com", "0907654321", User.UserRole.OFFICER);
        officer.setPassword("123456");
        officer.setIdNumber("009876543210");
        users.add(officer);

        // Officer 2
        User officer2 = new User("U004", "officer2", "Pham Van D", 
                "phamvand@bank.com", "0902222222", User.UserRole.OFFICER);
        officer2.setPassword("123456");
        officer2.setIdNumber("002222222222");
        users.add(officer2);

        return users;
    }

    // Mock Accounts
    public List<Account> getMockAccounts(String userId) {
        List<Account> accounts = new ArrayList<>();

        // Checking Account
        Account checking = new Account("A001", "1234567890", userId, 
                Account.AccountType.CHECKING, 50000000);
        accounts.add(checking);

        // Savings Account
        Account savings = new Account("A002", "1234567891", userId, 
                Account.AccountType.SAVINGS, 100000000);
        savings.setInterestRate(6.5);
        savings.setMonthlyProfit(541666.67);
        accounts.add(savings);

        // Mortgage Account
        Account mortgage = new Account("A003", "1234567892", userId, 
                Account.AccountType.MORTGAGE, -500000000);
        mortgage.setLoanAmount(500000000);
        mortgage.setMonthlyPayment(15000000);
        mortgage.setRemainingMonths(36);
        accounts.add(mortgage);

        return accounts;
    }

    // Mock Transactions
    public List<Transaction> getMockTransactions(String accountNumber) {
        List<Transaction> transactions = new ArrayList<>();

        Transaction t1 = new Transaction("T001", accountNumber, "9876543210", 
                5000000, Transaction.TransactionType.TRANSFER, "Transfer to friend");
        t1.setStatus(Transaction.TransactionStatus.COMPLETED);
        t1.setReferenceNumber("REF001234567");
        transactions.add(t1);

        Transaction t2 = new Transaction("T002", "9876543210", accountNumber, 
                10000000, Transaction.TransactionType.DEPOSIT, "Salary deposit");
        t2.setStatus(Transaction.TransactionStatus.COMPLETED);
        t2.setReferenceNumber("REF001234568");
        transactions.add(t2);

        Transaction t3 = new Transaction("T003", accountNumber, "ELECTRIC_COMPANY", 
                1500000, Transaction.TransactionType.BILL_PAYMENT, "Electricity bill");
        t3.setStatus(Transaction.TransactionStatus.COMPLETED);
        t3.setReferenceNumber("REF001234569");
        transactions.add(t3);

        return transactions;
    }

    // Mock Bank Branches (Hanoi locations)
    public List<BankBranch> getMockBankBranches() {
        List<BankBranch> branches = new ArrayList<>();

        branches.add(new BankBranch("B001", "Hoan Kiem Branch", 
                "12 Trang Tien, Hoan Kiem, Hanoi", 21.0285, 105.8542, "024-38254321"));
        
        branches.add(new BankBranch("B002", "Cau Giay Branch", 
                "456 Nguyen Trai, Cau Giay, Hanoi", 21.0245, 105.7979, "024-37654321"));
        
        branches.add(new BankBranch("B003", "Dong Da Branch", 
                "789 Lang Ha, Dong Da, Hanoi", 21.0167, 105.8127, "024-36254321"));
        
        branches.add(new BankBranch("B004", "Ba Dinh Branch", 
                "234 Giang Vo, Ba Dinh, Hanoi", 21.0278, 105.8194, "024-38354321"));

        return branches;
    }

    // Session Management
    public void saveLoggedInUser(String username, User.UserRole role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LOGGED_IN_USER, username);
        editor.putString(KEY_USER_ROLE, role.name());
        editor.apply();
    }

    public String getLoggedInUser() {
        return sharedPreferences.getString(KEY_LOGGED_IN_USER, null);
    }

    public User.UserRole getUserRole() {
        String role = sharedPreferences.getString(KEY_USER_ROLE, null);
        return role != null ? User.UserRole.valueOf(role) : null;
    }

    public void logout() {
        // Lưu username và fullName trước khi clear để giữ lại cho lần đăng nhập sau
        String lastUsername = getLoggedInUser();
        String lastFullName = getLastFullName(); // Giữ lại fullName hiện tại
        
        // Xóa session và token trong SharedPreferences
        // NHƯNG KHÔNG xóa refresh token trong Keystore (để có thể đăng nhập bằng vân tay lần sau)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_LOGGED_IN_USER);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        
        // Lưu lại username và fullName cuối cùng (nếu có)
        if (lastUsername != null) {
            editor.putString(KEY_LAST_USERNAME, lastUsername);
        }
        if (lastFullName != null && !lastFullName.isEmpty()) {
            editor.putString(KEY_LAST_FULL_NAME, lastFullName);
        }
        
        editor.apply();
    }
    
    /**
     * Lưu username cuối cùng đã đăng nhập
     */
    public void saveLastUsername(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LAST_USERNAME, username);
        editor.apply();
    }
    
    /**
     * Lấy username cuối cùng đã đăng nhập
     */
    public String getLastUsername() {
        return sharedPreferences.getString(KEY_LAST_USERNAME, null);
    }
    
    /**
     * Lưu tên đầy đủ (full name) của người dùng đăng nhập lần cuối
     */
    public void saveLastFullName(String fullName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LAST_FULL_NAME, fullName);
        editor.apply();
    }
    
    /**
     * Lấy tên đầy đủ (full name) của người dùng đăng nhập lần cuối
     */
    public String getLastFullName() {
        return sharedPreferences.getString(KEY_LAST_FULL_NAME, null);
    }

    public boolean isLoggedIn() {
        return getLoggedInUser() != null;
    }
    
    // Token Management (Mock - trong production sẽ gọi API)
    /**
     * Lưu access token và refresh token sau khi đăng nhập thành công
     * Mỗi lần đăng nhập đều cấp lại token mới
     */
    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }
    
    /**
     * Lấy access token hiện tại
     */
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }
    
    /**
     * Lấy refresh token hiện tại (chỉ dùng để lưu vào Keystore)
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }
    
    /**
     * Mock: Tạo access token và refresh token mới
     * Trong production, sẽ gọi API để lấy token từ backend
     */
    public void generateNewTokens(String username) {
        // Mock token generation
        // Trong production, đây sẽ là response từ API
        String accessToken = "mock_access_token_" + username + "_" + System.currentTimeMillis();
        String refreshToken = "mock_refresh_token_" + username + "_" + System.currentTimeMillis();
        saveTokens(accessToken, refreshToken);
    }
    
    /**
     * Mock: Refresh access token bằng refresh token
     * Trong production, sẽ gọi API /refresh với refresh token
     */
    public boolean refreshAccessToken(String refreshToken) {
        // Mock: Giả sử refresh token hợp lệ
        // Trong production, sẽ gọi API để refresh token
        String username = getLoggedInUser();
        if (username != null) {
            String newAccessToken = "mock_access_token_" + username + "_" + System.currentTimeMillis();
            String newRefreshToken = "mock_refresh_token_" + username + "_" + System.currentTimeMillis();
            saveTokens(newAccessToken, newRefreshToken);
            return true;
        }
        return false;
    }
    
    /**
     * Xóa tất cả token khi logout
     */
    public void clearTokens() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }
}


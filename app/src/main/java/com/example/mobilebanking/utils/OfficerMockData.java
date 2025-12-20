package com.example.mobilebanking.utils;

import com.example.mobilebanking.api.dto.TransactionDTO;
import com.example.mobilebanking.models.MortgageModel;
import com.example.mobilebanking.models.UserModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OfficerMockData - Singleton chứa mock data cho Officer functions
 * Không cần call API thật, chỉ dùng để demo giao diện
 */
public class OfficerMockData {
    private static OfficerMockData instance;
    
    private OfficerMockData() {
        // Private constructor
    }
    
    public static synchronized OfficerMockData getInstance() {
        if (instance == null) {
            instance = new OfficerMockData();
        }
        return instance;
    }
    
    /**
     * Lấy danh sách mock users (20 users)
     */
    public List<UserModel> getMockUsers() {
        List<UserModel> users = new ArrayList<>();
        
        // 16 Customers
        users.add(new UserModel(1L, "0901234567", "Nguyễn Văn An", "an.nguyen@example.com", "customer", false, "2024-01-15", "001234567890", "1990-05-20"));
        users.add(new UserModel(2L, "0902345678", "Trần Thị Bình", "binh.tran@example.com", "customer", false, "2024-01-20", "002345678901", "1992-08-15"));
        users.add(new UserModel(3L, "0903456789", "Lê Hoàng Cường", "cuong.le@example.com", "customer", true, "2024-02-10", "003456789012", "1988-12-30"));
        users.add(new UserModel(4L, "0904567890", "Phạm Thị Dung", "dung.pham@example.com", "customer", false, "2024-02-15", "004567890123", "1995-03-25"));
        users.add(new UserModel(5L, "0905678901", "Hoàng Văn Em", "em.hoang@example.com", "customer", false, "2024-03-01", "005678901234", "1991-07-10"));
        users.add(new UserModel(6L, "0906789012", "Vũ Thị Phượng", "phuong.vu@example.com", "customer", false, "2024-03-10", "006789012345", "1993-11-05"));
        users.add(new UserModel(7L, "0907890123", "Đặng Văn Giang", "giang.dang@example.com", "customer", false, "2024-03-20", "007890123456", "1989-04-18"));
        users.add(new UserModel(8L, "0908901234", "Bùi Thị Hương", "huong.bui@example.com", "customer", true, "2024-04-01", "008901234567", "1994-09-22"));
        users.add(new UserModel(9L, "0909012345", "Trịnh Văn Hải", "hai.trinh@example.com", "customer", false, "2024-04-15", "009012345678", "1987-06-14"));
        users.add(new UserModel(10L, "0900123456", "Lý Thị Khánh", "khanh.ly@example.com", "customer", false, "2024-05-01", "010123456789", "1996-02-28"));
        users.add(new UserModel(11L, "0911234567", "Võ Văn Long", "long.vo@example.com", "customer", false, "2024-05-10", "011234567890", "1990-10-11"));
        users.add(new UserModel(12L, "0912345678", "Đinh Thị Mai", "mai.dinh@example.com", "customer", false, "2024-05-20", "012345678901", "1992-01-07"));
        users.add(new UserModel(13L, "0913456789", "Phan Văn Nam", "nam.phan@example.com", "customer", false, "2024-06-01", "013456789012", "1991-12-19"));
        users.add(new UserModel(14L, "0914567890", "Dương Thị Oanh", "oanh.duong@example.com", "customer", false, "2024-06-15", "014567890123", "1993-05-03"));
        users.add(new UserModel(15L, "0915678901", "Ngô Văn Phong", "phong.ngo@example.com", "customer", false, "2024-07-01", "015678901234", "1988-08-27"));
        users.add(new UserModel(16L, "0916789012", "Trương Thị Quỳnh", "quynh.truong@example.com", "customer", false, "2024-07-15", "016789012345", "1995-11-12"));
        
        // 4 Officers
        users.add(new UserModel(17L, "0900000001", "Lê Thị Lan", "lan.le@bankstaff.com", "officer", false, "2023-01-01", "017890123456", "1985-03-15"));
        users.add(new UserModel(18L, "0900000002", "Phạm Văn Minh", "minh.pham@bankstaff.com", "officer", false, "2023-01-01", "018901234567", "1987-07-20"));
        users.add(new UserModel(19L, "0900000003", "Nguyễn Thị Hoa", "hoa.nguyen@bankstaff.com", "officer", false, "2023-01-01", "019012345678", "1986-09-10"));
        users.add(new UserModel(20L, "0900000004", "Trần Văn Tùng", "tung.tran@bankstaff.com", "officer", false, "2023-01-01", "020123456789", "1984-12-05"));
        
        return users;
    }
    
    /**
     * Lấy danh sách mock mortgages (15 khoản vay)
     */
    public List<MortgageModel> getMockMortgages() {
        List<MortgageModel> mortgages = new ArrayList<>();
        
        // 5 PENDING_APPRAISAL (Chờ duyệt)
        mortgages.add(new MortgageModel(1L, "VAY001", "Nguyễn Văn An", "0901234567", 
            500000000, 8.5, 120, "PENDING_APPRAISAL", "2024-12-15", 
            "Nhà đất", "Nhà 3 tầng tại Hà Nội", 6000000));
        mortgages.add(new MortgageModel(2L, "VAY002", "Trần Thị Bình", "0902345678", 
            800000000, 8.8, 180, "PENDING_APPRAISAL", "2024-12-18", 
            "Căn hộ", "Chung cư cao cấp Q1", 8500000));
        mortgages.add(new MortgageModel(3L, "VAY003", "Lê Hoàng Cường", "0903456789", 
            300000000, 9.0, 60, "PENDING_APPRAISAL", "2024-12-19", 
            "Xe ô tô", "Toyota Camry 2023", 5500000));
        mortgages.add(new MortgageModel(4L, "VAY004", "Phạm Thị Dung", "0904567890", 
            1200000000, 8.2, 240, "PENDING_APPRAISAL", "2024-12-20", 
            "Nhà đất", "Biệt thự Đà Nẵng", 12000000));
        mortgages.add(new MortgageModel(5L, "VAY005", "Hoàng Văn Em", "0905678901", 
            450000000, 8.7, 100, "PENDING_APPRAISAL", "2024-12-20", 
            "Nhà đất", "Nhà 2 tầng Hải Phòng", 5200000));
        
        // 6 ACTIVE (Đang vay)
        mortgages.add(new MortgageModel(6L, "VAY006", "Vũ Thị Phượng", "0906789012", 
            600000000, 8.5, 120, "ACTIVE", "2024-10-01", 
            "Nhà đất", "Nhà phố HCM", 7200000));
        mortgages.add(new MortgageModel(7L, "VAY007", "Đặng Văn Giang", "0907890123", 
            900000000, 8.8, 180, "ACTIVE", "2024-09-15", 
            "Căn hộ", "Chung cư Vinhomes", 9500000));
        mortgages.add(new MortgageModel(8L, "VAY008", "Bùi Thị Hương", "0908901234", 
            350000000, 9.0, 60, "ACTIVE", "2024-11-01", 
            "Xe ô tô", "Honda CR-V", 6400000));
        mortgages.add(new MortgageModel(9L, "VAY009", "Trịnh Văn Hải", "0909012345", 
            750000000, 8.3, 150, "ACTIVE", "2024-08-20", 
            "Nhà đất", "Nhà mặt tiền", 8000000));
        mortgages.add(new MortgageModel(10L, "VAY010", "Lý Thị Khánh", "0900123456", 
            400000000, 8.6, 90, "ACTIVE", "2024-10-10", 
            "Nhà đất", "Nhà cấp 4", 4800000));
        mortgages.add(new MortgageModel(11L, "VAY011", "Võ Văn Long", "0911234567", 
            550000000, 8.4, 110, "ACTIVE", "2024-09-01", 
            "Căn hộ", "Chung cư mini", 6300000));
        
        // 2 COMPLETED (Hoàn thành)
        mortgages.add(new MortgageModel(12L, "VAY012", "Đinh Thị Mai", "0912345678", 
            200000000, 9.5, 60, "COMPLETED", "2023-01-01", 
            "Xe ô tô", "Ford Everest", 0));
        mortgages.add(new MortgageModel(13L, "VAY013", "Phan Văn Nam", "0913456789", 
            300000000, 9.2, 72, "COMPLETED", "2022-06-15", 
            "Nhà đất", "Đất nền", 0));
        
        // 2 REJECTED (Từ chối)
        mortgages.add(new MortgageModel(14L, "VAY014", "Dương Thị Oanh", "0914567890", 
            1500000000, 8.0, 300, "REJECTED", "2024-12-10", 
            "Nhà đất", "Không đủ tài sản thế chấp", 0));
        mortgages.add(new MortgageModel(15L, "VAY015", "Ngô Văn Phong", "0915678901", 
            250000000, 10.0, 48, "REJECTED", "2024-12-05", 
            "Xe máy", "Không đủ điều kiện", 0));
        
        return mortgages;
    }
    
    /**
     * Lấy mock transactions cho 1 customer cụ thể
     */
    public List<TransactionDTO> getMockCustomerTransactions(String phone) {
        List<TransactionDTO> transactions = new ArrayList<>();
        
        // Tạo 10 giao dịch mẫu sử dụng constructor đầy đủ
        // TransactionDTO(transactionId, code, senderAccountNumber, senderAccountName, 
        //                receiverAccountNumber, receiverAccountName, amount, transactionType, 
        //                description, status, createdAt)
        
        transactions.add(new TransactionDTO(
            1L, "TXN001", "0123456789", "Nguyễn Văn An", 
            "9876543210", "Trần Thị Bình", 500000.0, "TRANSFER",
            "Chuyển tiền mua hàng", "SUCCESS", "2024-12-20T10:30:00"
        ));
        
        transactions.add(new TransactionDTO(
            2L, "TXN002", "9876543210", "Trần Thị Bình", 
            "0123456789", "Nguyễn Văn An", 1000000.0, "TRANSFER",
            "Nhận lương tháng 12", "SUCCESS", "2024-12-15T09:00:00"
        ));
        
        transactions.add(new TransactionDTO(
            3L, "TXN003", "0123456789", "Nguyễn Văn An", 
            "EVN00001", "Công ty Điện lực", 350000.0, "WITHDRAW",
            "Thanh toán tiền điện", "SUCCESS", "2024-12-10T14:20:00"
        ));
        
        transactions.add(new TransactionDTO(
            4L, "TXN004", "0123456789", "Nguyễn Văn An", 
            "WATER001", "Công ty Nước sạch", 150000.0, "WITHDRAW",
            "Thanh toán tiền nước", "SUCCESS", "2024-12-10T14:25:00"
        ));
        
        transactions.add(new TransactionDTO(
            5L, "TXN005", "0123456789", "Nguyễn Văn An", 
            "5555555555", "Lê Văn Cường", 2000000.0, "TRANSFER",
            "Trả nợ bạn", "SUCCESS", "2024-12-08T16:45:00"
        ));
        
        transactions.add(new TransactionDTO(
            6L, "TXN006", "1111111111", "Phạm Thị Dung", 
            "0123456789", "Nguyễn Văn An", 500000.0, "TRANSFER",
            "Hoàn tiền", "SUCCESS", "2024-12-05T11:00:00"
        ));
        
        transactions.add(new TransactionDTO(
            7L, "TXN007", "0123456789", "Nguyễn Văn An", 
            "FPT00001", "FPT Telecom", 200000.0, "WITHDRAW",
            "Cước internet tháng 12", "SUCCESS", "2024-12-01T08:00:00"
        ));
        
        transactions.add(new TransactionDTO(
            8L, "TXN008", "0123456789", "Nguyễn Văn An", 
            "7777777777", "Shop Online ABC", 3000000.0, "TRANSFER",
            "Mua hàng online", "SUCCESS", "2024-11-28T19:30:00"
        ));
        
        transactions.add(new TransactionDTO(
            9L, "TXN009", "8888888888", "Bố Nguyễn", 
            "0123456789", "Nguyễn Văn An", 5000000.0, "TRANSFER",
            "Nhận tiền từ gia đình", "SUCCESS", "2024-11-25T12:00:00"
        ));
        
        transactions.add(new TransactionDTO(
            10L, "TXN010", "0123456789", "Nguyễn Văn An", 
            "VIETTEL", "Viettel Telecom", 100000.0, "WITHDRAW",
            "Nạp tiền điện thoại", "SUCCESS", "2024-11-20T15:30:00"
        ));
        
        return transactions;
    }
    
    /**
     * Lấy thống kê tổng quan cho báo cáo
     */
    public ReportStatistics getReportStatistics() {
        return new ReportStatistics(
            20, // Tổng số khách hàng
            15, // Tổng số khoản vay
            5,  // Khoản vay chờ duyệt
            47, // Tổng giao dịch hôm nay
            150000000, // Tổng giá trị GD hôm nay
            6   // Khoản vay đang hoạt động
        );
    }
    
    /**
     * Inner class cho thống kê báo cáo
     */
    public static class ReportStatistics {
        public int totalCustomers;
        public int totalMortgages;
        public int pendingMortgages;
        public int todayTransactions;
        public double todayTransactionValue;
        public int activeMortgages;
        
        public ReportStatistics(int totalCustomers, int totalMortgages, 
                               int pendingMortgages, int todayTransactions,
                               double todayTransactionValue, int activeMortgages) {
            this.totalCustomers = totalCustomers;
            this.totalMortgages = totalMortgages;
            this.pendingMortgages = pendingMortgages;
            this.todayTransactions = todayTransactions;
            this.todayTransactionValue = todayTransactionValue;
            this.activeMortgages = activeMortgages;
        }
    }
}


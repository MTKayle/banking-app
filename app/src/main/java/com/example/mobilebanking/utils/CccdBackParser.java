package com.example.mobilebanking.utils;

import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for CCCD back side text extraction
 * Extracts: Đặc điểm nhận dạng, Ngày cấp, Nơi cấp, Authority seal
 */
public class CccdBackParser {
    private static final String TAG = "CccdBackParser";
    
    public static class CccdBackData {
        private String race; // Dân tộc
        private String religion; // Tôn giáo
        private String classInfo; // Giai cấp
        private String hometown; // Quê quán
        private String permanentAddress; // Địa chỉ thường trú
        private String issueDate; // Ngày cấp (DD/MM/YYYY)
        private String issuePlace; // Nơi cấp
        private boolean hasAuthoritySeal; // Có dấu authority seal
        
        // Getters
        public String getRace() { return race; }
        public String getReligion() { return religion; }
        public String getClassInfo() { return classInfo; }
        public String getHometown() { return hometown; }
        public String getPermanentAddress() { return permanentAddress; }
        public String getIssueDate() { return issueDate; }
        public String getIssuePlace() { return issuePlace; }
        public boolean hasAuthoritySeal() { return hasAuthoritySeal; }
        
        // Setters
        public void setRace(String race) { this.race = race; }
        public void setReligion(String religion) { this.religion = religion; }
        public void setClassInfo(String classInfo) { this.classInfo = classInfo; }
        public void setHometown(String hometown) { this.hometown = hometown; }
        public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
        public void setIssuePlace(String issuePlace) { this.issuePlace = issuePlace; }
        public void setHasAuthoritySeal(boolean hasAuthoritySeal) { this.hasAuthoritySeal = hasAuthoritySeal; }
        
        public boolean isValid() {
            return permanentAddress != null && !permanentAddress.isEmpty() &&
                   issueDate != null && !issueDate.isEmpty();
        }
        
        @Override
        public String toString() {
            return "CccdBackData{" +
                    "race='" + race + '\'' +
                    ", religion='" + religion + '\'' +
                    ", classInfo='" + classInfo + '\'' +
                    ", hometown='" + hometown + '\'' +
                    ", permanentAddress='" + permanentAddress + '\'' +
                    ", issueDate='" + issueDate + '\'' +
                    ", issuePlace='" + issuePlace + '\'' +
                    ", hasAuthoritySeal=" + hasAuthoritySeal +
                    '}';
        }
    }
    
    /**
     * Parse text from CCCD back side
     */
    public static CccdBackData parseBackText(String text) {
        if (text == null || text.trim().isEmpty()) {
            Log.w(TAG, "Empty text provided");
            return null;
        }
        
        Log.d(TAG, "=== PARSING CCCD BACK TEXT ===");
        Log.d(TAG, "Text length: " + text.length());
        Log.d(TAG, "First 500 chars: " + text.substring(0, Math.min(500, text.length())));
        
        CccdBackData data = new CccdBackData();
        String upperText = text.toUpperCase();
        
        // 1. Check for authority seal
        data.setHasAuthoritySeal(checkAuthoritySeal(upperText));
        
        // 2. Extract Đặc điểm nhận dạng section
        extractIdentificationFeatures(upperText, text, data);
        
        // 3. Extract Ngày cấp (Issue Date)
        extractIssueDate(upperText, text, data);
        
        // 4. Extract Nơi cấp (Issue Place)
        extractIssuePlace(upperText, text, data);
        
        // 5. Extract Địa chỉ thường trú (Permanent Address)
        extractPermanentAddress(upperText, text, data);
        
        Log.d(TAG, "Parsed data: " + data.toString());
        return data;
    }
    
    /**
     * Check for authority seal keywords
     */
    private static boolean checkAuthoritySeal(String text) {
        String[] authorityKeywords = {
            "CỤ TRƯỞNG CỤC CẢNH SÁT",
            "CỤC CẢNH SÁT",
            "QUẢN LÝ HÀNH CHÍNH",
            "TRẬT TỰ XÃ HỘI",
            "CẢNH SÁT QUẢN LÝ"
        };
        
        int matchCount = 0;
        for (String keyword : authorityKeywords) {
            if (text.contains(keyword)) {
                matchCount++;
            }
        }
        
        boolean hasSeal = matchCount >= 2; // Need at least 2 keywords
        Log.d(TAG, "Authority seal check: " + hasSeal + " (matched " + matchCount + " keywords)");
        return hasSeal;
    }
    
    /**
     * Extract Đặc điểm nhận dạng (Race, Religion, Class, Hometown)
     */
    private static void extractIdentificationFeatures(String upperText, String originalText, CccdBackData data) {
        // Find "ĐẶC ĐIỂM NHẬN DẠNG" section
        int featureStart = upperText.indexOf("ĐẶC ĐIỂM NHẬN DẠNG");
        if (featureStart == -1) {
            featureStart = upperText.indexOf("ĐẶC ĐIỂM");
        }
        
        if (featureStart == -1) {
            Log.w(TAG, "Could not find 'Đặc điểm nhận dạng' section");
            return;
        }
        
        // Extract text after "ĐẶC ĐIỂM NHẬN DẠNG"
        String featureSection = originalText.substring(featureStart);
        
        // Pattern for Dân tộc (Race)
        Pattern racePattern = Pattern.compile("(?i)(?:dân tộc|dân tộc:)\\s*([^,\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher raceMatcher = racePattern.matcher(featureSection);
        if (raceMatcher.find()) {
            data.setRace(raceMatcher.group(1).trim());
            Log.d(TAG, "Found race: " + data.getRace());
        }
        
        // Pattern for Tôn giáo (Religion)
        Pattern religionPattern = Pattern.compile("(?i)(?:tôn giáo|tôn giáo:)\\s*([^,\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher religionMatcher = religionPattern.matcher(featureSection);
        if (religionMatcher.find()) {
            data.setReligion(religionMatcher.group(1).trim());
            Log.d(TAG, "Found religion: " + data.getReligion());
        }
        
        // Pattern for Giai cấp (Class) - optional
        Pattern classPattern = Pattern.compile("(?i)(?:giai cấp|giai cấp:)\\s*([^,\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher classMatcher = classPattern.matcher(featureSection);
        if (classMatcher.find()) {
            data.setClassInfo(classMatcher.group(1).trim());
            Log.d(TAG, "Found class: " + data.getClassInfo());
        }
        
        // Pattern for Quê quán (Hometown)
        Pattern hometownPattern = Pattern.compile("(?i)(?:quê quán|quê quán:)\\s*([^,\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher hometownMatcher = hometownPattern.matcher(featureSection);
        if (hometownMatcher.find()) {
            data.setHometown(hometownMatcher.group(1).trim());
            Log.d(TAG, "Found hometown: " + data.getHometown());
        }
    }
    
    /**
     * Extract Ngày cấp (Issue Date) - format DD/MM/YYYY or "ngày xx tháng xx năm xxxx"
     */
    private static void extractIssueDate(String upperText, String originalText, CccdBackData data) {
        // Pattern 1: DD/MM/YYYY
        Pattern datePattern1 = Pattern.compile("(?:ngày cấp|ngày cấp:)\\s*(\\d{1,2}/\\d{1,2}/\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher dateMatcher1 = datePattern1.matcher(originalText);
        if (dateMatcher1.find()) {
            String dateStr = dateMatcher1.group(1);
            if (isValidDate(dateStr)) {
                data.setIssueDate(dateStr);
                Log.d(TAG, "Found issue date (format 1): " + data.getIssueDate());
                return;
            }
        }
        
        // Pattern 2: "ngày xx tháng xx năm xxxx"
        Pattern datePattern2 = Pattern.compile("(?:ngày cấp|ngày cấp:)\\s*ngày\\s*(\\d{1,2})\\s*tháng\\s*(\\d{1,2})\\s*năm\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher dateMatcher2 = datePattern2.matcher(originalText);
        if (dateMatcher2.find()) {
            String day = dateMatcher2.group(1);
            String month = dateMatcher2.group(2);
            String year = dateMatcher2.group(3);
            String dateStr = String.format("%s/%s/%s", day, month, year);
            if (isValidDate(dateStr)) {
                data.setIssueDate(dateStr);
                Log.d(TAG, "Found issue date (format 2): " + data.getIssueDate());
                return;
            }
        }
        
        // Pattern 3: Just look for DD/MM/YYYY near "NGÀY CẤP"
        int dateSectionStart = upperText.indexOf("NGÀY CẤP");
        if (dateSectionStart != -1) {
            String dateSection = originalText.substring(Math.max(0, dateSectionStart - 50), 
                                                         Math.min(originalText.length(), dateSectionStart + 100));
            Pattern datePattern3 = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})");
            Matcher dateMatcher3 = datePattern3.matcher(dateSection);
            if (dateMatcher3.find()) {
                String dateStr = dateMatcher3.group(1);
                if (isValidDate(dateStr)) {
                    data.setIssueDate(dateStr);
                    Log.d(TAG, "Found issue date (format 3): " + data.getIssueDate());
                    return;
                }
            }
        }
        
        Log.w(TAG, "Could not extract issue date");
    }
    
    /**
     * Extract Nơi cấp (Issue Place)
     */
    private static void extractIssuePlace(String upperText, String originalText, CccdBackData data) {
        // Pattern: "Nơi cấp: [place]" or "Nơi cấp [place]"
        Pattern placePattern = Pattern.compile("(?:nơi cấp|nơi cấp:)\\s*([^\\n]+?)(?:\\.|,|$)", Pattern.CASE_INSENSITIVE);
        Matcher placeMatcher = placePattern.matcher(originalText);
        if (placeMatcher.find()) {
            String place = placeMatcher.group(1).trim();
            // Remove common suffixes
            place = place.replaceAll("(?:^|\\s)(?:CỤC|PHÒNG|SỞ).*$", "").trim();
            if (!place.isEmpty()) {
                data.setIssuePlace(place);
                Log.d(TAG, "Found issue place: " + data.getIssuePlace());
                return;
            }
        }
        
        // Alternative: Look for text after "NƠI CẤP" until next section
        int placeStart = upperText.indexOf("NƠI CẤP");
        if (placeStart != -1) {
            String placeSection = originalText.substring(placeStart);
            // Extract until next keyword or newline
            Pattern placePattern2 = Pattern.compile("NƠI CẤP[^:]*:?\\s*([^\\n]+?)(?:\\.|,|\\n|NGÀY|ĐỊA CHỈ|$)", Pattern.CASE_INSENSITIVE);
            Matcher placeMatcher2 = placePattern2.matcher(placeSection);
            if (placeMatcher2.find()) {
                String place = placeMatcher2.group(1).trim();
                if (place.length() > 3 && place.length() < 100) {
                    data.setIssuePlace(place);
                    Log.d(TAG, "Found issue place (alternative): " + data.getIssuePlace());
                }
            }
        }
    }
    
    /**
     * Extract Địa chỉ thường trú (Permanent Address)
     */
    private static void extractPermanentAddress(String upperText, String originalText, CccdBackData data) {
        // Find "NƠI THƯỜNG TRÚ" or "ĐỊA CHỈ THƯỜNG TRÚ"
        int addressStart = upperText.indexOf("NƠI THƯỜNG TRÚ");
        if (addressStart == -1) {
            addressStart = upperText.indexOf("ĐỊA CHỈ THƯỜNG TRÚ");
        }
        if (addressStart == -1) {
            addressStart = upperText.indexOf("THƯỜNG TRÚ");
        }
        
        if (addressStart == -1) {
            Log.w(TAG, "Could not find 'Nơi thường trú' section");
            return;
        }
        
        // Extract address text after keyword
        String addressSection = originalText.substring(addressStart);
        
        // Pattern: Extract text after "NƠI THƯỜNG TRÚ:" until next section or end
        Pattern addressPattern = Pattern.compile("(?:NƠI THƯỜNG TRÚ|ĐỊA CHỈ THƯỜNG TRÚ|THƯỜNG TRÚ)[^:]*:?\\s*([^\\n]+?)(?:\\.|,|\\n|NGÀY|ĐẶC ĐIỂM|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher addressMatcher = addressPattern.matcher(addressSection);
        if (addressMatcher.find()) {
            String address = addressMatcher.group(1).trim();
            // Clean up address
            address = address.replaceAll("\\s+", " ").trim();
            if (address.length() > 10) { // Minimum address length
                data.setPermanentAddress(address);
                Log.d(TAG, "Found permanent address: " + data.getPermanentAddress());
                return;
            }
        }
        
        // Alternative: Extract multiple lines after keyword
        String[] lines = addressSection.split("\\n");
        if (lines.length > 1) {
            StringBuilder addressBuilder = new StringBuilder();
            for (int i = 1; i < Math.min(lines.length, 5); i++) {
                String line = lines[i].trim();
                if (line.isEmpty() || line.matches("^[A-Z\\s]+$") && line.length() < 5) {
                    continue; // Skip header lines
                }
                if (line.contains("NGÀY") || line.contains("ĐẶC ĐIỂM")) {
                    break; // Stop at next section
                }
                if (addressBuilder.length() > 0) {
                    addressBuilder.append(", ");
                }
                addressBuilder.append(line);
            }
            String address = addressBuilder.toString().trim();
            if (address.length() > 10) {
                data.setPermanentAddress(address);
                Log.d(TAG, "Found permanent address (alternative): " + data.getPermanentAddress());
            }
        }
    }
    
    /**
     * Validate date format DD/MM/YYYY
     */
    private static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        
        Pattern datePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
        Matcher matcher = datePattern.matcher(dateStr);
        if (!matcher.matches()) {
            return false;
        }
        
        try {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            
            // Basic validation
            if (year < 1900 || year > 2100) return false;
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}







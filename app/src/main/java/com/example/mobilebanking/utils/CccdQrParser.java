package com.example.mobilebanking.utils;

import android.util.Log;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Vietnamese CCCD (Citizen ID Card) QR code data
 */
public class CccdQrParser {
    private static final String TAG = "CccdQrParser";

    public static class CccdData {
        private String fullName; // Ho (Họ và Tên)
        private String idNumber; // soCCCD
        private String dateOfBirth; // ngaySinh (dd/mm/yyyy)
        private String gender; // gioiTinh (Nam/Nữ)
        private String permanentAddress; // diaChiThuongTru (Nơi thường trú)
        private String issueDate; // ngayCap (dd/mm/yyyy)

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getIdNumber() { return idNumber; }
        public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getPermanentAddress() { return permanentAddress; }
        public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

        public String getIssueDate() { return issueDate; }
        public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

        // Backward compatibility
        @Deprecated
        public String getAddress() { return permanentAddress; }
        @Deprecated
        public void setAddress(String address) { this.permanentAddress = address; }
    }

    /**
     * Parse QR code data from Vietnamese CCCD
     * CCCD QR codes can be in different formats:
     * 1. JSON format
     * 2. Pipe-delimited format
     * 3. Base64 encoded format
     * 4. Other custom formats
     */
    public static CccdData parseQrData(String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            Log.e(TAG, "QR data is null or empty");
            return null;
        }

        CccdData data = new CccdData();
        String originalData = qrData;
        qrData = qrData.trim();
        
        Log.d(TAG, "=== PARSING QR DATA ===");
        Log.d(TAG, "QR data length: " + qrData.length());
        Log.d(TAG, "Full QR data: " + qrData);
        Log.d(TAG, "First 200 chars: " + (qrData.length() > 200 ? qrData.substring(0, 200) : qrData));

        try {
            // Try to decode Base64 first (some CCCD QR codes are Base64 encoded)
            String decodedData = tryDecodeBase64(qrData);
            if (decodedData != null && !decodedData.equals(qrData)) {
                Log.d(TAG, "Decoded Base64 data");
                qrData = decodedData;
            }

            // Try to parse as JSON first
            if (qrData.startsWith("{") || qrData.startsWith("[")) {
                Log.d(TAG, "Attempting JSON parse");
                CccdData jsonResult = parseJsonFormat(qrData, data);
                if (jsonResult != null && (jsonResult.getFullName() != null || jsonResult.getIdNumber() != null)) {
                    Log.d(TAG, "Successfully parsed as JSON");
                    return jsonResult;
                }
            }

            // Try pipe-delimited format
            if (qrData.contains("|")) {
                Log.d(TAG, "Attempting pipe-delimited parse");
                CccdData pipeResult = parsePipeDelimitedFormat(qrData, data);
                if (pipeResult != null && (pipeResult.getFullName() != null || pipeResult.getIdNumber() != null)) {
                    Log.d(TAG, "Successfully parsed as pipe-delimited");
                    return pipeResult;
                }
            }

            // Try comma-delimited format (some CCCD use commas)
            if (qrData.contains(",") && qrData.split(",").length >= 3) {
                Log.d(TAG, "Attempting comma-delimited parse");
                CccdData commaResult = parseCommaDelimitedFormat(qrData, data);
                if (commaResult != null && (commaResult.getFullName() != null || commaResult.getIdNumber() != null)) {
                    Log.d(TAG, "Successfully parsed as comma-delimited");
                    return commaResult;
                }
            }

            // Try to extract data using regex patterns
            Log.d(TAG, "Attempting regex parse");
            CccdData regexResult = parseWithRegex(qrData, data);
            if (regexResult != null && (regexResult.getFullName() != null || regexResult.getIdNumber() != null)) {
                Log.d(TAG, "Successfully parsed with regex");
                return regexResult;
            }

            Log.w(TAG, "Could not parse QR data in any known format");
            Log.w(TAG, "If QR data is just a number, it might be ID-only format");
            
            // Last resort: if it's just a 12-digit number, treat it as CCCD number
            // Ignore 9-digit numbers (old CMND format)
            if (qrData.matches("^\\d{12}$")) {
                Log.d(TAG, "Treating QR data as CCCD number only (12 digits)");
                CccdData idOnlyData = new CccdData();
                idOnlyData.setIdNumber(qrData);
                return idOnlyData;
            }
            
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing QR data: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Try to decode Base64 string
     */
    private static String tryDecodeBase64(String data) {
        try {
            // Check if it looks like Base64
            if (data.matches("^[A-Za-z0-9+/=]+$") && data.length() > 20) {
                byte[] decoded = android.util.Base64.decode(data, android.util.Base64.DEFAULT);
                String decodedString = new String(decoded, "UTF-8");
                Log.d(TAG, "Base64 decoded successfully");
                return decodedString;
            }
        } catch (Exception e) {
            Log.d(TAG, "Not a valid Base64 string: " + e.getMessage());
        }
        return data;
    }
    
    /**
     * Parse comma-delimited format CCCD data
     * Note: Skip 9-digit CMND old numbers, only accept 12-digit CCCD
     * Smart parsing: Identify fields by pattern, not just position
     */
    private static CccdData parseCommaDelimitedFormat(String qrData, CccdData data) {
        try {
            String[] parts = qrData.split(",");
            Log.d(TAG, "Comma-delimited format: " + parts.length + " parts");
            for (int i = 0; i < parts.length; i++) {
                Log.d(TAG, "Part[" + i + "]: " + parts[i].trim());
            }
            
            // Smart field identification by pattern matching
            // First, find and set CCCD number (12 digits only, skip 9-digit CMND)
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("^\\d{12}$")) {
                    data.setIdNumber(trimmed);
                    Log.d(TAG, "Found CCCD number (12 digits): " + trimmed);
                    break;
                } else if (trimmed.matches("^\\d{9}$")) {
                    Log.d(TAG, "Skipping old CMND number (9 digits): " + trimmed);
                }
            }
            
            // Find name (contains Vietnamese characters or multiple words, not just numbers)
            for (String part : parts) {
                String trimmed = part.trim();
                // Skip if it's a number (CMND/CCCD or date)
                if (trimmed.matches("^\\d+$")) {
                    continue;
                }
                // Check if it looks like a Vietnamese name
                if (trimmed.matches(".*[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ].*") && 
                    trimmed.split("\\s+").length >= 2) {
                    data.setFullName(trimmed);
                    Log.d(TAG, "Found name: " + trimmed);
                    break;
                }
            }
            
            // Find date of birth (look for date patterns, usually 8 digits YYYYMMDD or DD/MM/YYYY)
            // Process dates in order: first date is date of birth, second is issue date
            boolean foundDateOfBirth = false;
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("\\d{8}") || trimmed.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}")) {
                    String formattedDate = formatDate(trimmed);
                    if (!foundDateOfBirth && isValidDate(formattedDate)) {
                        data.setDateOfBirth(formattedDate);
                        Log.d(TAG, "Found date of birth: " + trimmed + " -> " + formattedDate);
                        foundDateOfBirth = true;
                    } else if (foundDateOfBirth && isValidDate(formattedDate)) {
                        // This is the second date, likely issue date
                        data.setIssueDate(formattedDate);
                        Log.d(TAG, "Found issue date: " + trimmed + " -> " + formattedDate);
                        break;
                    }
                }
            }
            
            // Find gender (Nam/Nữ or M/F or 1/0)
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("(?i)^(Nam|Nữ|Male|Female|M|F|1|0)$")) {
                    data.setGender(formatGender(trimmed));
                    Log.d(TAG, "Found gender: " + trimmed);
                    break;
                }
            }
            
            // Find address (long text, not a number, not a date, not gender)
            for (String part : parts) {
                String trimmed = part.trim();
                // Skip if it's a number, date, gender, or name
                if (trimmed.matches("^\\d+$") || 
                    trimmed.matches("\\d{8}") || 
                    trimmed.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}") ||
                    trimmed.matches("(?i)^(Nam|Nữ|Male|Female|M|F|1|0)$") ||
                    (data.getFullName() != null && trimmed.equals(data.getFullName()))) {
                    continue;
                }
                // If it's a long text (likely address)
                if (trimmed.length() > 15) {
                    data.setPermanentAddress(trimmed);
                    Log.d(TAG, "Found address: " + trimmed);
                    break;
                }
            }

            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing comma-delimited format: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse JSON format CCCD data
     */
    private static CccdData parseJsonFormat(String qrData, CccdData data) {
        try {
            JSONObject json = new JSONObject(qrData);

            // Common field names in Vietnamese CCCD JSON
            if (json.has("hoTen") || json.has("fullName") || json.has("name")) {
                data.setFullName(json.optString("hoTen", 
                    json.optString("fullName", json.optString("name", ""))));
            }

            // Only accept 12-digit CCCD, ignore 9-digit CMND old numbers
            if (json.has("soCCCD")) {
                String id = json.optString("soCCCD", "");
                if (id.matches("^\\d{12}$")) {
                    data.setIdNumber(id);
                    Log.d(TAG, "Extracted CCCD from soCCCD: " + id);
                } else if (id.matches("^\\d{9}$")) {
                    Log.d(TAG, "Skipping old CMND number (9 digits) from soCCCD: " + id);
                }
            } else if (json.has("idNumber")) {
                String id = json.optString("idNumber", "");
                if (id.matches("^\\d{12}$")) {
                    data.setIdNumber(id);
                    Log.d(TAG, "Extracted CCCD from idNumber: " + id);
                } else if (id.matches("^\\d{9}$")) {
                    Log.d(TAG, "Skipping old CMND number (9 digits) from idNumber: " + id);
                }
            }
            // Note: We don't use "cmnd" field as it's the old format

            if (json.has("ngaySinh") || json.has("dateOfBirth") || json.has("birthDate")) {
                String dob = json.optString("ngaySinh", 
                    json.optString("dateOfBirth", json.optString("birthDate", "")));
                data.setDateOfBirth(formatDate(dob));
            }

            if (json.has("gioiTinh") || json.has("gender") || json.has("sex")) {
                String gender = json.optString("gioiTinh", 
                    json.optString("gender", json.optString("sex", "")));
                data.setGender(formatGender(gender));
            }

            // Permanent address (Nơi thường trú)
            if (json.has("diaChiThuongTru") || json.has("diaChi") || json.has("address") || json.has("queQuan")) {
                String address = json.optString("diaChiThuongTru", 
                    json.optString("diaChi", 
                        json.optString("address", json.optString("queQuan", ""))));
                data.setPermanentAddress(address);
            }

            // Issue date (Ngày cấp CCCD)
            if (json.has("ngayCap") || json.has("issueDate") || json.has("dateOfIssue")) {
                String issueDate = json.optString("ngayCap", 
                    json.optString("issueDate", json.optString("dateOfIssue", "")));
                data.setIssueDate(formatDate(issueDate));
            }

            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON format: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse pipe-delimited format CCCD data
     * Format: ID|Name|DOB|Gender|Address|...
     * Note: Skip 9-digit CMND old numbers, only accept 12-digit CCCD
     * Smart parsing: Identify fields by pattern, not just position
     */
    private static CccdData parsePipeDelimitedFormat(String qrData, CccdData data) {
        try {
            String[] parts = qrData.split("\\|");
            Log.d(TAG, "Pipe-delimited format: " + parts.length + " parts");
            for (int i = 0; i < parts.length; i++) {
                Log.d(TAG, "Part[" + i + "]: " + parts[i].trim());
            }
            
            // Smart field identification by pattern matching
            // First, find and set CCCD number (12 digits only, skip 9-digit CMND)
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("^\\d{12}$")) {
                    data.setIdNumber(trimmed);
                    Log.d(TAG, "Found CCCD number (12 digits): " + trimmed);
                    break;
                } else if (trimmed.matches("^\\d{9}$")) {
                    Log.d(TAG, "Skipping old CMND number (9 digits): " + trimmed);
                }
            }
            
            // Find name (contains Vietnamese characters or multiple words, not just numbers)
            for (String part : parts) {
                String trimmed = part.trim();
                // Skip if it's a number (CMND/CCCD or date)
                if (trimmed.matches("^\\d+$")) {
                    continue;
                }
                // Check if it looks like a Vietnamese name
                if (trimmed.matches(".*[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ].*") && 
                    trimmed.split("\\s+").length >= 2) {
                    data.setFullName(trimmed);
                    Log.d(TAG, "Found name: " + trimmed);
                    break;
                }
            }
            
            // Find date of birth (look for date patterns, usually 8 digits YYYYMMDD or DD/MM/YYYY)
            // Process dates in order: first date is date of birth, second is issue date
            boolean foundDateOfBirth = false;
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("\\d{8}") || trimmed.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}")) {
                    String formattedDate = formatDate(trimmed);
                    if (!foundDateOfBirth && isValidDate(formattedDate)) {
                        data.setDateOfBirth(formattedDate);
                        Log.d(TAG, "Found date of birth: " + trimmed + " -> " + formattedDate);
                        foundDateOfBirth = true;
                    } else if (foundDateOfBirth && isValidDate(formattedDate)) {
                        // This is the second date, likely issue date
                        data.setIssueDate(formattedDate);
                        Log.d(TAG, "Found issue date: " + trimmed + " -> " + formattedDate);
                        break;
                    }
                }
            }
            
            // Find gender (Nam/Nữ or M/F or 1/0)
            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.matches("(?i)^(Nam|Nữ|Male|Female|M|F|1|0)$")) {
                    data.setGender(formatGender(trimmed));
                    Log.d(TAG, "Found gender: " + trimmed);
                    break;
                }
            }
            
            // Find address (long text, not a number, not a date, not gender)
            for (String part : parts) {
                String trimmed = part.trim();
                // Skip if it's a number, date, gender, or name
                if (trimmed.matches("^\\d+$") || 
                    trimmed.matches("\\d{8}") || 
                    trimmed.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}") ||
                    trimmed.matches("(?i)^(Nam|Nữ|Male|Female|M|F|1|0)$") ||
                    (data.getFullName() != null && trimmed.equals(data.getFullName()))) {
                    continue;
                }
                // If it's a long text (likely address)
                if (trimmed.length() > 15) {
                    data.setPermanentAddress(trimmed);
                    Log.d(TAG, "Found address: " + trimmed);
                    break;
                }
            }

            return data;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing pipe-delimited format: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse using regex patterns to extract data
     */
    private static CccdData parseWithRegex(String qrData, CccdData data) {
        Log.d(TAG, "Attempting regex extraction");
        
        // Check if QR data is just a number (ID only format)
        // Only accept 12-digit CCCD, not 9-digit CMND
        if (qrData.matches("^\\d{12}$")) {
            Log.d(TAG, "QR data appears to be CCCD number only (12 digits): " + qrData);
            data.setIdNumber(qrData);
            return data;
        }
        
        // Extract CCCD number (12 digits only, ignore 9-digit CMND old numbers)
        // Priority: Look for 12-digit numbers first (CCCD), ignore 9-digit (CMND old)
        Pattern idPattern1 = Pattern.compile("\\b(\\d{12})\\b"); // CCCD: 12 digits
        Pattern idPattern2 = Pattern.compile("CCCD[\\s:]*?(\\d{12})", Pattern.CASE_INSENSITIVE);
        Pattern idPattern3 = Pattern.compile("soCCCD[\\s:]*?(\\d{12})", Pattern.CASE_INSENSITIVE);
        
        Matcher idMatcher = idPattern1.matcher(qrData);
        if (idMatcher.find()) {
            String id = idMatcher.group(1);
            data.setIdNumber(id);
            Log.d(TAG, "Extracted CCCD number (12 digits): " + id);
        } else {
            idMatcher = idPattern2.matcher(qrData);
            if (idMatcher.find()) {
                data.setIdNumber(idMatcher.group(1));
                Log.d(TAG, "Extracted CCCD number from CCCD pattern: " + idMatcher.group(1));
            } else {
                idMatcher = idPattern3.matcher(qrData);
                if (idMatcher.find()) {
                    data.setIdNumber(idMatcher.group(1));
                    Log.d(TAG, "Extracted CCCD number from soCCCD pattern: " + idMatcher.group(1));
                }
            }
        }
        
        // Note: We intentionally skip 9-digit numbers (old CMND format)

        // Extract date of birth (DD/MM/YYYY or DD-MM-YYYY or YYYYMMDD)
        // Priority: Look for 8-digit format first (YYYYMMDD - most common in Vietnamese CCCD)
        Pattern datePattern8Digit = Pattern.compile("\\b(\\d{8})\\b");
        Pattern datePattern1 = Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})\\b");
        Pattern datePattern3 = Pattern.compile("ngaySinh[\\s:]*?(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})", Pattern.CASE_INSENSITIVE);
        
        // First try 8-digit format (YYYYMMDD)
        Matcher dateMatcher = datePattern8Digit.matcher(qrData);
        if (dateMatcher.find()) {
            String date8Digit = dateMatcher.group(1);
            String formattedDate = formatDate(date8Digit);
            if (!formattedDate.equals(date8Digit) && isValidDate(formattedDate)) { // If successfully formatted
                data.setDateOfBirth(formattedDate);
                Log.d(TAG, "Extracted date of birth from 8-digit format: " + date8Digit + " -> " + formattedDate);
            } else {
                // Try other patterns
                dateMatcher = datePattern1.matcher(qrData);
                if (dateMatcher.find()) {
                    String date = dateMatcher.group(1) + "/" + dateMatcher.group(2) + "/" + dateMatcher.group(3);
                    String formatted = formatDate(date);
                    if (isValidDate(formatted)) {
                        data.setDateOfBirth(formatted);
                        Log.d(TAG, "Extracted date of birth: " + date + " -> " + formatted);
                    }
                }
            }
        } else {
            dateMatcher = datePattern1.matcher(qrData);
            if (dateMatcher.find()) {
                String date = dateMatcher.group(1) + "/" + dateMatcher.group(2) + "/" + dateMatcher.group(3);
                String formatted = formatDate(date);
                if (isValidDate(formatted)) {
                    data.setDateOfBirth(formatted);
                    Log.d(TAG, "Extracted date of birth: " + date + " -> " + formatted);
                }
            } else {
                dateMatcher = datePattern3.matcher(qrData);
                if (dateMatcher.find()) {
                    String date = dateMatcher.group(1) + "/" + dateMatcher.group(2) + "/" + dateMatcher.group(3);
                    String formatted = formatDate(date);
                    if (isValidDate(formatted)) {
                        data.setDateOfBirth(formatted);
                        Log.d(TAG, "Extracted date of birth from ngaySinh pattern: " + date + " -> " + formatted);
                    }
                }
            }
        }

        // Extract issue date (look for second date, usually after date of birth)
        // Try 8-digit format first
        Pattern issueDatePattern8Digit = Pattern.compile("\\b(\\d{8})\\b");
        Matcher issueDateMatcher = issueDatePattern8Digit.matcher(qrData);
        int dateOfBirthEnd = -1;
        if (data.getDateOfBirth() != null) {
            // Find position after date of birth
            String dobSearch = data.getDateOfBirth().replace("/", "");
            int dobPos = qrData.indexOf(dobSearch);
            if (dobPos >= 0) {
                dateOfBirthEnd = dobPos + dobSearch.length();
            }
        }
        
        // Look for second 8-digit date after date of birth
        while (issueDateMatcher.find()) {
            int matchStart = issueDateMatcher.start();
            // If we found date of birth, look for issue date after it
            if (dateOfBirthEnd < 0 || matchStart > dateOfBirthEnd) {
                String date8Digit = issueDateMatcher.group(1);
                String formattedDate = formatDate(date8Digit);
                if (!formattedDate.equals(date8Digit) && isValidDate(formattedDate)) { // If successfully formatted
                    data.setIssueDate(formattedDate);
                    Log.d(TAG, "Extracted issue date from 8-digit format: " + date8Digit + " -> " + formattedDate);
                    break;
                }
            }
        }
        
        // Also try pattern with ngayCap prefix
        Pattern issueDatePattern = Pattern.compile("ngayCap[\\s:]*?(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})", Pattern.CASE_INSENSITIVE);
        issueDateMatcher = issueDatePattern.matcher(qrData);
        if (issueDateMatcher.find() && data.getIssueDate() == null) {
            String issueDate = issueDateMatcher.group(1) + "/" + issueDateMatcher.group(2) + "/" + issueDateMatcher.group(3);
            String formatted = formatDate(issueDate);
            if (isValidDate(formatted)) {
                data.setIssueDate(formatted);
                Log.d(TAG, "Extracted issue date from ngayCap pattern: " + issueDate + " -> " + formatted);
            }
        }

        // Try to extract name (Vietnamese names with diacritics)
        Pattern namePattern1 = Pattern.compile("hoTen[\\s:]*?([A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+(?:\\s+[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+)*)", Pattern.CASE_INSENSITIVE);
        Pattern namePattern2 = Pattern.compile("([A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+(?:\\s+[A-ZÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ][a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]+)+)");
        
        Matcher nameMatcher = namePattern1.matcher(qrData);
        if (nameMatcher.find()) {
            data.setFullName(nameMatcher.group(1).trim());
            Log.d(TAG, "Extracted full name from hoTen pattern: " + nameMatcher.group(1));
        } else {
            nameMatcher = namePattern2.matcher(qrData);
            if (nameMatcher.find()) {
                String name = nameMatcher.group(1).trim();
                // Only use if it's not just a single word and looks like a name
                if (name.split("\\s+").length >= 2) {
                    data.setFullName(name);
                    Log.d(TAG, "Extracted full name: " + name);
                }
            }
        }

        // Extract gender
        Pattern genderPattern = Pattern.compile("gioiTinh[\\s:]*?(Nam|Nữ|Male|Female|M|F|1|0)", Pattern.CASE_INSENSITIVE);
        Matcher genderMatcher = genderPattern.matcher(qrData);
        if (genderMatcher.find()) {
            data.setGender(formatGender(genderMatcher.group(1)));
            Log.d(TAG, "Extracted gender: " + genderMatcher.group(1));
        }

        // Extract address
        Pattern addressPattern = Pattern.compile("diaChiThuongTru[\\s:]*?([^|,\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher addressMatcher = addressPattern.matcher(qrData);
        if (addressMatcher.find()) {
            data.setPermanentAddress(addressMatcher.group(1).trim());
            Log.d(TAG, "Extracted address: " + addressMatcher.group(1));
        }

        Log.d(TAG, "Regex extraction complete. ID: " + data.getIdNumber() + ", Name: " + data.getFullName());
        return data;
    }

    /**
     * Format date to DD/MM/YYYY
     * Handles various date formats including YYYYMMDD (8 digits)
     */
    private static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }

        dateStr = dateStr.trim();
        
        // Handle 8-digit format (YYYYMMDD or DDMMYYYY)
        if (dateStr.matches("^\\d{8}$")) {
            try {
                // Try YYYYMMDD first (most common in Vietnamese CCCD)
                String year = dateStr.substring(0, 4);
                String month = dateStr.substring(4, 6);
                String day = dateStr.substring(6, 8);
                
                int yearInt = Integer.parseInt(year);
                int monthInt = Integer.parseInt(month);
                int dayInt = Integer.parseInt(day);
                
                // Validate: year should be between 1900 and current year + 1
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (yearInt >= 1900 && yearInt <= currentYear + 1 && 
                    monthInt >= 1 && monthInt <= 12 && 
                    dayInt >= 1 && dayInt <= 31) {
                    // Valid YYYYMMDD format
                    return String.format(Locale.getDefault(), "%02d/%02d/%04d", dayInt, monthInt, yearInt);
                }
                
                // Try DDMMYYYY format
                day = dateStr.substring(0, 2);
                month = dateStr.substring(2, 4);
                year = dateStr.substring(4, 8);
                
                dayInt = Integer.parseInt(day);
                monthInt = Integer.parseInt(month);
                yearInt = Integer.parseInt(year);
                
                if (yearInt >= 1900 && yearInt <= currentYear + 1 && 
                    monthInt >= 1 && monthInt <= 12 && 
                    dayInt >= 1 && dayInt <= 31) {
                    // Valid DDMMYYYY format
                    return String.format(Locale.getDefault(), "%02d/%02d/%04d", dayInt, monthInt, yearInt);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing 8-digit date: " + dateStr, e);
            }
        }

        // Try different date formats
        String[] formats = {
            "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", 
            "dd/MM/yy", "dd-MM-yy", "yyyyMMdd"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                sdf.setLenient(false); // Strict parsing
                Date date = sdf.parse(dateStr);
                
                // Validate date is reasonable (between 1900 and 2100)
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                int year = Integer.parseInt(yearFormat.format(date));
                if (year >= 1900 && year <= 2100) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // Try next format
            }
        }

        Log.w(TAG, "Could not parse date: " + dateStr);
        return dateStr; // Return original if can't parse
    }

    /**
     * Format gender to Vietnamese format
     */
    private static String formatGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            return "";
        }

        String lower = gender.toLowerCase();
        if (lower.contains("nam") || lower.contains("male") || lower.equals("1") || lower.equals("m")) {
            return "Nam";
        } else if (lower.contains("nữ") || lower.contains("nu") || lower.contains("female") || lower.equals("0") || lower.equals("f")) {
            return "Nữ";
        }

        return gender; // Return original if can't determine
    }
    
    /**
     * Validate if a date string is reasonable (between 1900 and 2100)
     */
    private static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        
        try {
            // Try to parse as dd/MM/yyyy
            if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] parts = dateStr.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                
                // Validate reasonable range
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                if (year >= 1900 && year <= currentYear + 1 && 
                    month >= 1 && month <= 12 && 
                    day >= 1 && day <= 31) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating date: " + dateStr, e);
        }
        
        return false;
    }
}


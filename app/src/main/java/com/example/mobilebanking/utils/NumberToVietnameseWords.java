package com.example.mobilebanking.utils;

public class NumberToVietnameseWords {
    
    private static final String[] ones = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] teens = {"mười", "mười một", "mười hai", "mười ba", "mười bốn", "mười lăm", 
                                           "mười sáu", "mười bảy", "mười tám", "mười chín"};
    
    public static String convert(long number) {
        if (number == 0) {
            return "Không đồng";
        }
        
        if (number < 0) {
            return "Số âm không hợp lệ";
        }
        
        String result = convertNumber(number);
        return capitalize(result.trim()) + " đồng";
    }
    
    private static String convertNumber(long number) {
        if (number == 0) {
            return "";
        }
        
        if (number < 10) {
            return ones[(int) number];
        }
        
        if (number < 20) {
            return teens[(int) (number - 10)];
        }
        
        if (number < 100) {
            int tens = (int) (number / 10);
            int remainder = (int) (number % 10);
            String result = ones[tens] + " mươi";
            if (remainder == 1) {
                result += " mốt";
            } else if (remainder == 5 && tens > 1) {
                result += " lăm";
            } else if (remainder > 0) {
                result += " " + ones[remainder];
            }
            return result;
        }
        
        if (number < 1000) {
            int hundreds = (int) (number / 100);
            int remainder = (int) (number % 100);
            String result = ones[hundreds] + " trăm";
            if (remainder > 0) {
                if (remainder < 10) {
                    result += " lẻ " + ones[remainder];
                } else {
                    result += " " + convertNumber(remainder);
                }
            }
            return result;
        }
        
        if (number < 1000000) {
            int thousands = (int) (number / 1000);
            int remainder = (int) (number % 1000);
            String result = convertNumber(thousands) + " nghìn";
            if (remainder > 0) {
                if (remainder < 100) {
                    result += " lẻ " + convertNumber(remainder);
                } else {
                    result += " " + convertNumber(remainder);
                }
            }
            return result;
        }
        
        if (number < 1000000000) {
            int millions = (int) (number / 1000000);
            int remainder = (int) (number % 1000000);
            String result = convertNumber(millions) + " triệu";
            if (remainder > 0) {
                if (remainder < 1000) {
                    result += " lẻ " + convertNumber(remainder);
                } else {
                    result += " " + convertNumber(remainder);
                }
            }
            return result;
        }
        
        if (number < 1000000000000L) {
            long billions = number / 1000000000;
            long remainder = number % 1000000000;
            String result = convertNumber(billions) + " tỷ";
            if (remainder > 0) {
                if (remainder < 1000000) {
                    result += " lẻ " + convertNumber(remainder);
                } else {
                    result += " " + convertNumber(remainder);
                }
            }
            return result;
        }
        
        return "Số quá lớn";
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

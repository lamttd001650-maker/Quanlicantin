package com.javaweb.util;

/**
 * ValidationUtil - Utility class for form and data validation
 */
public class ValidationUtil {

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String e = email.trim();
        // Quick acceptance: if it's a gmail address accept regardless of local-part format
        if (e.toLowerCase().endsWith("@gmail.com")) {
            return true;
        }
        return e.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validate username format
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.length() >= 3 && username.length() <= 50 
               && username.matches("^[A-Za-z0-9_-]+$");
    }

    /**
     * Validate phone number (Vietnam format)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String p = phone.trim();
        // Require exactly 10 digits and starting with 0 (e.g., 0912345678)
        return p.matches("^0\\d{9}$");
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 6 && password.length() <= 100;
    }

    /**
     * Validate student code format
     */
    public static boolean isValidStudentCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return code.matches("^SV[0-9]{3,4}$|^[A-Z]{2}[0-9]{3,4}$");
    }

    /**
     * Validate staff code format
     */
    public static boolean isValidStaffCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        return code.matches("^NV[0-9]{3,4}$|^[A-Z]{2}[0-9]{3,4}$");
    }

    /**
     * Sanitize input to prevent XSS
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Validate not null or empty
     */
    public static boolean isNotEmpty(String... values) {
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get validation error message
     */
    public static String getValidationError(String fieldName, String error) {
        switch (error) {
            case "empty":
                return "Vui lòng nhập " + fieldName;
            case "invalid":
                return fieldName + " không hợp lệ";
            case "exists":
                return fieldName + " đã tồn tại";
            case "mismatch":
                return fieldName + " không khớp";
            default:
                return "Lỗi: " + fieldName;
        }
    }

    public static void main(String[] args) {
        // Test validation
        System.out.println("Email valid: " + isValidEmail("test@example.com"));
        System.out.println("Username valid: " + isValidUsername("user123"));
        System.out.println("Phone valid: " + isValidPhone("0912345678"));
        System.out.println("Student code valid: " + isValidStudentCode("SV001"));
        System.out.println("Staff code valid: " + isValidStaffCode("NV001"));
    }
}

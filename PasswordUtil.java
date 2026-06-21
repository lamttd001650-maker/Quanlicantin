package com.javaweb.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil - Utility class for password hashing
 */
public class PasswordUtil {

    /**
     * Hash password using MD5
     * Note: MD5 is not recommended for production. Use BCrypt instead.
     */
    public static String hashPasswordMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());

            // Convert byte array to hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }

    /**
     * Verify password matches hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return hashPasswordMD5(plainPassword).equals(hashedPassword);
    }

    /**
     * Validate password strength
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // At least one uppercase, one lowercase, one digit
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        
        return hasUpperCase && hasLowerCase && hasDigit;
    }

    public static void main(String[] args) {
        // Test password hashing
        String password = "admin123";
        String hashed = hashPasswordMD5(password);
        System.out.println("Original: " + password);
        System.out.println("Hashed: " + hashed);
        System.out.println("Verify: " + verifyPassword(password, hashed));
    }
}

package com.javaweb.servlet;

import com.javaweb.util.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LoginServlet - Handles user login for Admin, Staff, and Customer
 * Uses SQL Server and MD5 password hashing
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = request.getSession();
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String loginType = request.getParameter("loginType");

        // Trim input
        if (username != null) username = username.trim();
        if (password != null) password = password.trim();

        // Validate input
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập và mật khẩu!");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return;
        }

        try {
            LoginResult result = validateLogin(username, password, loginType);
            
            if (result.isSuccess()) {
                HttpSession session = request.getSession();
                session.setAttribute("userId", result.getUserId());
                session.setAttribute("username", username);
                session.setAttribute("user", username); // For compatibility
                session.setAttribute("role", result.getRole());
                session.setAttribute("fullName", result.getFullName());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
                
                // Redirect based on role
                String redirectPath = switch (result.getRole()) {
                    case "ADMIN" -> "/admin";
                    case "STAFF" -> "/staff";
                    case "CUSTOMER" -> "/home";
                    default -> "/login";
                };
                response.sendRedirect(request.getContextPath() + redirectPath);
            } else {
                request.setAttribute("error", result.getErrorMessage());
                request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
        }
    }

    private LoginResult validateLogin(String username, String password, String loginType) {
        if (loginType == null || loginType.isEmpty()) {
            return new LoginResult(false, "Vui lòng chọn loại đăng nhập!", null, null, null);
        }

        String hashedPassword = md5Hash(password);

        try {
            switch (loginType.toLowerCase()) {
                case "admin":
                    return validateAdminLogin(username, hashedPassword);
                case "staff":
                    return validateStaffLogin(username, hashedPassword);
                case "customer":
                    return validateCustomerLogin(username, hashedPassword);
                default:
                    return new LoginResult(false, "Loại đăng nhập không hợp lệ!", null, null, null);
            }
        } catch (SQLException e) {
            return new LoginResult(false, "Lỗi kết nối database: " + e.getMessage(), null, null, null);
        }
    }

    private LoginResult validateAdminLogin(String username, String hashedPassword) throws SQLException {
        String query = "SELECT admin_id, full_name, status FROM admin_users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        return new LoginResult(true, null, 
                            String.valueOf(rs.getInt("admin_id")), 
                            "ADMIN", 
                            rs.getString("full_name"));
                    } else {
                        return new LoginResult(false, "Tài khoản đã bị khóa!", null, null, null);
                    }
                }
            }
        }
        return new LoginResult(false, "Tên đăng nhập admin hoặc mật khẩu không chính xác!", null, null, null);
    }

    private LoginResult validateStaffLogin(String username, String hashedPassword) throws SQLException {
        String query = "SELECT staff_id, full_name, status FROM staff_members WHERE (username = ? OR staff_code = ?) AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        return new LoginResult(true, null, 
                            String.valueOf(rs.getInt("staff_id")), 
                            "STAFF", 
                            rs.getString("full_name"));
                    } else {
                        return new LoginResult(false, "Tài khoản đã bị khóa!", null, null, null);
                    }
                }
            }
        }
        return new LoginResult(false, "Tên đăng nhập nhân viên hoặc mật khẩu không chính xác!", null, null, null);
    }

    private LoginResult validateCustomerLogin(String username, String hashedPassword) throws SQLException {
        String query = "SELECT customer_id, full_name, status FROM customers WHERE (username = ? OR student_code = ?) AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        return new LoginResult(true, null, 
                            String.valueOf(rs.getInt("customer_id")), 
                            "CUSTOMER", 
                            rs.getString("full_name"));
                    } else {
                        return new LoginResult(false, "Tài khoản đã bị khóa!", null, null, null);
                    }
                }
            }
        }
        return new LoginResult(false, "Mã sinh viên hoặc mật khẩu không chính xác!", null, null, null);
    }

    /**
     * MD5 hash function
     */
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Inner class to represent login result
     */
    private static class LoginResult {
        private final boolean success;
        private final String errorMessage;
        private final String userId;
        private final String role;
        private final String fullName;

        public LoginResult(boolean success, String errorMessage, String userId, String role, String fullName) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.userId = userId;
            this.role = role;
            this.fullName = fullName;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public String getUserId() { return userId; }
        public String getRole() { return role; }
        public String getFullName() { return fullName; }
    }
}

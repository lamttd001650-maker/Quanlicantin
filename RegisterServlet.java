package com.javaweb.servlet;

import com.javaweb.util.DatabaseConnection;
import com.javaweb.util.PasswordUtil;
import com.javaweb.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RegisterServlet - Handles user registration requests
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String username = trim(request.getParameter("username"));
            String email = trim(request.getParameter("email"));
            String fullName = trim(request.getParameter("fullName"));
            String phone = trim(request.getParameter("phone"));
            String password = request.getParameter("password");
            String passwordConfirm = request.getParameter("passwordConfirm");
            String agreeTerms = request.getParameter("agreeTerms");
            String dateOfBirthStr = trim(request.getParameter("dateOfBirth"));
            String className = trim(request.getParameter("className"));
            String studentCode = trim(request.getParameter("studentCode"));

            if (!validateRegistrationData(username, email, fullName, phone, password, passwordConfirm, agreeTerms, dateOfBirthStr, className, studentCode, request, response)) {
                return;
            }

            boolean registrationSaved = saveCustomerAccount(username, email, fullName, phone, password, dateOfBirthStr, className, studentCode, request);

            if (registrationSaved) {
                request.setAttribute("success", "Đăng kí thành công! Bạn có thể đăng nhập ngay bằng tên đăng nhập và mật khẩu vừa tạo.");
            } else if (request.getAttribute("error") == null) {
                request.setAttribute("error", "Lỗi khi lưu đơn đăng kí. Vui lòng thử lại!");
            }

            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean validateRegistrationData(String username, String email, String fullName, String phone,
                                            String password, String passwordConfirm, String agreeTerms,
                                            String dateOfBirthStr, String className, String studentCode,
                                            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!ValidationUtil.isValidUsername(username)) {
            request.setAttribute("fieldError_username", "Tên đăng nhập phải có từ 3 ký tự và chỉ chứa chữ, số, -, _!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("fieldError_email", "Email không hợp lệ!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (fullName == null || fullName.isEmpty()) {
            request.setAttribute("fieldError_fullName", "Vui lòng nhập họ tên!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (phone == null || phone.isEmpty()) {
            request.setAttribute("fieldError_phone", "Vui lòng nhập số điện thoại!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("fieldError_phone", "Số điện thoại phải đủ 10 chữ số và bắt đầu bằng 0!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            request.setAttribute("fieldError_password", "Mật khẩu phải có ít nhất 6 ký tự!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            request.setAttribute("fieldError_passwordConfirm", "Mật khẩu xác nhận không khớp!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (agreeTerms == null || agreeTerms.isEmpty()) {
            request.setAttribute("fieldError_agreeTerms", "Vui lòng đồng ý với các điều khoản dịch vụ!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        if (dateOfBirthStr == null || dateOfBirthStr.isEmpty()) {
            request.setAttribute("fieldError_dateOfBirth", "Vui lòng nhập ngày sinh!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            request.setAttribute("showRegisterTab", "true");
            request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
            return false;
        }

        return true;
    }

    private void setRegistrationValues(HttpServletRequest request, String username, String email, String fullName, String phone, String dateOfBirthStr, String className, String studentCode) {
        request.setAttribute("val_username", username);
        request.setAttribute("val_email", email);
        request.setAttribute("val_fullName", fullName);
        request.setAttribute("val_phone", phone);
        request.setAttribute("val_dateOfBirth", dateOfBirthStr);
        request.setAttribute("val_className", className);
        request.setAttribute("val_studentCode", studentCode);
    }

    private boolean saveCustomerAccount(String username, String email, String fullName, String phone,
                                       String password, String dateOfBirthStr, String className, String studentCode, HttpServletRequest request) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (isValueTaken(conn, "SELECT COUNT(*) FROM admin_users WHERE LOWER(username) = ?", username.toLowerCase())
                    || isValueTaken(conn, "SELECT COUNT(*) FROM customers WHERE LOWER(username) = ?", username.toLowerCase())
                    || isValueTaken(conn, "SELECT COUNT(*) FROM staff_members WHERE LOWER(username) = ?", username.toLowerCase())) {
                request.setAttribute("saveError", true);
                request.setAttribute("fieldError_username", "Tên đăng nhập đã tồn tại.");
                setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
                return false;
            }

            if (isValueTaken(conn, "SELECT COUNT(*) FROM admin_users WHERE LOWER(email) = ?", email.toLowerCase())
                    || isValueTaken(conn, "SELECT COUNT(*) FROM customers WHERE LOWER(email) = ?", email.toLowerCase())
                    || isValueTaken(conn, "SELECT COUNT(*) FROM staff_members WHERE LOWER(email) = ?", email.toLowerCase())) {
                request.setAttribute("saveError", true);
                request.setAttribute("fieldError_email", "Email đã tồn tại.");
                setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
                return false;
            }

            String insertCustomer = "INSERT INTO customers (username, password, email, full_name, phone, student_code, date_of_birth, [class], balance, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0.00, 'ACTIVE')";
            try (PreparedStatement stmt = conn.prepareStatement(insertCustomer)) {
                stmt.setString(1, username);
                stmt.setString(2, PasswordUtil.hashPasswordMD5(password));
                stmt.setString(3, email);
                stmt.setString(4, fullName);
                stmt.setString(5, phone);
                stmt.setString(6, studentCode);
                stmt.setDate(7, java.sql.Date.valueOf(dateOfBirthStr));
                stmt.setString(8, className);
                int updated = stmt.executeUpdate();
                return updated > 0;
            }
        } catch (IllegalArgumentException ex) {
            request.setAttribute("saveError", true);
            request.setAttribute("fieldError_dateOfBirth", "Ngày sinh không hợp lệ!");
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("saveError", true);
            String sqlError = e.getMessage();
            if (sqlError != null && sqlError.toLowerCase().contains("unique")) {
                if (sqlError.toLowerCase().contains("username")) {
                    request.setAttribute("fieldError_username", "Tên đăng nhập đã tồn tại.");
                } else if (sqlError.toLowerCase().contains("email")) {
                    request.setAttribute("fieldError_email", "Email đã tồn tại.");
                } else {
                    request.setAttribute("error", "Lỗi trùng dữ liệu: " + sqlError);
                }
            } else {
                request.setAttribute("error", "Lỗi khi lưu đơn đăng kí: " + sqlError);
            }
            setRegistrationValues(request, username, email, fullName, phone, dateOfBirthStr, className, studentCode);
            return false;
        }
    }

    private boolean isValueTaken(Connection conn, String query, String value) throws SQLException {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}

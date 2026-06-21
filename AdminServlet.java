package com.javaweb.servlet;

import com.javaweb.model.Customer;
import com.javaweb.model.Staff;
import com.javaweb.util.DatabaseConnection;
import com.javaweb.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminServlet - Quản lý người dùng (Xem, Khóa, Xóa tài khoản)
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private static List<Staff> staffList = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        // Khởi tạo dữ liệu mẫu nhân viên
        initSampleData();
    }

    /**
     * Khởi tạo dữ liệu mẫu
     */
    private void initSampleData() {
        if (staffList.isEmpty()) {
            Staff s1 = new Staff("STAFF001", "password123", "staff001@canteen.edu.vn", "Lê Văn C");
            s1.setUserId(1);
            s1.setStaffCode("NV001");
            s1.setPosition("Quản lý bán hàng");
            s1.setDepartment("Kinh doanh");
            s1.setStatus("ACTIVE");
            staffList.add(s1);

            Staff s2 = new Staff("STAFF002", "password123", "staff002@canteen.edu.vn", "Phạm Thị D");
            s2.setUserId(2);
            s2.setStaffCode("NV002");
            s2.setPosition("Thu ngân");
            s2.setDepartment("Tài chính");
            s2.setStatus("ACTIVE");
            staffList.add(s2);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        // Check if logged in
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        // Only ADMIN or STAFF can access
        if (!("ADMIN".equals(role) || "STAFF".equals(role))) {
            response.sendError(403, "Bạn không có quyền truy cập!");
            return;
        }

        String action = request.getParameter("action");

        if (action == null) {
            // Dashboard admin
            showAdminDashboard(request, response);
        } else if ("customers".equals(action)) {
            // Quản lý khách hàng
            listCustomers(request, response);
        } else if ("staff".equals(action)) {
            // Quản lý nhân viên
            listStaff(request, response);
        } else if ("view_customer".equals(action)) {
            // Xem chi tiết khách hàng
            viewCustomer(request, response);
        } else if ("view_staff".equals(action)) {
            // Xem chi tiết nhân viên
            viewStaff(request, response);
        } else if ("edit_staff".equals(action)) {
            // Chỉnh sửa thông tin nhân viên
            showEditStaffForm(request, response);
        } else if ("add_staff".equals(action)) {
            // Form thêm nhân viên mới
            showAddStaffForm(request, response);
        } else if ("block".equals(action)) {
            // Khóa tài khoản
            blockUser(request, response);
        } else if ("unblock".equals(action)) {
            // Mở khóa tài khoản
            unblockUser(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("delete_customer".equals(action)) {
            deleteCustomer(request, response);
        } else if ("delete_staff".equals(action)) {
            deleteStaff(request, response);
        } else if ("save_staff".equals(action)) {
            saveStaff(request, response);
        }
    }

    /**
     * Hiển thị dashboard admin
     */
    private void showAdminDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Thống kê tổng quan
        List<Customer> dbCustomers = loadCustomersFromDatabase();
        int totalCustomers = dbCustomers.size();
        int totalStaff = staffList.size();
        int activeCustomers = (int) dbCustomers.stream().filter(c -> "ACTIVE".equals(c.getStatus())).count();
        int activeStaff = (int) staffList.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();

        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("totalStaff", totalStaff);
        request.setAttribute("activeCustomers", activeCustomers);
        request.setAttribute("activeStaff", activeStaff);

        request.getRequestDispatcher("/views/canteen/admin_dashboard.jsp").forward(request, response);
    }

    /**
     * Liệt kê danh sách khách hàng
     */
    private void listCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Customer> dbCustomers = loadCustomersFromDatabase();
        request.setAttribute("customers", dbCustomers);
        request.getRequestDispatcher("/views/canteen/admin_customers.jsp").forward(request, response);
    }

    /**
     * Liệt kê danh sách nhân viên
     */
    private void listStaff(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Staff> dbStaff = loadStaffFromDatabase();
        request.setAttribute("staffList", dbStaff);
        request.getRequestDispatcher("/views/canteen/admin_staff.jsp").forward(request, response);
    }

    /**
     * Load all customers from the database
     */
    private List<Customer> loadCustomersFromDatabase() {
        List<Customer> dbCustomers = new ArrayList<>();
        String sql = "SELECT customer_id, username, password, email, full_name, phone, student_code, date_of_birth, address, [class], balance, status " +
                "FROM customers ORDER BY customer_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dbCustomers.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbCustomers;
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setUserId(rs.getInt("customer_id"));
        customer.setUsername(rs.getString("username"));
        customer.setPassword(rs.getString("password"));
        customer.setEmail(rs.getString("email"));
        customer.setFullName(rs.getString("full_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setStudentCode(rs.getString("student_code"));
        java.sql.Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            customer.setDateOfBirth(dob.toLocalDate());
        }
        customer.setAddress(rs.getString("address"));
        customer.setClassName(rs.getString("class"));
        customer.setBalance(rs.getDouble("balance"));
        customer.setStatus(rs.getString("status"));
        customer.setRole("CUSTOMER");
        return customer;
    }

    private boolean updateCustomerStatus(int customerId, String status) {
        String sql = "UPDATE customers SET status = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteCustomerById(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Customer findCustomerById(int customerId) {
        String sql = "SELECT customer_id, username, password, email, full_name, phone, student_code, date_of_birth, address, [class], balance, status " +
                "FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Xem chi tiết khách hàng
     */
    private void viewCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int customerId = Integer.parseInt(request.getParameter("id"));
        Customer customer = findCustomerById(customerId);
        if (customer != null) {
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/views/canteen/admin_customer_detail.jsp").forward(request, response);
        } else {
            response.sendError(404, "Khách hàng không tồn tại");
        }
    }

    /**
     * Xem chi tiết nhân viên
     */
    private void viewStaff(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int staffId = Integer.parseInt(request.getParameter("id"));
        Staff staff = findStaffById(staffId);
        if (staff != null) {
            request.setAttribute("staff", staff);
            request.getRequestDispatcher("/views/canteen/admin_staff_detail.jsp").forward(request, response);
        } else {
            response.sendError(404, "Nhân viên không tồn tại");
        }
    }

    /**
     * Khóa tài khoản
     */
    private void blockUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userType = request.getParameter("userType");
        String actionTarget = "customers";
        if ("staff".equals(userType)) {
            actionTarget = "staff";
        }
        String redirectUrl = request.getContextPath() + "/admin?action=" + actionTarget;

        try {
            int userId = Integer.parseInt(request.getParameter("id"));

            if ("customer".equals(userType)) {
                if (updateCustomerStatus(userId, "INACTIVE")) {
                    String success = URLEncoder.encode("Khóa tài khoản khách hàng thành công!", StandardCharsets.UTF_8);
                    redirectUrl += "&success=" + success;
                } else {
                    String error = URLEncoder.encode("Không tìm thấy khách hàng để khóa.", StandardCharsets.UTF_8);
                    redirectUrl += "&error=" + error;
                }
            } else if ("staff".equals(userType)) {
                if (updateStaffStatus(userId, "INACTIVE")) {
                    String success = URLEncoder.encode("Khóa tài khoản nhân viên thành công!", StandardCharsets.UTF_8);
                    redirectUrl += "&success=" + success;
                } else {
                    String error = URLEncoder.encode("Không tìm thấy nhân viên để khóa.", StandardCharsets.UTF_8);
                    redirectUrl += "&error=" + error;
                }
            }
        } catch (Exception e) {
            String error = URLEncoder.encode("Lỗi: " + e.getMessage(), StandardCharsets.UTF_8);
            redirectUrl += "&error=" + error;
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Mở khóa tài khoản
     */
    private void unblockUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userType = request.getParameter("userType");
        String actionTarget = "customers";
        if ("staff".equals(userType)) {
            actionTarget = "staff";
        }
        String redirectUrl = request.getContextPath() + "/admin?action=" + actionTarget;

        try {
            int userId = Integer.parseInt(request.getParameter("id"));

            if ("customer".equals(userType)) {
                if (updateCustomerStatus(userId, "ACTIVE")) {
                    String success = URLEncoder.encode("Mở khóa tài khoản khách hàng thành công!", StandardCharsets.UTF_8);
                    redirectUrl += "&success=" + success;
                } else {
                    String error = URLEncoder.encode("Không tìm thấy khách hàng để mở khóa.", StandardCharsets.UTF_8);
                    redirectUrl += "&error=" + error;
                }
            } else if ("staff".equals(userType)) {
                if (updateStaffStatus(userId, "ACTIVE")) {
                    String success = URLEncoder.encode("Mở khóa tài khoản nhân viên thành công!", StandardCharsets.UTF_8);
                    redirectUrl += "&success=" + success;
                } else {
                    String error = URLEncoder.encode("Không tìm thấy nhân viên để mở khóa.", StandardCharsets.UTF_8);
                    redirectUrl += "&error=" + error;
                }
            }
        } catch (Exception e) {
            String error = URLEncoder.encode("Lỗi: " + e.getMessage(), StandardCharsets.UTF_8);
            redirectUrl += "&error=" + error;
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Xóa khách hàng
     */
    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int customerId = Integer.parseInt(request.getParameter("id"));
            if (deleteCustomerById(customerId)) {
                request.setAttribute("success", "Xóa khách hàng thành công!");
            } else {
                request.setAttribute("error", "Không tìm thấy khách hàng để xóa.");
            }
            listCustomers(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            listCustomers(request, response);
        }
    }

    /**
     * Xóa nhân viên
     */
    private void deleteStaff(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int staffId = Integer.parseInt(request.getParameter("id"));
            if (deleteStaffById(staffId)) {
                request.setAttribute("success", "Xóa nhân viên thành công!");
            } else {
                request.setAttribute("error", "Không tìm thấy nhân viên để xóa.");
            }
            listStaff(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            listStaff(request, response);
        }
    }

    private boolean updateStaffStatus(int staffId, String status) {
        String sql = "UPDATE staff_members SET status = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, staffId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteStaffById(int staffId) {
        String sql = "DELETE FROM staff_members WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tìm nhân viên theo ID
     */
    private Staff findStaffById(int staffId) {
        String sql = "SELECT staff_id, username, password, email, full_name, phone, staff_code, position, salary, department, status "  +
                "FROM staff_members WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapStaff(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load all staff from the database
     */
    private List<Staff> loadStaffFromDatabase() {
        List<Staff> dbStaff = new ArrayList<>();
        String sql = "SELECT staff_id, username, password, email, full_name, phone, staff_code, position, salary, department, status "  +
                "FROM staff_members ORDER BY staff_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dbStaff.add(mapStaff(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbStaff;
    }

    private Staff mapStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setUserId(rs.getInt("staff_id"));
        staff.setUsername(rs.getString("username"));
        staff.setPassword(rs.getString("password"));
        staff.setEmail(rs.getString("email"));
        staff.setFullName(rs.getString("full_name"));
        staff.setPhone(rs.getString("phone"));
        staff.setStaffCode(rs.getString("staff_code"));
        staff.setPosition(rs.getString("position"));
        staff.setSalary(rs.getDouble("salary"));
        staff.setDepartment(rs.getString("department"));
        staff.setStatus(rs.getString("status"));
        staff.setRole("STAFF");
        return staff;
    }

    private void showAddStaffForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Thêm nhân viên mới");
        request.setAttribute("formAction", "save_staff");
        request.setAttribute("staff", new Staff());
        request.getRequestDispatcher("/views/canteen/admin_staff_form.jsp").forward(request, response);
    }

    private void saveStaff(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String fullName = request.getParameter("fullName");
            String staffCode = request.getParameter("staffCode");
            String position = request.getParameter("position");
            String department = request.getParameter("department");
            double salary = 0;
            try {
                String salaryStr = request.getParameter("salary");
                if (salaryStr != null && !salaryStr.trim().isEmpty()) {
                    salary = Double.parseDouble(salaryStr);
                }
            } catch (NumberFormatException ignored) {
            }
            boolean isNewStaff = idParam == null || idParam.trim().isEmpty() || "0".equals(idParam.trim());
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (isNewStaff) {
                    // Check for duplicates
                    if (isValueTaken(conn, "SELECT COUNT(*) FROM staff_members WHERE LOWER(username) = ?", username.toLowerCase())
                            || isValueTaken(conn, "SELECT COUNT(*) FROM staff_members WHERE LOWER(email) = ?", email.toLowerCase())
                            || isValueTaken(conn, "SELECT COUNT(*) FROM staff_members WHERE LOWER(staff_code) = ?", staffCode.toLowerCase())) {
                        request.setAttribute("error", "Tên đăng nhập, email hoặc mã nhân viên đã tồn tại.");
                        showAddStaffForm(request, response);
                        return;
                    }
                    
                    String insertStaff = "INSERT INTO staff_members (username, password, email, full_name, phone, staff_code, position, salary, department, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')";
                    try (PreparedStatement stmt = conn.prepareStatement(insertStaff)) {
                        stmt.setString(1, username);
                        stmt.setString(2, PasswordUtil.hashPasswordMD5(password));
                        stmt.setString(3, email);
                        stmt.setString(4, fullName);
                        String phone = request.getParameter("phone");
                        stmt.setString(5, phone != null ? phone : "");
                        stmt.setString(6, staffCode);
                        stmt.setString(7, position);
                        stmt.setDouble(8, salary);
                        stmt.setString(9, department);
                        int updated = stmt.executeUpdate();
                        if (updated > 0) {
                            String success = URLEncoder.encode("Thêm nhân viên mới thành công", StandardCharsets.UTF_8);
                            response.sendRedirect(request.getContextPath() + "/admin?action=staff&success=" + success);
                        } else {
                            request.setAttribute("error", "Không thể thêm nhân viên.");
                            showAddStaffForm(request, response);
                        }
                    }
                } else {
                    // Update existing staff
                    int staffId = Integer.parseInt(idParam.trim());
                    Staff staff = findStaffById(staffId);
                    if (staff == null) {
                        request.setAttribute("error", "Nhân viên không tồn tại để cập nhật.");
                        listStaff(request, response);
                        return;
                    }
                    
                    String updateStaff = "UPDATE staff_members SET username = ?, password = ?, email = ?, full_name = ?, phone = ?, staff_code = ?, position = ?, salary = ?, department = ? WHERE staff_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateStaff)) {
                        stmt.setString(1, username);
                        stmt.setString(2, PasswordUtil.hashPasswordMD5(password));
                        stmt.setString(3, email);
                        stmt.setString(4, fullName);
                        String phone = request.getParameter("phone");
                        stmt.setString(5, phone != null ? phone : "");
                        stmt.setString(6, staffCode);
                        stmt.setString(7, position);
                        stmt.setDouble(8, salary);
                        stmt.setString(9, department);
                        stmt.setInt(10, staffId);
                        int updated = stmt.executeUpdate();
                        if (updated > 0) {
                            String success = URLEncoder.encode("Cập nhật nhân viên thành công", StandardCharsets.UTF_8);
                            response.sendRedirect(request.getContextPath() + "/admin?action=staff&success=" + success);
                        } else {
                            request.setAttribute("error", "Không thể cập nhật nhân viên.");
                            showEditStaffForm(request, response);
                        }
                    }
                }
            }
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            showAddStaffForm(request, response);
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

    private void showEditStaffForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int staffId = Integer.parseInt(request.getParameter("id"));
        Staff staff = findStaffById(staffId);
        if (staff == null) {
            response.sendError(404, "Nhân viên không tồn tại");
            return;
        }
        request.setAttribute("pageTitle", "Chỉnh sửa nhân viên");
        request.setAttribute("formAction", "save_staff");
        request.setAttribute("staff", staff);
        request.getRequestDispatcher("/views/canteen/admin_staff_form.jsp").forward(request, response);
    }
}

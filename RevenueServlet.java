package com.javaweb.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * RevenueServlet - Thống kê doanh thu
 */
@WebServlet("/revenue")
public class RevenueServlet extends HttpServlet {

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
        
        // Only ADMIN or STAFF can access revenue
        if (!("ADMIN".equals(role) || "STAFF".equals(role))) {
            response.sendError(403, "Bạn không có quyền truy cập!");
            return;
        }

        String action = request.getParameter("action");

        if (action == null || "summary".equals(action)) {
            // Tóm tắt doanh thu
            showSummary(request, response);
        } else if ("daily".equals(action)) {
            // Thống kê doanh thu theo ngày
            showDailyRevenue(request, response);
        } else if ("monthly".equals(action)) {
            // Thống kê doanh thu theo tháng
            showMonthlyRevenue(request, response);
        }
    }

    /**
     * Hiển thị doanh thu theo ngày
     */
    private void showDailyRevenue(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        java.util.List<com.javaweb.model.Order> orders = OrderServlet.getAllOrders();
        LocalDate today = LocalDate.now();
        int totalOrders = 0;
        int completedOrders = 0;
        int cancelledOrders = 0;
        double totalRevenue = 0.0;
        Map<String, Double> paymentRevenue = new HashMap<>();

        for (com.javaweb.model.Order order : orders) {
            if (order.getCreatedAt() != null && today.equals(order.getCreatedAt().toLocalDate())) {
                totalOrders++;
                if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                    cancelledOrders++;
                }
                if ("COMPLETED".equalsIgnoreCase(order.getStatus()) || "PROCESSING".equalsIgnoreCase(order.getStatus()) || "PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                    completedOrders++;
                    totalRevenue += order.getTotalAmount();
                }
                String pm = order.getPaymentMethod() != null ? order.getPaymentMethod() : "UNKNOWN";
                paymentRevenue.put(pm, paymentRevenue.getOrDefault(pm, 0.0) + order.getTotalAmount());
            }
        }

        request.setAttribute("date", today);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("completedOrders", completedOrders);
        request.setAttribute("cancelledOrders", cancelledOrders);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("avgOrder", totalOrders > 0 ? totalRevenue / totalOrders : 0.0);
        request.setAttribute("paymentRevenue", paymentRevenue);

        request.getRequestDispatcher("/views/canteen/revenue_dashboard.jsp").forward(request, response);
    }

    /**
     * Hiển thị doanh thu theo tháng
     */
    private void showMonthlyRevenue(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        java.util.List<com.javaweb.model.Order> orders = OrderServlet.getAllOrders();
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();
        int totalOrders = 0;
        int completedOrders = 0;
        int cancelledOrders = 0;
        double totalRevenue = 0.0;
        Map<String, Double> paymentRevenue = new HashMap<>();

        for (com.javaweb.model.Order order : orders) {
            if (order.getCreatedAt() != null && order.getCreatedAt().getMonthValue() == month && order.getCreatedAt().getYear() == year) {
                totalOrders++;
                if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                    cancelledOrders++;
                }
                if ("COMPLETED".equalsIgnoreCase(order.getStatus()) || "PROCESSING".equalsIgnoreCase(order.getStatus()) || "PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                    completedOrders++;
                    totalRevenue += order.getTotalAmount();
                }
                String pm = order.getPaymentMethod() != null ? order.getPaymentMethod() : "UNKNOWN";
                paymentRevenue.put(pm, paymentRevenue.getOrDefault(pm, 0.0) + order.getTotalAmount());
            }
        }

        request.setAttribute("month", String.format("%02d", month));
        request.setAttribute("year", String.valueOf(year));
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("completedOrders", completedOrders);
        request.setAttribute("cancelledOrders", cancelledOrders);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("avgOrder", totalOrders > 0 ? totalRevenue / totalOrders : 0.0);
        request.setAttribute("paymentRevenue", paymentRevenue);

        request.getRequestDispatcher("/views/canteen/revenue_dashboard.jsp").forward(request, response);
    }

    /**
     * Hiển thị tóm tắt doanh thu
     */
    private void showSummary(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Compute from actual orders
        java.util.List<com.javaweb.model.Order> orders = OrderServlet.getAllOrders();
        Map<String, Double> productRevenue = new HashMap<>();
        Map<String, Double> paymentRevenue = new HashMap<>();

        double totalRevenue = 0.0;
        int totalOrders = 0;
        int completedOrders = 0;
        int cancelledOrders = 0;

        for (com.javaweb.model.Order o : orders) {
            totalOrders++;
            if ("CANCELLED".equalsIgnoreCase(o.getStatus())) {
                cancelledOrders++;
            }
            if ("COMPLETED".equalsIgnoreCase(o.getStatus()) || "PROCESSING".equalsIgnoreCase(o.getStatus()) || "PAID".equalsIgnoreCase(o.getPaymentStatus())) {
                completedOrders++;
                totalRevenue += o.getTotalAmount();
            }
            if (o.getOrderDetails() != null) {
                for (com.javaweb.model.OrderDetail detail : o.getOrderDetails()) {
                    String productName = "Unknown";
                    if (detail.getMenuItem() != null && detail.getMenuItem().getItemName() != null) {
                        productName = detail.getMenuItem().getItemName();
                    }
                    productRevenue.put(productName, productRevenue.getOrDefault(productName, 0.0) + detail.getSubtotal());
                }
            }
            String pm = o.getPaymentMethod();
            if (pm == null) pm = "UNKNOWN";
            paymentRevenue.put(pm, paymentRevenue.getOrDefault(pm, 0.0) + o.getTotalAmount());
        }

        double avgOrder = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;

        request.setAttribute("productRevenue", productRevenue);
        request.setAttribute("paymentRevenue", paymentRevenue);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("completedOrders", completedOrders);
        request.setAttribute("cancelledOrders", cancelledOrders);
        request.setAttribute("avgOrder", avgOrder);

        request.getRequestDispatcher("/views/canteen/revenue_dashboard.jsp").forward(request, response);
    }
}

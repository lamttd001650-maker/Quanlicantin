package com.javaweb.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/staff")
public class StaffServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        // Kiểm tra quyền Nhân viên hoặc Admin
        if (session == null || !("STAFF".equals(session.getAttribute("role")) || "ADMIN".equals(session.getAttribute("role")))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        java.util.List<com.javaweb.model.Order> orders = OrderServlet.getAllOrders();
        int totalOrders = 0;
        int pendingOrders = 0;
        int receivedOrders = 0;
        int processingOrders = 0;
        int completedOrders = 0;
        int cancelledOrders = 0;
        double totalRevenue = 0.0;

        for (com.javaweb.model.Order order : orders) {
            totalOrders++;
            String status = order.getStatus();
            if ("PENDING".equalsIgnoreCase(status)) {
                pendingOrders++;
            } else if ("RECEIVED".equalsIgnoreCase(status)) {
                receivedOrders++;
            } else if ("PROCESSING".equalsIgnoreCase(status)) {
                processingOrders++;
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                completedOrders++;
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                cancelledOrders++;
            }
            if ("COMPLETED".equalsIgnoreCase(status) || "PROCESSING".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                totalRevenue += order.getTotalAmount();
            }
        }

        java.util.List<com.javaweb.model.Order> latestOrders = new java.util.ArrayList<>();
        for (int i = orders.size() - 1; i >= 0 && latestOrders.size() < 5; i--) {
            latestOrders.add(orders.get(i));
        }

        request.setAttribute("totalOrders", totalOrders);
        request.setAttribute("pendingOrders", pendingOrders);
        request.setAttribute("receivedOrders", receivedOrders);
        request.setAttribute("processingOrders", processingOrders);
        request.setAttribute("completedOrders", completedOrders);
        request.setAttribute("cancelledOrders", cancelledOrders);
        request.setAttribute("totalRevenue", totalRevenue);
        request.setAttribute("latestOrders", latestOrders);

        request.getRequestDispatcher("/views/canteen/staff_home.jsp").forward(request, response);
    }
}

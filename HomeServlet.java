package com.javaweb.servlet;

import com.javaweb.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        // If not logged in, redirect to login
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Check if role is CUSTOMER
        String role = (String) session.getAttribute("role");
        if (!"CUSTOMER".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Load recent order history for customer
        int customerId = -1;
        try {
            String userIdStr = (String) session.getAttribute("userId");
            if (userIdStr != null) {
                customerId = Integer.parseInt(userIdStr);
            } else {
                customerId = Integer.parseInt((String) session.getAttribute("user"));
            }
        } catch (NumberFormatException ignored) {
        }

        List<Order> recentOrders = com.javaweb.servlet.OrderServlet.getOrdersByCustomer(customerId);
        String successMessage = session.getAttribute("successMessage") != null ? (String) session.getAttribute("successMessage") : null;
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }
        if (recentOrders != null && !recentOrders.isEmpty()) {
            recentOrders.sort(Comparator.comparing(Order::getOrderDate).reversed());
            if (recentOrders.size() > 5) {
                recentOrders = recentOrders.subList(0, 5);
            }
        }
        request.setAttribute("recentOrders", recentOrders);

        // Forward to the home page if logged in as customer
        request.getRequestDispatcher("/views/canteen/home.jsp").forward(request, response);
    }
}

package com.javaweb.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * CheckoutServlet - lightweight entry that shows the delivery info form directly
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Only customers may access checkout form
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session == null || !"CUSTOMER".equals(session.getAttribute("role"))) {
            String msg = java.net.URLEncoder.encode("Chỉ tài khoản khách hàng mới được phép đặt hàng.", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login?error=" + msg);
            return;
        }

        // Forward itemId and quantity to the order form view
        String itemIdParam = request.getParameter("itemId");
        String quantityParam = request.getParameter("quantity");
        if (itemIdParam != null && !itemIdParam.trim().isEmpty()) {
            try {
                int itemId = Integer.parseInt(itemIdParam);
                request.setAttribute("selectedItem", MenuServlet.findMenuItemByIdStatic(itemId));
            } catch (NumberFormatException ignored) {
            }
        }
        int selectedQuantity = 1;
        if (quantityParam != null) {
            try { selectedQuantity = Integer.parseInt(quantityParam); } catch (NumberFormatException ignored) {}
            if (selectedQuantity < 1) selectedQuantity = 1;
        }
        request.setAttribute("selectedQuantity", selectedQuantity);
        request.setAttribute("menuItems", MenuServlet.getMenuItems());
        request.getRequestDispatcher("/views/canteen/order_form.jsp").forward(request, response);
    }
}

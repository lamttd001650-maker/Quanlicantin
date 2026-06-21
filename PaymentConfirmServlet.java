package com.javaweb.servlet;

import com.javaweb.model.Order;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet("/order/payment/confirm")
public class PaymentConfirmServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        int orderId;
        try {
            orderId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        Order order = OrderServlet.findOrderByIdStatic(orderId);
        if (order == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Order not found");
            return;
        }

        if (!OrderServlet.canAccessOrder(request.getSession(false), order)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập đơn hàng này.");
            return;
        }

        if (order.getPaymentExpiresAt() != null && LocalDateTime.now().isAfter(order.getPaymentExpiresAt())) {
            order.setStatus("EXPIRED");
            order.setPaymentStatus("EXPIRED");
            order.setUpdatedAt(LocalDateTime.now());
            response.sendError(HttpServletResponse.SC_GONE, "Thời hạn thanh toán 5 phút đã chấm dứt. Vui lòng đặt lại đơn hàng.");
            return;
        }

        // Mark as paid, add reference and date
        order.setPaymentStatus("PAID");
        order.setStatus("RECEIVED");
        order.setPaymentReference(UUID.randomUUID().toString());
        order.setPaymentDate(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // If redirect parameter present, go back to order detail with success message
        String redirect = request.getParameter("redirect");
        if (redirect != null && ("1".equals(redirect) || "true".equalsIgnoreCase(redirect))) {
            String ctx = request.getContextPath();
            String msg = java.net.URLEncoder.encode("Thanh toán thành công!", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(ctx + "/order?action=view&id=" + orderId + "&success=" + msg);
            return;
        }

        // Otherwise, return simple text response (for gateway callback)
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("OK");
    }
}

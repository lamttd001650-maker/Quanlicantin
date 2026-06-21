package com.javaweb.servlet;

import com.javaweb.model.Payment;
import com.javaweb.model.Order;
import java.time.LocalDateTime;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PaymentServlet - Xử lý thanh toán
 */
@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    private static List<Payment> payments = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (action == null) {
            // Hiển thị danh sách thanh toán
            request.setAttribute("payments", payments);
            request.getRequestDispatcher("/views/canteen/payment_list.jsp").forward(request, response);
        } else if ("detail".equals(action)) {
            // Xem chi tiết thanh toán
            int paymentId = Integer.parseInt(request.getParameter("id"));
            Payment payment = findPaymentById(paymentId);
            if (payment != null) {
                request.setAttribute("payment", payment);
                request.getRequestDispatcher("/views/canteen/payment_detail.jsp").forward(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("create".equals(action)) {
            createPayment(request, response, user);
        } else if ("confirm".equals(action)) {
            confirmPayment(request, response);
        }
    }

    /**
     * Tạo yêu cầu thanh toán mới
     */
    private void createPayment(HttpServletRequest request, HttpServletResponse response, String user) 
            throws ServletException, IOException {
        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            int customerId = Integer.parseInt(user);
            double amount = Double.parseDouble(request.getParameter("amount"));
            String paymentMethod = request.getParameter("paymentMethod");

            if (!isValidPaymentMethod(paymentMethod)) {
                request.setAttribute("error", "Phương thức thanh toán không hợp lệ!");
                doGet(request, response);
                return;
            }

            Payment payment = new Payment(orderId, customerId, amount, paymentMethod);
            payment.setPaymentId(payments.size() + 1);
            payments.add(payment);

            // Link payment to order and set order payment metadata
            Order order = OrderServlet.findOrderByIdStatic(orderId);
            if (order != null) {
                if ("CASH".equalsIgnoreCase(paymentMethod)) {
                    order.setPaymentMethod("COD");
                    order.setPaymentStatus("PENDING");
                } else {
                    order.setPaymentMethod("ONLINE");
                    order.setPaymentStatus("PENDING");
                    // set 5 minute expiry for online payments
                    order.setPaymentExpiresAt(LocalDateTime.now().plusMinutes(5));
                }
                order.setUpdatedAt(LocalDateTime.now());
            }

            request.setAttribute("success", "Tạo yêu cầu thanh toán thành công!");
            doGet(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            doGet(request, response);
        }
    }

    /**
     * Xác nhận thanh toán
     */
    private void confirmPayment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int paymentId = Integer.parseInt(request.getParameter("id"));
            Payment payment = findPaymentById(paymentId);

            if (payment != null) {
                // Giả lập xử lý thanh toán
                String transactionCode = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
                payment.setTransactionCode(transactionCode);
                payment.setStatus("COMPLETED");
                LocalDateTime now = LocalDateTime.now();
                payment.setPaymentDate(now);

                // Update related order as paid
                Order order = OrderServlet.findOrderByIdStatic(payment.getOrderId());
                if (order != null) {
                    order.setPaymentStatus("PAID");
                    order.setPaymentReference(transactionCode);
                    order.setPaymentDate(now);
                    order.setStatus("RECEIVED");
                    order.setUpdatedAt(now);
                }

                request.setAttribute("success", "Thanh toán thành công! Mã giao dịch: " + transactionCode);
            } else {
                request.setAttribute("error", "Không tìm thấy thanh toán!");
            }
            doGet(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            doGet(request, response);
        }
    }

    /**
     * Kiểm tra phương thức thanh toán hợp lệ
     */
    private boolean isValidPaymentMethod(String method) {
        return method != null && 
               (method.equals("CASH") || method.equals("CARD") || 
                method.equals("TRANSFER") || method.equals("WALLET"));
    }

    /**
     * Tìm thanh toán theo ID
     */
    private Payment findPaymentById(int paymentId) {
        for (Payment payment : payments) {
            if (payment.getPaymentId() == paymentId) {
                return payment;
            }
        }
        return null;
    }
}

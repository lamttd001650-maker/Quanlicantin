package com.javaweb.servlet;

import com.javaweb.model.CartItem;
import com.javaweb.model.Order;
import com.javaweb.model.OrderDetail;
import com.javaweb.util.ReviewDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderServlet - Quản lý đơn hàng (Tạo, Xem, Cập nhật)
 */
@WebServlet("/order")
public class OrderServlet extends HttpServlet {

    private static List<Order> orders = new ArrayList<>();
    private static int nextOrderId = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        String userIdStr = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        // Check authentication
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (action == null || "history".equals(action)) {
            String itemIdParam = request.getParameter("itemId");
            if (action == null && itemIdParam != null && !itemIdParam.trim().isEmpty()) {
                // Direct access from "Đặt ngay" URL without explicit action parameter
                if (!"CUSTOMER".equals(role)) {
                    String msg = URLEncoder.encode("Chỉ tài khoản khách hàng mới được phép đặt hàng.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                    return;
                }
                showOrderForm(request, response);
                return;
            }

            // Hiển thị danh sách đơn hàng của user
            if ("ADMIN".equals(role) || "STAFF".equals(role)) {
                // Admin và nhân viên xem tất cả đơn
                request.setAttribute("orders", orders);
                request.setAttribute("pageTitle", "Danh sách đơn hàng");
            } else if ("CUSTOMER".equals(role)) {
                // Chỉ xem đơn hàng của mình
                int custId = -1;
                try {
                    if (userIdStr != null) {
                        custId = Integer.parseInt(userIdStr);
                    } else {
                        custId = Integer.parseInt(user);
                    }
                } catch (NumberFormatException ignored) {
                }
                List<Order> customerOrders = new ArrayList<>();
                for (Order order : orders) {
                    if (order.getCustomerId() == custId) {
                        customerOrders.add(order);
                    }
                }
                request.setAttribute("orders", customerOrders);
                request.setAttribute("pageTitle", "Lịch sử đặt hàng của tôi");
            }
            request.getRequestDispatcher("/views/canteen/order_list.jsp").forward(request, response);
        } else if ("create".equals(action)) {
            if (!"CUSTOMER" .equals(role)) {
                String msg = URLEncoder.encode("Chỉ tài khoản khách hàng mới được phép đặt hàng.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                return;
            }
            showOrderForm(request, response);
            return;
        } else if ("view".equals(action)) {
            // Xem chi tiết đơn hàng
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = findOrderById(orderId);
            if (order != null) {
                if (!canAccessOrder(session, order)) {
                    String msg = URLEncoder.encode("Bạn không có quyền truy cập đơn hàng này.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                    return;
                }
                if ("CUSTOMER".equals(role) && ("DELIVERED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))) {
                    int custId = -1;
                    try {
                        if (userIdStr != null) {
                            custId = Integer.parseInt(userIdStr);
                        } else {
                            custId = Integer.parseInt(user);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    if (custId == order.getCustomerId()) {
                        ReviewDAO reviewDAO = new ReviewDAO();
                        List<OrderDetail> reviewableDetails = new ArrayList<>();
                        if (order.getOrderDetails() != null) {
                            for (OrderDetail detail : order.getOrderDetails()) {
                                if (detail.getMenuItem() != null && !reviewDAO.hasReviewedOrderItem(custId, detail.getMenuItem().getItemId(), order.getOrderId())) {
                                    reviewableDetails.add(detail);
                                }
                            }
                        }
                        request.setAttribute("reviewableDetails", reviewableDetails);
                    }
                }
                request.setAttribute("order", order);
                request.getRequestDispatcher("/views/canteen/order_detail.jsp").forward(request, response);
            } else {
                response.sendError(404, "Đơn hàng không tồn tại");
            }
        } else if ("viewBill".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = findOrderById(orderId);
            if (order != null) {
                if (!canAccessOrder(session, order)) {
                    String msg = URLEncoder.encode("Bạn không có quyền truy cập đơn hàng này.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                    return;
                }
                request.setAttribute("order", order);
                request.getRequestDispatcher("/views/canteen/order_bill.jsp").forward(request, response);
            } else {
                response.sendError(404, "Đơn hàng không tồn tại");
            }
        } else if ("pay".equals(action)) {
            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = findOrderById(orderId);
            if (order != null) {
                if (!canAccessOrder(session, order)) {
                    String msg = URLEncoder.encode("Bạn không có quyền truy cập đơn hàng này.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                    return;
                }
                // Serve local QR image endpoint for server-side generated PNG
                String qrUrl = request.getContextPath() + "/order/qr?id=" + order.getOrderId();
                request.setAttribute("qrUrl", qrUrl);
                request.setAttribute("order", order);
                request.getRequestDispatcher("/views/canteen/order_payment_qr.jsp").forward(request, response);
            } else {
                response.sendError(404, "Đơn hàng không tồn tại");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        String userIdStr = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if ("create".equals(action)) {
            if (!"CUSTOMER".equals(role)) {
                String msg = URLEncoder.encode("Chỉ tài khoản khách hàng mới được phép đặt hàng.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                return;
            }
            createOrder(request, response, user);
        } else if ("update".equals(action)) {
            if (!"ADMIN".equals(role) && !"STAFF".equals(role)) {
                String msg = URLEncoder.encode("Chỉ nhân viên hoặc admin mới được cập nhật đơn hàng.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                return;
            }
            updateOrder(request, response);
        } else if ("cancel".equals(action)) {
            cancelOrder(request, response);
        }
    }

    private void showOrderForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("menuItems", com.javaweb.servlet.MenuServlet.getMenuItems());
        String itemIdParam = request.getParameter("itemId");
        String quantityParam = request.getParameter("quantity");
        HttpSession session = request.getSession(false);
        if (session != null) {
            java.util.List<CartItem> cartItems = getSessionCart(session);
            if (!cartItems.isEmpty()) {
                double cartTotal = 0.0;
                for (CartItem cartItem : cartItems) {
                    cartItem.setItem(com.javaweb.servlet.MenuServlet.findMenuItemByIdStatic(cartItem.getItemId()));
                    if (cartItem.getItem() != null) {
                        cartItem.setSubtotal(cartItem.getQuantity() * cartItem.getItem().getPrice());
                    }
                    cartTotal += cartItem.getSubtotal();
                }
                request.setAttribute("cartItems", cartItems);
                request.setAttribute("cartTotal", cartTotal);
            }
        }
        int selectedQuantity = 1;
        if (quantityParam != null && !quantityParam.trim().isEmpty()) {
            try {
                selectedQuantity = Integer.parseInt(quantityParam);
                if (selectedQuantity < 1) {
                    selectedQuantity = 1;
                }
            } catch (NumberFormatException ignored) {
                selectedQuantity = 1;
            }
        }
        request.setAttribute("selectedQuantity", selectedQuantity);
        if (itemIdParam != null && !itemIdParam.trim().isEmpty()) {
            try {
                int itemId = Integer.parseInt(itemIdParam);
                request.setAttribute("selectedItem", com.javaweb.servlet.MenuServlet.findMenuItemByIdStatic(itemId));
            } catch (NumberFormatException ignored) {
            }
        }
        request.getRequestDispatcher("/views/canteen/order_form.jsp").forward(request, response);
    }

    /**
     * Tạo đơn hàng mới
     */
    private void createOrder(HttpServletRequest request, HttpServletResponse response, String user) 
            throws ServletException, IOException {
        try {
            // Resolve numeric customer id from session or username
            HttpSession session = request.getSession(false);
            String userIdStr = session != null ? (String) session.getAttribute("userId") : null;
            int customerId;
            try {
                if (userIdStr != null) {
                    customerId = Integer.parseInt(userIdStr);
                } else {
                    customerId = Integer.parseInt(user);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Không xác định được thông tin khách hàng. Vui lòng đăng nhập lại.");
                showOrderForm(request, response);
                return;
            }
            String notes = request.getParameter("notes");
            String recipientName = request.getParameter("recipientName");
            String recipientPhone = request.getParameter("recipientPhone");
            String recipientAddress = request.getParameter("recipientAddress");
            String paymentMethod = request.getParameter("paymentMethod");

            String checkoutFromCart = request.getParameter("checkoutFromCart");
            String itemIdStr = request.getParameter("itemId");
            String qtyStr = request.getParameter("quantity");
            int itemId = itemIdStr != null && !itemIdStr.isEmpty() ? Integer.parseInt(itemIdStr) : -1;
            int quantity = qtyStr != null && !qtyStr.isEmpty() ? Integer.parseInt(qtyStr) : 1;
            java.util.List<CartItem> cartItems = getSessionCart(session);
            boolean useCart = "true".equalsIgnoreCase(checkoutFromCart) || (itemId <= 0 && !cartItems.isEmpty());

            java.util.List<OrderDetail> details = new java.util.ArrayList<>();
            double total = 0.0;
            if (useCart) {
                if (cartItems.isEmpty()) {
                    request.setAttribute("error", "Giỏ hàng đang trống. Vui lòng thêm món trước khi thanh toán.");
                    showOrderForm(request, response);
                    return;
                }
                for (CartItem cartItem : cartItems) {
                    com.javaweb.model.MenuItem menuItem = com.javaweb.servlet.MenuServlet.findMenuItemByIdStatic(cartItem.getItemId());
                    if (menuItem == null) {
                        request.setAttribute("error", "Món ăn trong giỏ hàng không tồn tại hoặc đã bị xóa.");
                        showOrderForm(request, response);
                        return;
                    }
                    OrderDetail od = new OrderDetail(cartItem.getItemId(), cartItem.getQuantity(), menuItem.getPrice());
                    od.setMenuItem(menuItem);
                    details.add(od);
                    total += od.getSubtotal();
                }
            } else {
                if (itemId <= 0) {
                    request.setAttribute("error", "Vui lòng chọn món ăn để đặt.");
                    request.setAttribute("selectedQuantity", quantity);
                    request.setAttribute("selectedItem", null);
                    showOrderForm(request, response);
                    return;
                }
                if (quantity < 1) {
                    request.setAttribute("error", "Số lượng phải là số nguyên dương.");
                    request.setAttribute("selectedQuantity", 1);
                    request.setAttribute("selectedItem", com.javaweb.servlet.MenuServlet.findMenuItemByIdStatic(itemId));
                    showOrderForm(request, response);
                    return;
                }
                com.javaweb.model.MenuItem menuItem = com.javaweb.servlet.MenuServlet.findMenuItemByIdStatic(itemId);
                if (menuItem == null) {
                    request.setAttribute("error", "Món ăn không tồn tại hoặc đã bị xóa.");
                    request.setAttribute("selectedQuantity", quantity);
                    showOrderForm(request, response);
                    return;
                }
                OrderDetail od = new OrderDetail(itemId, quantity, menuItem.getPrice());
                od.setMenuItem(menuItem);
                details.add(od);
                total = od.getSubtotal();
            }

            Order order = new Order(customerId);
            order.setOrderId(nextOrderId++);
            order.setNotes(notes);
            order.setRecipientName(recipientName);
            order.setRecipientPhone(recipientPhone);
            order.setRecipientAddress(recipientAddress);
            order.setPaymentMethod(paymentMethod != null ? paymentMethod : "COD");
            // Initialize payment status
            order.setPaymentStatus("PENDING");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            if ("ONLINE".equalsIgnoreCase(order.getPaymentMethod())) {
                order.setPaymentExpiresAt(order.getCreatedAt().plusMinutes(5));
            }

            // Apply coupon discount from session if present
            double discount = 0.0;
            Object discObj = session.getAttribute("discountAmount");
            if (discObj instanceof Double) {
                discount = (Double) discObj;
            } else if (discObj instanceof Number) {
                discount = ((Number) discObj).doubleValue();
            }
            if (discount > 0) {
                total = Math.max(0.0, total - discount);
                Object appliedCoupon = session.getAttribute("appliedCoupon");
                if (appliedCoupon != null) {
                    order.setNotes((order.getNotes() == null ? "" : order.getNotes() + " ") + "Coupon applied");
                }
                session.removeAttribute("appliedCoupon");
                session.removeAttribute("discountAmount");
            }
            order.setOrderDetails(details);
            order.setTotalAmount(total);
            orders.add(order);
            if (useCart) {
                session.removeAttribute("cartItems");
            }

            if ("ONLINE".equalsIgnoreCase(order.getPaymentMethod())) {
                // Redirect to payment QR page
                response.sendRedirect(request.getContextPath() + "/order?action=pay&id=" + order.getOrderId());
            } else {
                // COD: redirect to printable bill
                response.sendRedirect(request.getContextPath() + "/order?action=viewBill&id=" + order.getOrderId());
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Giá trị nhập không hợp lệ.");
            showOrderForm(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Lỗi: " + e.getMessage());
            showOrderForm(request, response);
        }
    }

    // Handle viewing printable bill and payment QR
    // Add to doGet: handle viewBill and pay actions

    /**
     * Cập nhật trạng thái đơn hàng
     */
    private void updateOrder(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int orderId = Integer.parseInt(request.getParameter("id"));
            String status = request.getParameter("status");

            Order order = findOrderById(orderId);
            if (order != null) {
                order.setStatus(status);
                order.setUpdatedAt(LocalDateTime.now());
                if (("COMPLETED".equalsIgnoreCase(status) || "DELIVERED".equalsIgnoreCase(status) || "ARRIVED".equalsIgnoreCase(status)) && "COD".equalsIgnoreCase(order.getPaymentMethod())) {
                    order.setPaymentStatus("PAID");
                }
                if (("DELIVERED".equalsIgnoreCase(status) || "ARRIVED".equalsIgnoreCase(status)) && "ONLINE".equalsIgnoreCase(order.getPaymentMethod()) && "PAID".equalsIgnoreCase(order.getPaymentStatus())) {
                    order.setPaymentStatus("PAID");
                }
                String msg = URLEncoder.encode("Cập nhật đơn hàng thành công!", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?success=" + msg);
                return;
            } else {
                String msg = URLEncoder.encode("Không tìm thấy đơn hàng!", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
                return;
            }
        } catch (Exception e) {
            String msg = URLEncoder.encode("Lỗi: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/order?error=" + msg);
            return;
        }
    }

    /**
     * Hủy đơn hàng
     */
    private void cancelOrder(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            String role = session != null ? (String) session.getAttribute("role") : null;
            String user = session != null ? (String) session.getAttribute("user") : null;
            String userIdStr = session != null ? (String) session.getAttribute("userId") : null;

            int orderId = Integer.parseInt(request.getParameter("id"));
            Order order = findOrderById(orderId);
            if (order == null) {
                String enc = URLEncoder.encode("Không tìm thấy đơn hàng!", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + enc);
                return;
            }

            boolean authorized = false;
            if ("ADMIN".equals(role) || "STAFF".equals(role)) {
                authorized = true;
            } else if ("CUSTOMER".equals(role)) {
                int custId = -1;
                try {
                    if (userIdStr != null) {
                        custId = Integer.parseInt(userIdStr);
                    } else {
                        custId = Integer.parseInt(user);
                    }
                } catch (NumberFormatException ignored) {
                }
                if (order.getCustomerId() == custId && "PENDING".equalsIgnoreCase(order.getStatus())) {
                    authorized = true;
                }
            }

            if (!authorized) {
                String enc = URLEncoder.encode("Bạn không có quyền hủy đơn hàng này hoặc đơn hàng không thể hủy.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + enc);
                return;
            }

            if ("PENDING".equalsIgnoreCase(order.getStatus()) || "PROCESSING".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("CANCELLED");
                order.setUpdatedAt(LocalDateTime.now());
                String paymentMethod = order.getPaymentMethod();
                String paymentStatus = order.getPaymentStatus();
                String msg;
                if ("ONLINE".equalsIgnoreCase(paymentMethod)) {
                    if ("PAID".equalsIgnoreCase(paymentStatus)) {
                        order.setPaymentStatus("REFUNDED");
                        msg = "Hủy đơn hàng thành công! Số tiền đã được hoàn lại (đã thanh toán online).";
                    } else {
                        order.setPaymentStatus("CANCELLED");
                        msg = "Hủy đơn hàng thành công! Đơn thanh toán online chưa hoàn tất, không cần hoàn tiền.";
                    }
                } else {
                    msg = "Hủy đơn hàng thành công!";
                }
                String enc = URLEncoder.encode(msg, StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?success=" + enc);
            } else {
                String enc = URLEncoder.encode("Không thể hủy đơn hàng ở trạng thái này!", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/order?error=" + enc);
            }
        } catch (Exception e) {
            String enc = URLEncoder.encode("Lỗi: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/order?error=" + enc);
        }
    }

    /**
     * Tìm đơn hàng theo ID
     */
    private Order findOrderById(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    // Public static finder so other servlets can locate orders
    public static Order findOrderByIdStatic(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    // Public accessor for other servlets to read all orders (live list)
    public static List<Order> getAllOrders() {
        return orders;
    }

    // Public accessor for other servlets to read a customer's orders
    public static List<Order> getOrdersByCustomer(int customerId) {
        List<Order> customerOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getCustomerId() == customerId) {
                customerOrders.add(order);
            }
        }
        return customerOrders;
    }

    private java.util.List<CartItem> getSessionCart(HttpSession session) {
        java.util.List<CartItem> cartItems = (java.util.List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }
        return cartItems;
    }

    public static boolean canAccessOrder(HttpSession session, Order order) {
        if (session == null || order == null) {
            return false;
        }
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role) || "STAFF".equals(role)) {
            return true;
        }
        if ("CUSTOMER".equals(role)) {
            String userIdStr = (String) session.getAttribute("userId");
            String user = (String) session.getAttribute("user");
            int custId = -1;
            try {
                if (userIdStr != null) {
                    custId = Integer.parseInt(userIdStr);
                } else if (user != null) {
                    custId = Integer.parseInt(user);
                }
            } catch (NumberFormatException ignored) {
            }
            return order.getCustomerId() == custId;
        }
        return false;
    }
}


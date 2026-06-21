package com.javaweb.servlet;

import com.javaweb.model.CartItem;
import com.javaweb.model.MenuItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CartServlet - Quản lý giỏ hàng của khách hàng
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"CUSTOMER".equals(session.getAttribute("role"))) {
            String msg = java.net.URLEncoder.encode("Chỉ tài khoản khách hàng mới được phép truy cập giỏ hàng.", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login?error=" + msg);
            return;
        }

        String action = request.getParameter("action");
        if (action == null || "view".equals(action)) {
            prepareCart(request, session);
            request.getRequestDispatcher("/views/canteen/cart.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"CUSTOMER".equals(session.getAttribute("role"))) {
            String msg = java.net.URLEncoder.encode("Chỉ tài khoản khách hàng mới được phép sử dụng giỏ hàng.", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login?error=" + msg);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        if ("add".
                equals(action)) {
            handleAddToCart(request, response, session);
        } else if ("update".
                equals(action)) {
            handleUpdateCart(request, response, session);
        } else if ("remove".
                equals(action)) {
            handleRemoveFromCart(request, response, session);
        } else if ("clear".
                equals(action)) {
            handleClearCart(request, response, session);
        } else {
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }

    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String itemIdParam = request.getParameter("itemId");
        String quantityParam = request.getParameter("quantity");
        int itemId = -1;
        int quantity = 1;
        try {
            if (itemIdParam != null) {
                itemId = Integer.parseInt(itemIdParam);
            }
        } catch (NumberFormatException ignored) {
        }
        try {
            if (quantityParam != null) {
                quantity = Integer.parseInt(quantityParam);
            }
        } catch (NumberFormatException ignored) {
        }
        if (quantity < 1) {
            quantity = 1;
        }

        MenuItem menuItem = MenuServlet.findMenuItemByIdStatic(itemId);
        if (menuItem == null) {
            String msg = java.net.URLEncoder.encode("Món ăn không tồn tại.", java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect(request.getHeader("Referer") != null ? request.getHeader("Referer") : request.getContextPath() + "/menu?error=" + msg);
            return;
        }

        List<CartItem> cartItems = getCart(session);
        CartItem existing = null;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItemId() == itemId) {
                existing = cartItem;
                break;
            }
        }
        if (existing != null) {
            existing.addQuantity(quantity);
        } else {
            CartItem cartItem = new CartItem(getCustomerId(session), itemId, quantity);
            cartItem.setItem(menuItem);
            cartItem.setSubtotal(quantity * menuItem.getPrice());
            cartItems.add(cartItem);
        }
        session.setAttribute("cartItems", cartItems);
        session.setAttribute("cartSuccess", "Thêm vào giỏ hàng thành công.");
        session.setAttribute("successMessage", "Thêm vào giỏ hàng thành công.");
        response.sendRedirect(request.getContextPath() + "/menu");
    }

    private void handleUpdateCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String itemIdParam = request.getParameter("itemId");
        String quantityParam = request.getParameter("quantity");
        int itemId = -1;
        int quantity = 1;
        try {
            if (itemIdParam != null) {
                itemId = Integer.parseInt(itemIdParam);
            }
            if (quantityParam != null) {
                quantity = Integer.parseInt(quantityParam);
            }
        } catch (NumberFormatException ignored) {
        }
        List<CartItem> cartItems = getCart(session);
        CartItem current = null;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItemId() == itemId) {
                current = cartItem;
                break;
            }
        }
        if (current != null) {
            if (quantity <= 0) {
                cartItems.remove(current);
            } else {
                current.setQuantity(quantity);
                if (current.getItem() != null) {
                    current.setSubtotal(quantity * current.getItem().getPrice());
                }
            }
        }
        session.setAttribute("cartItems", cartItems);
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String itemIdParam = request.getParameter("itemId");
        int itemId = -1;
        try {
            if (itemIdParam != null) {
                itemId = Integer.parseInt(itemIdParam);
            }
        } catch (NumberFormatException ignored) {
        }
        List<CartItem> cartItems = getCart(session);
        final int targetItemId = itemId;
        cartItems.removeIf(cartItem -> cartItem.getItemId() == targetItemId);
        session.setAttribute("cartItems", cartItems);
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private void handleClearCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        session.removeAttribute("cartItems");
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private List<CartItem> getCart(HttpSession session) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }
        return cartItems;
    }

    private void prepareCart(HttpServletRequest request, HttpSession session) {
        List<CartItem> cartItems = getCart(session);
        int totalQuantity = 0;
        for (CartItem cartItem : cartItems) {
            MenuItem menuItem = MenuServlet.findMenuItemByIdStatic(cartItem.getItemId());
            cartItem.setItem(menuItem);
            if (menuItem != null) {
                cartItem.setSubtotal(cartItem.getQuantity() * menuItem.getPrice());
            }
            totalQuantity += cartItem.getQuantity();
        }
        request.setAttribute("cartItems", cartItems);
        request.setAttribute("cartQuantity", totalQuantity);
        request.setAttribute("cartItemCount", cartItems.size());
        String cartSuccess = (String) session.getAttribute("cartSuccess");
        if (cartSuccess != null) {
            request.setAttribute("cartSuccess", cartSuccess);
            session.removeAttribute("cartSuccess");
        }
    }

    private int getCustomerId(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String user = (String) session.getAttribute("user");
        try {
            if (userId != null) {
                return Integer.parseInt(userId);
            } else if (user != null) {
                return Integer.parseInt(user);
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }
}

package com.javaweb.servlet;

import com.javaweb.model.MenuItem;
import com.javaweb.model.Order;
import com.javaweb.model.Review;
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
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewServlet - Quản lý đánh giá sản phẩm
 */
@WebServlet("/review")
public class ReviewServlet extends HttpServlet {
    private ReviewDAO reviewDAO = new ReviewDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("view".equals(action)) {
            // Xem các reviews của một sản phẩm
            int itemId = Integer.parseInt(request.getParameter("item_id"));
            
            List<Review> reviews = reviewDAO.getReviewsByItem(itemId);
            double avgRating = reviewDAO.getAverageRating(itemId);
            int reviewCount = reviewDAO.getReviewCount(itemId);
            int[] ratingDistribution = reviewDAO.getRatingDistribution(itemId);
            
            request.setAttribute("reviews", reviews);
            request.setAttribute("avgRating", avgRating);
            request.setAttribute("reviewCount", reviewCount);
            request.setAttribute("ratingDistribution", ratingDistribution);
            request.setAttribute("itemId", itemId);
            
            request.getRequestDispatcher("/views/canteen/reviews.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        
        if ("add".equals(action)) {
            try {
                int itemId = Integer.parseInt(request.getParameter("item_id"));
                int orderId = Integer.parseInt(request.getParameter("order_id"));
                int rating = Integer.parseInt(request.getParameter("rating"));
                String comment = request.getParameter("comment");
                
                // Lấy customer từ session
                Object userObj = session.getAttribute("user");
                if (userObj == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    request.setAttribute("error", "Bạn phải đăng nhập để đánh giá");
                    request.getRequestDispatcher("/views/canteen/login.jsp").forward(request, response);
                    return;
                }
                
                String customerIdStr = (String) session.getAttribute("userId");
                int customerId;
                try {
                    customerId = Integer.parseInt(customerIdStr != null ? customerIdStr : userObj.toString());
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Không xác định được thông tin khách hàng.");
                    return;
                }

                Order order = OrderServlet.findOrderByIdStatic(orderId);
                if (order == null || order.getCustomerId() != customerId || 
                        !("DELIVERED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))) {
                    String msg = URLEncoder.encode("Bạn chỉ có thể đánh giá món sau khi đơn hàng đã giao thành công.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?action=view&id=" + orderId + "&error=" + msg);
                    return;
                }

                if (reviewDAO.hasReviewedOrderItem(customerId, itemId, orderId)) {
                    String msg = URLEncoder.encode("Bạn đã đánh giá món này cho đơn hàng này rồi.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?action=view&id=" + orderId + "&error=" + msg);
                    return;
                }

                Review review = new Review(itemId, customerId, orderId, rating, comment);
                
                if (reviewDAO.addReview(review)) {
                    // Cập nhật rating trên menu_items table
                    updateMenuItemRating(itemId);
                    String successMsg = URLEncoder.encode("Đã gửi đánh giá. Cảm ơn bạn!", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/order?action=view&id=" + orderId + "&success=" + successMsg);
                } else {
                    request.setAttribute("error", "Lỗi khi thêm đánh giá");
                    request.getRequestDispatcher("/views/canteen/order_detail.jsp").forward(request, response);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "Dữ liệu không hợp lệ");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
            }
        }
    }

    /**
     * Cập nhật rating trung bình cho sản phẩm
     */
    private void updateMenuItemRating(int itemId) {
        double avgRating = reviewDAO.getAverageRating(itemId);
        int reviewCount = reviewDAO.getReviewCount(itemId);
        
        String sql = "UPDATE menu_items SET rating = ?, review_count = ? WHERE item_id = ?";
        
        try (java.sql.Connection conn = com.javaweb.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, avgRating);
            pstmt.setInt(2, reviewCount);
            pstmt.setInt(3, itemId);
            pstmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("Error updating menu item rating: " + e.getMessage());
        }
    }
}

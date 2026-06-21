package com.javaweb.servlet;

import com.javaweb.model.Coupon;
import com.javaweb.util.CouponDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * CouponServlet - Quản lý mã giảm giá/coupon
 */
@WebServlet("/coupon")
public class CouponServlet extends HttpServlet {
    private CouponDAO couponDAO = new CouponDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            // Lấy danh sách coupon đang active
            List<Coupon> activeCoupons = couponDAO.getActiveCoupons();
            request.setAttribute("coupons", activeCoupons);
            request.getRequestDispatcher("/views/canteen/coupons.jsp").forward(request, response);
        } else if ("verify".equals(action)) {
            // Xác minh coupon code
            String couponCode = request.getParameter("code");
            Coupon coupon = couponDAO.getCouponByCode(couponCode);
            
            if (coupon != null && coupon.isValid()) {
                response.setContentType("application/json");
                response.getWriter().write("{\"valid\": true, \"discount\": " + coupon.getDiscountValue() + ", " +
                        "\"type\": \"" + coupon.getDiscountType() + "\"}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"valid\": false, \"error\": \"Mã coupon không hợp lệ\"}");
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        
        if ("apply".equals(action)) {
            try {
                String couponCode = request.getParameter("coupon_code");
                double orderAmount = Double.parseDouble(request.getParameter("order_amount"));
                
                Coupon coupon = couponDAO.getCouponByCode(couponCode);
                
                if (coupon == null) {
                    request.setAttribute("couponError", "Mã coupon không tồn tại");
                } else if (!coupon.isValid()) {
                    request.setAttribute("couponError", "Mã coupon đã hết hạn hoặc không còn hiệu lực");
                } else if (orderAmount < coupon.getMinOrderAmount()) {
                    request.setAttribute("couponError", 
                        "Đơn hàng phải từ " + String.format("%,.0f", coupon.getMinOrderAmount()) + "đ");
                } else {
                    // Áp dụng coupon
                    double discount = coupon.calculateDiscount(orderAmount);
                    session.setAttribute("appliedCoupon", coupon);
                    session.setAttribute("discountAmount", discount);
                    
                    // Tăng used_count
                    couponDAO.incrementUsedCount(coupon.getCouponId());
                    
                    request.setAttribute("couponSuccess", "Áp dụng mã " + couponCode + " thành công! " +
                            "Giảm giá: " + String.format("%,.0f", discount) + "đ");
                }
                
                // Quay lại trang giỏ hàng hoặc đặt hàng
                String referer = request.getHeader("Referer");
                response.sendRedirect(referer != null ? referer : "/order");
            } catch (NumberFormatException e) {
                request.setAttribute("couponError", "Dữ liệu không hợp lệ");
                response.sendRedirect("/order");
            }
        }
    }
}

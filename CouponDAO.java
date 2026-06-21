package com.javaweb.util;

import com.javaweb.model.Coupon;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CouponDAO - Xử lý tất cả database operations cho Coupon
 */
public class CouponDAO {

    /**
     * Lấy coupon theo mã code
     */
    public Coupon getCouponByCode(String couponCode) {
        String sql = "SELECT * FROM coupons WHERE coupon_code = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, couponCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCoupon(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching coupon: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Lấy tất cả coupon đang active
     */
    public List<Coupon> getActiveCoupons() {
        String sql = "SELECT * FROM coupons WHERE status = 'ACTIVE' " +
                     "AND valid_from <= GETDATE() AND valid_to >= GETDATE() " +
                     "ORDER BY discount_value DESC";
        
        List<Coupon> coupons = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                coupons.add(mapResultSetToCoupon(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active coupons: " + e.getMessage());
        }
        
        return coupons;
    }

    /**
     * Thêm coupon mới
     */
    public boolean addCoupon(Coupon coupon) {
        String sql = "INSERT INTO coupons (coupon_code, description, discount_type, discount_value, " +
                     "min_order_amount, max_discount, max_uses, valid_from, valid_to, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, coupon.getCouponCode());
            pstmt.setString(2, coupon.getDescription());
            pstmt.setString(3, coupon.getDiscountType());
            pstmt.setDouble(4, coupon.getDiscountValue());
            pstmt.setDouble(5, coupon.getMinOrderAmount());
            pstmt.setDouble(6, coupon.getMaxDiscount());
            pstmt.setInt(7, coupon.getMaxUses());
            pstmt.setTimestamp(8, coupon.getValidFrom() != null ? Timestamp.valueOf(coupon.getValidFrom()) : null);
            pstmt.setTimestamp(9, coupon.getValidTo() != null ? Timestamp.valueOf(coupon.getValidTo()) : null);
            pstmt.setString(10, coupon.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding coupon: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật used_count khi coupon được sử dụng
     */
    public boolean incrementUsedCount(int couponId) {
        String sql = "UPDATE coupons SET used_count = used_count + 1 WHERE coupon_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, couponId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error incrementing coupon usage: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper: Map ResultSet to Coupon object
     */
    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setCouponId(rs.getInt("coupon_id"));
        coupon.setCouponCode(rs.getString("coupon_code"));
        coupon.setDescription(rs.getString("description"));
        coupon.setDiscountType(rs.getString("discount_type"));
        coupon.setDiscountValue(rs.getDouble("discount_value"));
        coupon.setMinOrderAmount(rs.getDouble("min_order_amount"));
        coupon.setMaxDiscount(rs.getDouble("max_discount"));
        coupon.setMaxUses(rs.getInt("max_uses"));
        coupon.setUsedCount(rs.getInt("used_count"));
        
        Timestamp validFromTs = rs.getTimestamp("valid_from");
        if (validFromTs != null) {
            coupon.setValidFrom(validFromTs.toLocalDateTime());
        }
        
        Timestamp validToTs = rs.getTimestamp("valid_to");
        if (validToTs != null) {
            coupon.setValidTo(validToTs.toLocalDateTime());
        }
        
        coupon.setStatus(rs.getString("status"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            coupon.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        return coupon;
    }
}

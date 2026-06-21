package com.javaweb.util;

import com.javaweb.model.Review;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewDAO - Xử lý tất cả database operations cho Review
 */
public class ReviewDAO {

    /**
     * Thêm review mới
     */
    public boolean addReview(Review review) {
        String sql = "INSERT INTO reviews (item_id, customer_id, order_id, rating, comment, verified_purchase) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, review.getItemId());
            pstmt.setInt(2, review.getCustomerId());
            pstmt.setInt(3, review.getOrderId());
            pstmt.setInt(4, review.getRating());
            pstmt.setString(5, review.getComment());
            pstmt.setBoolean(6, review.isVerifiedPurchase());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy tất cả reviews cho một sản phẩm
     */
    public List<Review> getReviewsByItem(int itemId) {
        String sql = "SELECT * FROM reviews WHERE item_id = ? ORDER BY created_at DESC";
        List<Review> reviews = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
        }
        
        return reviews;
    }

    /**
     * Tính rating trung bình cho một sản phẩm
     */
    public double getAverageRating(int itemId) {
        String sql = "SELECT AVG(CAST(rating AS FLOAT)) as avg_rating FROM reviews WHERE item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        
        return 0.0;
    }

    /**
     * Lấy số lượng reviews cho một sản phẩm
     */
    public int getReviewCount(int itemId) {
        String sql = "SELECT COUNT(*) as review_count FROM reviews WHERE item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("review_count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting review count: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Kiểm tra khách hàng đã mua sản phẩm này chưa (verified purchase)
     */
    public boolean hasPurchasedItem(int customerId, int itemId) {
        String sql = "SELECT COUNT(*) as cnt FROM orders o " +
                     "INNER JOIN order_details od ON o.order_id = od.order_id " +
                     "WHERE o.customer_id = ? AND od.item_id = ? AND o.status = 'COMPLETED'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking purchase: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Kiểm tra khách hàng đã đánh giá món này trong đơn hàng chưa
     */
    public boolean hasReviewedOrderItem(int customerId, int itemId, int orderId) {
        String sql = "SELECT COUNT(*) as cnt FROM reviews WHERE customer_id = ? AND item_id = ? AND order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking existing review: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Lấy phân bố rating (5 sao, 4 sao, v.v.)
     */
    public int[] getRatingDistribution(int itemId) {
        int[] distribution = new int[5];
        String sql = "SELECT rating, COUNT(*) as cnt FROM reviews WHERE item_id = ? GROUP BY rating";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int rating = rs.getInt("rating") - 1;
                if (rating >= 0 && rating < 5) {
                    distribution[rating] = rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting rating distribution: " + e.getMessage());
        }
        
        return distribution;
    }

    /**
     * Helper: Map ResultSet to Review object
     */
    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setItemId(rs.getInt("item_id"));
        review.setCustomerId(rs.getInt("customer_id"));
        review.setOrderId(rs.getInt("order_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setVerifiedPurchase(rs.getBoolean("verified_purchase"));
        review.setHelpfulCount(rs.getInt("helpful_count"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            review.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            review.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        
        return review;
    }
}

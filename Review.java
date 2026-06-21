package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Review - Đánh giá sản phẩm
 */
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private int reviewId;
    private int itemId;
    private int customerId;
    private int orderId;
    private int rating;              // 1-5 stars
    private String comment;
    private boolean verifiedPurchase;
    private int helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Review() {}

    public Review(int itemId, int customerId, int orderId, int rating, String comment) {
        this.itemId = itemId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.verifiedPurchase = true;
        this.helpfulCount = 0;
    }

    // Getters and Setters
    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = Math.max(1, Math.min(5, rating)); }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }

    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", itemId=" + itemId +
                ", customerId=" + customerId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

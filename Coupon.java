package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Coupon - Mã giảm giá
 */
public class Coupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private int couponId;
    private String couponCode;
    private String description;
    private String discountType;     // PERCENT or FIXED
    private double discountValue;
    private double minOrderAmount;
    private double maxDiscount;
    private int maxUses;
    private int usedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String status;           // ACTIVE, EXPIRED, DISABLED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Coupon() {}

    public Coupon(String couponCode, String description, String discountType, double discountValue) {
        this.couponCode = couponCode;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.status = "ACTIVE";
    }

    // Methods
    public boolean isValid() {
        if(!"ACTIVE".equals(status)) return false;
        
        LocalDateTime now = LocalDateTime.now();
        if(validFrom != null && now.isBefore(validFrom)) return false;
        if(validTo != null && now.isAfter(validTo)) return false;
        if(maxUses > 0 && usedCount >= maxUses) return false;
        
        return true;
    }

    public double calculateDiscount(double orderAmount) {
        if(!isValid()) return 0;
        if(orderAmount < minOrderAmount) return 0;
        
        double discount;
        if("PERCENT".equals(discountType)) {
            discount = (orderAmount * discountValue) / 100;
        } else {
            discount = discountValue;
        }
        
        if(maxDiscount > 0 && discount > maxDiscount) {
            discount = maxDiscount;
        }
        
        return discount;
    }

    // Getters and Setters
    public int getCouponId() { return couponId; }
    public void setCouponId(int couponId) { this.couponId = couponId; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public double getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(double maxDiscount) { this.maxDiscount = maxDiscount; }

    public int getMaxUses() { return maxUses; }
    public void setMaxUses(int maxUses) { this.maxUses = maxUses; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Coupon{" +
                "couponCode='" + couponCode + '\'' +
                ", description='" + description + '\'' +
                ", discountValue=" + discountValue +
                ", status='" + status + '\'' +
                '}';
    }
}

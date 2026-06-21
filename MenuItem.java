package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MenuItem - Thực đơn/Món ăn
 */
public class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int itemId;
    private String itemName;
    private String description;
    private String category; // Cơm, Bánh mì, Thức uống, etc
    private double price;
    private double rating;
    private String imageUrl;
    private int available; // 1: available, 0: unavailable
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public MenuItem() {}

    public MenuItem(String itemName, String category, double price) {
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.available = 1;
    }

    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "MenuItem{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", available=" + available +
                '}';
    }
}

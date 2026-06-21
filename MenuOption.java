package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * MenuOption - Tùy chọn cho sản phẩm (Size, Topping, etc)
 */
public class MenuOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private int optionId;
    private int itemId;
    private String optionType;      // SIZE, TOPPING, EXTRA, SPECIAL, etc
    private String optionName;      // Small, Medium, Large, Cheese, Bacon, etc
    private double priceAdjustment;
    private boolean available;
    private LocalDateTime createdAt;

    // Constructors
    public MenuOption() {}

    public MenuOption(int itemId, String optionType, String optionName, double priceAdjustment) {
        this.itemId = itemId;
        this.optionType = optionType;
        this.optionName = optionName;
        this.priceAdjustment = priceAdjustment;
        this.available = true;
    }

    // Getters and Setters
    public int getOptionId() { return optionId; }
    public void setOptionId(int optionId) { this.optionId = optionId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getOptionType() { return optionType; }
    public void setOptionType(String optionType) { this.optionType = optionType; }

    public String getOptionName() { return optionName; }
    public void setOptionName(String optionName) { this.optionName = optionName; }

    public double getPriceAdjustment() { return priceAdjustment; }
    public void setPriceAdjustment(double priceAdjustment) { this.priceAdjustment = priceAdjustment; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "MenuOption{" +
                "optionType='" + optionType + '\'' +
                ", optionName='" + optionName + '\'' +
                ", priceAdjustment=" + priceAdjustment +
                '}';
    }
}

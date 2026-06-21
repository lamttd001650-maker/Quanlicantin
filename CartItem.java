package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * CartItem - Mục trong giỏ hàng
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int cartId;
    private int customerId;
    private int itemId;
    private int quantity;
    private String specialNotes;
    private String optionsJson;     // {"size": "large", "topping": ["cheese", "bacon"]}
    private LocalDateTime addedAt;
    
    // Additional fields (not from DB, for display)
    private MenuItem item;          // For displaying product info
    private double subtotal;        // quantity * price

    // Constructors
    public CartItem() {}

    public CartItem(int customerId, int itemId, int quantity) {
        this.customerId = customerId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    // Methods
    public double calculateSubtotal(double basePrice) {
        return quantity * basePrice;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
        if(this.quantity <= 0) this.quantity = 1;
    }

    // Getters and Setters
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }

    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public MenuItem getItem() { return item; }
    public void setItem(MenuItem item) { this.item = item; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return "CartItem{" +
                "cartId=" + cartId +
                ", itemId=" + itemId +
                ", quantity=" + quantity +
                ", specialNotes='" + specialNotes + '\'' +
                '}';
    }
}

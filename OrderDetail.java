package com.javaweb.model;

import java.io.Serializable;

/**
 * OrderDetail - Chi tiết đơn hàng
 */
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private int detailId;
    private int orderId;
    private int itemId;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    private MenuItem menuItem; // Để lưu thông tin món ăn

    // Constructors
    public OrderDetail() {}

    public OrderDetail(int itemId, int quantity, double unitPrice) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    // Getters and Setters
    public int getDetailId() { return detailId; }
    public void setDetailId(int detailId) { this.detailId = detailId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        this.subtotal = quantity * this.unitPrice;
    }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "detailId=" + detailId +
                ", orderId=" + orderId +
                ", itemId=" + itemId +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}

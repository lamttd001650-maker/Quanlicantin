package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Order - Đơn hàng
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private int orderId;
    private int customerId;
    private int staffId;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private double totalAmount;
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED
    private String notes;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String paymentMethod; // COD or ONLINE
    private String paymentStatus; // e.g., PENDING, PAID
    private String paymentReference;
    private LocalDateTime paymentDate;
    private LocalDateTime paymentExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderDetail> orderDetails;

    // Constructors
    public Order() {}

    public Order(int customerId) {
        this.customerId = customerId;
        this.status = "PENDING";
        this.orderDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Date getOrderDateAsDate() {
        if (orderDate == null) {
            return null;
        }
        return Date.from(orderDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }

    public Date getDeliveryDateAsDate() {
        if (deliveryDate == null) {
            return null;
        }
        return Date.from(deliveryDate.atZone(ZoneId.systemDefault()).toInstant());
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public LocalDateTime getPaymentExpiresAt() { return paymentExpiresAt; }
    public void setPaymentExpiresAt(LocalDateTime paymentExpiresAt) { this.paymentExpiresAt = paymentExpiresAt; }

    public java.util.Date getPaymentDateAsDate() {
        if (paymentDate == null) return null;
        return java.util.Date.from(paymentDate.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    public java.util.Date getPaymentExpiresAtAsDate() {
        if (paymentExpiresAt == null) return null;
        return java.util.Date.from(paymentExpiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    public boolean isPaymentExpired() {
        return paymentExpiresAt != null && LocalDateTime.now().isAfter(paymentExpiresAt);
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

package com.javaweb.model;

import java.io.Serializable;

/**
 * Admin - Administrator user class
 */
public class Admin extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String adminLevel; // SUPER_ADMIN, ADMIN, MODERATOR
    private String department;
    private boolean canManageUsers;
    private boolean canManageOrders;
    private boolean canManageFinance;

    // Constructors
    public Admin() {
        super();
    }

    public Admin(String username, String password, String email, String fullName) {
        super(username, password, email, fullName);
        this.setRole("ADMIN");
        this.adminLevel = "ADMIN";
    }

    // Getters and Setters
    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isCanManageUsers() {
        return canManageUsers;
    }

    public void setCanManageUsers(boolean canManageUsers) {
        this.canManageUsers = canManageUsers;
    }

    public boolean isCanManageOrders() {
        return canManageOrders;
    }

    public void setCanManageOrders(boolean canManageOrders) {
        this.canManageOrders = canManageOrders;
    }

    public boolean isCanManageFinance() {
        return canManageFinance;
    }

    public void setCanManageFinance(boolean canManageFinance) {
        this.canManageFinance = canManageFinance;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminLevel='" + adminLevel + '\'' +
                ", username='" + this.getUsername() + '\'' +
                ", fullName='" + this.getFullName() + '\'' +
                '}';
    }
}

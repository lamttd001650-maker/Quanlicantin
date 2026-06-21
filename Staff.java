package com.javaweb.model;

import java.io.Serializable;

/**
 * Staff - Staff member user class
 */
public class Staff extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String staffCode;
    private String position;
    private double salary;
    private String department;
    private String shift; // CA SÁNG, CA CHIỀU, CA TỐI

    // Constructors
    public Staff() {
        super();
    }

    public Staff(String username, String password, String email, String fullName) {
        super(username, password, email, fullName);
        this.setRole("STAFF");
    }

    // Getters and Setters
    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "staffCode='" + staffCode + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", username='" + this.getUsername() + '\'' +
                ", fullName='" + this.getFullName() + '\'' +
                '}';
    }
}

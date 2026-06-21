package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Customer - Customer/Student user class
 */
public class Customer extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentCode;
    private LocalDate dateOfBirth;
    private String address;
    private String className; // Lớp
    private double balance;

    // Constructors
    public Customer() {
        super();
    }

    public Customer(String username, String password, String email, String fullName) {
        super(username, password, email, fullName);
        this.setRole("CUSTOMER");
    }

    // Getters and Setters
    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "studentCode='" + studentCode + '\'' +
                ", className='" + className + '\'' +
                ", balance=" + balance +
                ", username='" + this.getUsername() + '\'' +
                ", fullName='" + this.getFullName() + '\'' +
                '}';
    }
}

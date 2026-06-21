package com.javaweb.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * RegistrationRequest - Model for pending user registration requests
 */
public class RegistrationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int registrationId;
    private String username;
    private String email;
    private String fullName;
    private String userType; // CUSTOMER, STAFF
    private String phone;
    private String studentCode;
    private String staffCode;
    private String position;
    private String department;
    private String className;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private int reviewedBy;
    private String notes;
    private String additionalInfo;

    // Constructors
    public RegistrationRequest() {
    }

    public RegistrationRequest(String username, String email, String fullName, String userType) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public int getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(int reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "registrationId=" + registrationId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", userType='" + userType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

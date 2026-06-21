package com.javaweb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Utility class for database connection
 * Configuration: Update the constants below with your database details
 */
public class DatabaseConnection {

    // Database Configuration (SQL Server is the default for the provided schema).
    private static final String DB_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=canteen_db;encrypt=false;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123456"; // Thay mật khẩu cho tài khoản SQL Server

    // Connection pool (optional)
    private static Connection connection = null;

    /**
     * Get database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load DB driver
            Class.forName(DB_DRIVER);
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully!");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get connection with custom parameters
     */
    public static Connection getConnection(String url, String user, String password) throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }

    /**
     * Close connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed!");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                closeConnection(conn);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        // Test connection
        if (testConnection()) {
            System.out.println("✓ Database connection successful!");
        } else {
            System.out.println("✗ Database connection failed!");
        }
    }
}

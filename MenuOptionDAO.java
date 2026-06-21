package com.javaweb.util;

import com.javaweb.model.MenuOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuOptionDAO - Xử lý tất cả database operations cho MenuOption
 */
public class MenuOptionDAO {

    /**
     * Lấy tất cả options cho một sản phẩm
     */
    public List<MenuOption> getOptionsByItem(int itemId) {
        String sql = "SELECT * FROM menu_options WHERE item_id = ? AND available = 1 " +
                     "ORDER BY option_type, option_name";
        
        List<MenuOption> options = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                options.add(mapResultSetToMenuOption(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching menu options: " + e.getMessage());
        }
        
        return options;
    }

    /**
     * Lấy options theo loại (SIZE, TOPPING, etc)
     */
    public List<MenuOption> getOptionsByType(int itemId, String optionType) {
        String sql = "SELECT * FROM menu_options WHERE item_id = ? AND option_type = ? AND available = 1 " +
                     "ORDER BY option_name";
        
        List<MenuOption> options = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, itemId);
            pstmt.setString(2, optionType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                options.add(mapResultSetToMenuOption(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching options by type: " + e.getMessage());
        }
        
        return options;
    }

    /**
     * Thêm option mới cho sản phẩm
     */
    public boolean addOption(MenuOption option) {
        String sql = "INSERT INTO menu_options (item_id, option_type, option_name, price_adjustment, available) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, option.getItemId());
            pstmt.setString(2, option.getOptionType());
            pstmt.setString(3, option.getOptionName());
            pstmt.setDouble(4, option.getPriceAdjustment());
            pstmt.setBoolean(5, option.isAvailable());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding menu option: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa option
     */
    public boolean deleteOption(int optionId) {
        String sql = "DELETE FROM menu_options WHERE option_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, optionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting menu option: " + e.getMessage());
            return false;
        }
    }

    /**
     * Helper: Map ResultSet to MenuOption object
     */
    private MenuOption mapResultSetToMenuOption(ResultSet rs) throws SQLException {
        MenuOption option = new MenuOption();
        option.setOptionId(rs.getInt("option_id"));
        option.setItemId(rs.getInt("item_id"));
        option.setOptionType(rs.getString("option_type"));
        option.setOptionName(rs.getString("option_name"));
        option.setPriceAdjustment(rs.getDouble("price_adjustment"));
        option.setAvailable(rs.getBoolean("available"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            option.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        return option;
    }
}

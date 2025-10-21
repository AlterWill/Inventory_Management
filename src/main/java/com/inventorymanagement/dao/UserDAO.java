package com.inventorymanagement.dao;

import com.inventorymanagement.Database;
import com.inventorymanagement.models.User;
import com.inventorymanagement.storage.InMemoryStorage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticate(String username, String password) {
        if (Database.isUsingFallbackMode()) {
            return InMemoryStorage.authenticateUser(username, password);
        }
        
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = true";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean createUser(String username, String password, String fullName, String role) {
        if (Database.isUsingFallbackMode()) {
            return InMemoryStorage.createUser(username, password, fullName, role);
        }
        
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, role);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<User> getAllEmployees() {
        if (Database.isUsingFallbackMode()) {
            return InMemoryStorage.getAllEmployees();
        }
        
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'EMPLOYEE' ORDER BY created_at DESC";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    public boolean updateUser(int userId, String fullName, boolean isActive) {
        if (Database.isUsingFallbackMode()) {
            return InMemoryStorage.updateUser(userId, fullName, isActive);
        }
        
        String sql = "UPDATE users SET full_name = ?, is_active = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fullName);
            pstmt.setBoolean(2, isActive);
            pstmt.setInt(3, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updatePassword(int userId, String newPassword) {
        if (Database.isUsingFallbackMode()) {
            return InMemoryStorage.updatePassword(userId, newPassword);
        }
        
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteUser(int userId) {
        if (Database.isUsingFallbackMode()) {
            return InMemoryStorage.deleteUser(userId);
        }
        
        String sql = "DELETE FROM users WHERE id = ? AND role != 'ADMIN'";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}

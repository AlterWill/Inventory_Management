package com.inventorymanagement.dao;

import com.inventorymanagement.Database;
import com.inventorymanagement.models.Transaction;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    public boolean addTransaction(Transaction transaction) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // Insert transaction
            String sql = "INSERT INTO transactions (product_id, user_id, transaction_type, quantity, price_per_unit, total_amount, tax_amount, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, transaction.getProductId());
            pstmt.setInt(2, transaction.getUserId());
            pstmt.setString(3, transaction.getTransactionType());
            pstmt.setInt(4, transaction.getQuantity());
            pstmt.setBigDecimal(5, transaction.getPricePerUnit());
            pstmt.setBigDecimal(6, transaction.getTotalAmount());
            pstmt.setBigDecimal(7, transaction.getTaxAmount());
            pstmt.setString(8, transaction.getNotes());
            
            pstmt.executeUpdate();
            
            // Update product stock
            ProductDAO productDAO = new ProductDAO();
            var product = productDAO.getProductById(transaction.getProductId());
            
            if (product != null) {
                int newStock = product.getStockQuantity();
                
                if ("SALE".equals(transaction.getTransactionType())) {
                    newStock -= transaction.getQuantity();
                } else if ("PURCHASE".equals(transaction.getTransactionType()) || "RETURN".equals(transaction.getTransactionType())) {
                    newStock += transaction.getQuantity();
                }
                
                if (newStock < 0) {
                    conn.rollback();
                    return false;
                }
                
                String updateStockSql = "UPDATE products SET stock_quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateStockSql);
                updateStmt.setInt(1, newStock);
                updateStmt.setInt(2, transaction.getProductId());
                updateStmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, p.name as product_name, u.username 
            FROM transactions t 
            LEFT JOIN products p ON t.product_id = p.id 
            LEFT JOIN users u ON t.user_id = u.id 
            ORDER BY t.transaction_date DESC
        """;
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    public List<Transaction> getTransactionsByType(String type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, p.name as product_name, u.username 
            FROM transactions t 
            LEFT JOIN products p ON t.product_id = p.id 
            LEFT JOIN users u ON t.user_id = u.id 
            WHERE t.transaction_type = ?
            ORDER BY t.transaction_date DESC
        """;
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    public BigDecimal getTotalSales() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM transactions WHERE transaction_type = 'SALE'";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalTaxCollected() {
        String sql = "SELECT COALESCE(SUM(tax_amount), 0) as total FROM transactions WHERE transaction_type = 'SALE'";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setProductId(rs.getInt("product_id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setQuantity(rs.getInt("quantity"));
        transaction.setPricePerUnit(rs.getBigDecimal("price_per_unit"));
        transaction.setTotalAmount(rs.getBigDecimal("total_amount"));
        transaction.setTaxAmount(rs.getBigDecimal("tax_amount"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date"));
        transaction.setNotes(rs.getString("notes"));
        
        try {
            transaction.setProductName(rs.getString("product_name"));
            transaction.setUsername(rs.getString("username"));
        } catch (SQLException e) {
            // Fields might not exist in all queries
        }
        
        return transaction;
    }
}

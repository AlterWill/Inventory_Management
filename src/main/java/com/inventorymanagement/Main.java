package com.inventorymanagement;
import com.inventorymanagement.ui.LoginUI;

public class Main {
    public static void main(String[] args) {
        // Initialize database connection and create tables
        System.out.println("Starting Inventory Management System...");
        Database.initializeDatabase();
        
        // Launch the login UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginUI();
        });
    }
}

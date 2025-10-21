package com.inventorymanagement;

import java.sql.*;

public class Database {
    private static final String URL = "jdbc:postgresql://localhost:5432/inventory_db"; 
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static Connection connection = null;
    private static boolean useFallbackMode = false;

    public static Connection getConnection() {
        if (useFallbackMode) {
            return null; // Return null to indicate in-memory mode
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            enableFallbackMode();
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn != null ? conn.createStatement() : null) {

            if (conn == null || stmt == null) {
                System.out.println("!! Using IN-MEMORY FALLBACK MODE - Data will not be persisted!");
                return;
            }

            // Create users table (employees and admin)
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    full_name VARCHAR(100) NOT NULL,
                    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EMPLOYEE')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_active BOOLEAN DEFAULT true
                )
            """;
            stmt.execute(createUsersTable);

            // Create products table
            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    category VARCHAR(50),
                    price DECIMAL(10, 2) NOT NULL,
                    stock_quantity INTEGER NOT NULL DEFAULT 0,
                    minimum_stock INTEGER DEFAULT 10,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createProductsTable);

            // Create transactions table
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id SERIAL PRIMARY KEY,
                    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
                    user_id INTEGER REFERENCES users(id),
                    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('SALE', 'PURCHASE', 'RETURN')),
                    quantity INTEGER NOT NULL,
                    price_per_unit DECIMAL(10, 2) NOT NULL,
                    total_amount DECIMAL(10, 2) NOT NULL,
                    tax_amount DECIMAL(10, 2) DEFAULT 0,
                    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    notes TEXT
                )
            """;
            stmt.execute(createTransactionsTable);

            // Insert default admin if not exists
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                String insertAdmin = """
                    INSERT INTO users (username, password, full_name, role) 
                    VALUES ('admin', 'admin123', 'System Administrator', 'ADMIN')
                """;
                stmt.execute(insertAdmin);
                System.out.println("Default admin created (username: admin, password: admin123)");
            }

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            enableFallbackMode();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isUsingFallbackMode() {
        return useFallbackMode;
    }
    
    private static void enableFallbackMode() {
        if (!useFallbackMode) {
            useFallbackMode = true;
            System.out.println("\n" + "=".repeat(60));
            System.out.println("DATABASE CONNECTION FAILED!");
            System.out.println("FALLBACK MODE ENABLED - Using In-Memory Storage");
            System.out.println("All data will be lost when the application closes!");
            System.out.println("=".repeat(60) + "\n");
        }
    }
}

package com.inventorymanagement.storage;

import com.inventorymanagement.models.Product;
import com.inventorymanagement.models.Transaction;
import com.inventorymanagement.models.User;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryStorage {
    private static final Map<Integer, User> users = new ConcurrentHashMap<>();
    private static final Map<Integer, Product> products = new ConcurrentHashMap<>();
    private static final Map<Integer, Transaction> transactions = new ConcurrentHashMap<>();
    
    private static final AtomicInteger userIdGenerator = new AtomicInteger(1);
    private static final AtomicInteger productIdGenerator = new AtomicInteger(1);
    private static final AtomicInteger transactionIdGenerator = new AtomicInteger(1);
    
    static {
        // Initialize with default admin
        User admin = new User();
        admin.setId(userIdGenerator.getAndIncrement());
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setFullName("System Administrator");
        admin.setRole("ADMIN");
        admin.setActive(true);
        admin.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        users.put(admin.getId(), admin);
        
        System.out.println("In-Memory Storage initialized with default admin");
    }
    
    // User operations
    public static User authenticateUser(String username, String password) {
        return users.values().stream()
            .filter(u -> u.getUsername().equals(username) && 
                        u.getPassword().equals(password) && 
                        u.isActive())
            .findFirst()
            .orElse(null);
    }
    
    public static boolean createUser(String username, String password, String fullName, String role) {
        // Check if username exists
        boolean exists = users.values().stream()
            .anyMatch(u -> u.getUsername().equals(username));
        
        if (exists) {
            return false;
        }
        
        User user = new User();
        user.setId(userIdGenerator.getAndIncrement());
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        users.put(user.getId(), user);
        return true;
    }
    
    public static List<User> getAllEmployees() {
        return users.values().stream()
            .filter(u -> "EMPLOYEE".equals(u.getRole()))
            .collect(Collectors.toList());
    }
    
    public static boolean updateUser(int userId, String fullName, boolean isActive) {
        User user = users.get(userId);
        if (user != null) {
            user.setFullName(fullName);
            user.setActive(isActive);
            return true;
        }
        return false;
    }
    
    public static boolean deleteUser(int userId) {
        User user = users.get(userId);
        if (user != null && !"ADMIN".equals(user.getRole())) {
            users.remove(userId);
            return true;
        }
        return false;
    }
    
    // Product operations
    public static boolean addProduct(Product product) {
        product.setId(productIdGenerator.getAndIncrement());
        product.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        product.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        products.put(product.getId(), product);
        return true;
    }
    
    public static List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
    
    public static Product getProductById(int id) {
        return products.get(id);
    }
    
    public static boolean updateProduct(Product product) {
        if (products.containsKey(product.getId())) {
            product.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            products.put(product.getId(), product);
            return true;
        }
        return false;
    }
    
    public static boolean updateStock(int productId, int newQuantity) {
        Product product = products.get(productId);
        if (product != null) {
            product.setStockQuantity(newQuantity);
            product.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            return true;
        }
        return false;
    }
    
    public static boolean deleteProduct(int productId) {
        return products.remove(productId) != null;
    }
    
    public static List<Product> getLowStockProducts() {
        return products.values().stream()
            .filter(Product::isLowStock)
            .collect(Collectors.toList());
    }
    
    // Transaction operations
    public static boolean addTransaction(Transaction transaction) {
        // Check stock availability for sales
        Product product = products.get(transaction.getProductId());
        if (product == null) {
            return false;
        }
        
        int newStock = product.getStockQuantity();
        
        if ("SALE".equals(transaction.getTransactionType())) {
            newStock -= transaction.getQuantity();
        } else if ("PURCHASE".equals(transaction.getTransactionType()) || 
                   "RETURN".equals(transaction.getTransactionType())) {
            newStock += transaction.getQuantity();
        }
        
        if (newStock < 0) {
            return false; // Insufficient stock
        }
        
        // Update stock
        product.setStockQuantity(newStock);
        product.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Add transaction
        transaction.setId(transactionIdGenerator.getAndIncrement());
        transaction.setTransactionDate(new Timestamp(System.currentTimeMillis()));
        
        // Set product and user names for display
        transaction.setProductName(product.getName());
        User user = users.get(transaction.getUserId());
        if (user != null) {
            transaction.setUsername(user.getUsername());
        }
        
        transactions.put(transaction.getId(), transaction);
        return true;
    }
    
    public static List<Transaction> getAllTransactions() {
        return transactions.values().stream()
            .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
            .collect(Collectors.toList());
    }
    
    public static List<Transaction> getTransactionsByType(String type) {
        return transactions.values().stream()
            .filter(t -> type.equals(t.getTransactionType()))
            .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
            .collect(Collectors.toList());
    }
    
    public static java.math.BigDecimal getTotalSales() {
        return transactions.values().stream()
            .filter(t -> "SALE".equals(t.getTransactionType()))
            .map(Transaction::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    public static java.math.BigDecimal getTotalTaxCollected() {
        return transactions.values().stream()
            .filter(t -> "SALE".equals(t.getTransactionType()))
            .map(Transaction::getTaxAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    // Clear all data (for testing)
    public static void clearAll() {
        users.clear();
        products.clear();
        transactions.clear();
        userIdGenerator.set(1);
        productIdGenerator.set(1);
        transactionIdGenerator.set(1);
    }
}

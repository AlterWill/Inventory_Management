package com.inventorymanagement.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.inventorymanagement.dao.ProductDAO;
import com.inventorymanagement.dao.TransactionDAO;
import com.inventorymanagement.dao.UserDAO;
import com.inventorymanagement.models.Product;
import com.inventorymanagement.models.Transaction;
import com.inventorymanagement.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class AdminDashboard extends JFrame {
    private User currentUser;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private TransactionDAO transactionDAO;
    private JTable employeeTable;
    private JTable productTable;
    private JTable transactionTable;
    private DefaultTableModel employeeTableModel;
    private DefaultTableModel productTableModel;
    private DefaultTableModel transactionTableModel;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    public AdminDashboard(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        this.productDAO = new ProductDAO();
        this.transactionDAO = new TransactionDAO();
        initializeUI();
        loadEmployees();
        loadProducts();
        loadTransactions();
    }

    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Admin Dashboard - " + currentUser.getFullName());
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Admin Dashboard - " + currentUser.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginUI();
        });

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Employee Management", createEmployeePanel());
        tabbedPane.addTab("Products", createProductsPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Admin Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        
        panel.add(new JLabel("New Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton changePasswordButton = new JButton("Change Password");
        panel.add(changePasswordButton, gbc);

        changePasswordButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to change your password?",
                    "Confirm Password Change",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (userDAO.updatePassword(currentUser.getId(), newPassword)) {
                    JOptionPane.showMessageDialog(this, "Password updated successfully!");
                    newPasswordField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update password!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;

        // Title
        JLabel titleLabel = new JLabel("System Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Statistics Cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        
        JPanel card1 = createStatCard("Total Products", "0", new Color(52, 152, 219));
        JPanel card2 = createStatCard("Total Sales", "$0.00", new Color(46, 204, 113));
        JPanel card3 = createStatCard("Total Employees", "0", new Color(155, 89, 182));
        JPanel card4 = createStatCard("Low Stock Items", "0", new Color(231, 76, 60));

        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);
        statsPanel.add(card4);

        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(statsPanel, gbc);

        // Refresh Button
        gbc.gridy++;
        gbc.weighty = 0;
        JButton refreshButton = new JButton("Refresh Dashboard");
        refreshButton.setPreferredSize(new Dimension(200, 40));
        refreshButton.addActionListener(e -> {
            List<Product> products = productDAO.getAllProducts();
            ((JLabel) ((JPanel) card1).getComponent(1)).setText(String.valueOf(products.size()));
            
            BigDecimal totalSales = transactionDAO.getTotalSales();
            ((JLabel) ((JPanel) card2).getComponent(1)).setText("$" + totalSales.toString());
            
            List<User> employees = userDAO.getAllEmployees();
            ((JLabel) ((JPanel) card3).getComponent(1)).setText(String.valueOf(employees.size()));
            
            List<Product> lowStock = productDAO.getLowStockProducts();
            ((JLabel) ((JPanel) card4).getComponent(1)).setText(String.valueOf(lowStock.size()));
        });
        panel.add(refreshButton, gbc);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(color);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Employee Table
        String[] columns = {"ID", "Username", "Full Name", "Role", "Status", "Created"};
        employeeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(employeeTableModel);
        JScrollPane scrollPane = new JScrollPane(employeeTable);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton addButton = new JButton("Add Employee");
        JButton editButton = new JButton("Edit Employee");
        JButton deleteButton = new JButton("Delete Employee");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showAddEmployeeDialog());
        editButton.addActionListener(e -> showEditEmployeeDialog());
        deleteButton.addActionListener(e -> deleteEmployee());
        refreshButton.addActionListener(e -> loadEmployees());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Product Table
        String[] columns = {"ID", "Name", "Category", "Price", "Stock", "Min Stock", "Status"};
        productTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton addButton = new JButton("Add Product");
        JButton editButton = new JButton("Edit Product");
        JButton deleteButton = new JButton("Delete Product");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> showAddProductDialog());
        editButton.addActionListener(e -> showEditProductDialog());
        deleteButton.addActionListener(e -> deleteProduct());
        refreshButton.addActionListener(e -> loadProducts());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Transaction Table
        String[] columns = {"ID", "Product", "User", "Type", "Quantity", "Price/Unit", "Total", "Tax", "Date"};
        transactionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(transactionTableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton saleButton = new JButton("Record Sale");
        JButton purchaseButton = new JButton("Record Purchase");
        JButton returnButton = new JButton("Record Return");
        JButton refreshButton = new JButton("Refresh");

        saleButton.addActionListener(e -> showTransactionDialog("SALE"));
        purchaseButton.addActionListener(e -> showTransactionDialog("PURCHASE"));
        returnButton.addActionListener(e -> showTransactionDialog("RETURN"));
        refreshButton.addActionListener(e -> loadTransactions());

        buttonPanel.add(saleButton);
        buttonPanel.add(purchaseButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Business Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Total Sales
        JLabel totalSalesLabel = new JLabel("Total Sales Revenue:");
        totalSalesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        panel.add(totalSalesLabel, gbc);

        JLabel totalSalesValue = new JLabel("$0.00");
        totalSalesValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        panel.add(totalSalesValue, gbc);

        // Total Tax
        gbc.gridy++;
        JLabel totalTaxLabel = new JLabel("Total Tax Collected:");
        totalTaxLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        panel.add(totalTaxLabel, gbc);

        JLabel totalTaxValue = new JLabel("$0.00");
        totalTaxValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        panel.add(totalTaxValue, gbc);

        // Net Revenue
        gbc.gridy++;
        JLabel netRevenueLabel = new JLabel("Net Revenue (Sales - Tax):");
        netRevenueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        panel.add(netRevenueLabel, gbc);

        JLabel netRevenueValue = new JLabel("$0.00");
        netRevenueValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 1;
        panel.add(netRevenueValue, gbc);

        // Low Stock Alert
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lowStockLabel = new JLabel("Low Stock Products Alert:");
        lowStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lowStockLabel, gbc);

        gbc.gridy++;
        JTextArea lowStockArea = new JTextArea(12, 50);
        lowStockArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(lowStockArea);
        panel.add(scrollPane, gbc);

        // Refresh Button
        gbc.gridy++;
        JButton refreshButton = new JButton("Refresh Reports");
        refreshButton.setPreferredSize(new Dimension(150, 35));
        refreshButton.addActionListener(e -> {
            BigDecimal totalSales = transactionDAO.getTotalSales();
            BigDecimal totalTax = transactionDAO.getTotalTaxCollected();
            BigDecimal netRevenue = totalSales.subtract(totalTax);
            
            totalSalesValue.setText("$" + totalSales.toString());
            totalTaxValue.setText("$" + totalTax.toString());
            netRevenueValue.setText("$" + netRevenue.toString());

            List<Product> lowStockProducts = productDAO.getLowStockProducts();
            StringBuilder sb = new StringBuilder();
            if (lowStockProducts.isEmpty()) {
                sb.append("All products are adequately stocked!");
            } else {
                for (Product p : lowStockProducts) {
                    sb.append(String.format("⚠ %s - Current: %d, Minimum: %d, Category: %s\n", 
                        p.getName(), p.getStockQuantity(), p.getMinimumStock(), p.getCategory()));
                }
            }
            lowStockArea.setText(sb.toString());
        });
        panel.add(refreshButton, gbc);

        return panel;
    }

    private void loadEmployees() {
        employeeTableModel.setRowCount(0);
        List<User> employees = userDAO.getAllEmployees();
        for (User u : employees) {
            String status = u.isActive() ? "Active" : "Inactive";
            employeeTableModel.addRow(new Object[]{
                u.getId(), u.getUsername(), u.getFullName(),
                u.getRole(), status, u.getCreatedAt()
            });
        }
    }

    private void loadProducts() {
        productTableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();
        for (Product p : products) {
            String status = p.isLowStock() ? "LOW STOCK" : "OK";
            productTableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getCategory(),
                "$" + p.getPrice(), p.getStockQuantity(),
                p.getMinimumStock(), status
            });
        }
    }

    private void loadTransactions() {
        transactionTableModel.setRowCount(0);
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        for (Transaction t : transactions) {
            transactionTableModel.addRow(new Object[]{
                t.getId(), t.getProductName(), t.getUsername(),
                t.getTransactionType(), t.getQuantity(),
                "$" + t.getPricePerUnit(), "$" + t.getTotalAmount(),
                "$" + t.getTaxAmount(), t.getTransactionDate()
            });
        }
    }

    private void showAddEmployeeDialog() {
        JDialog dialog = new JDialog(this, "Add Employee", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField fullNameField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        panel.add(fullNameField, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required!");
                return;
            }

            if (userDAO.createUser(username, password, fullName, "EMPLOYEE")) {
                JOptionPane.showMessageDialog(dialog, "Employee added successfully!");
                loadEmployees();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add employee! Username may already exist.");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditEmployeeDialog() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to edit");
            return;
        }

        int userId = (int) employeeTableModel.getValueAt(selectedRow, 0);
        String fullName = (String) employeeTableModel.getValueAt(selectedRow, 2);
        String status = (String) employeeTableModel.getValueAt(selectedRow, 4);
        boolean isActive = status.equals("Active");

        JDialog dialog = new JDialog(this, "Edit Employee", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField fullNameField = new JTextField(fullName, 20);
        JCheckBox activeCheckBox = new JCheckBox("Active", isActive);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        panel.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        panel.add(activeCheckBox, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (userDAO.updateUser(userId, fullNameField.getText().trim(), activeCheckBox.isSelected())) {
                JOptionPane.showMessageDialog(dialog, "Employee updated successfully!");
                loadEmployees();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update employee!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete");
            return;
        }

        int userId = (int) employeeTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this employee?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "Employee deleted successfully!");
                loadEmployees();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete employee!");
            }
        }
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add Product", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JTextField categoryField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextField stockField = new JTextField(20);
        JTextField minStockField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Minimum Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(minStockField, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                Product product = new Product();
                product.setName(nameField.getText());
                product.setDescription(descArea.getText());
                product.setCategory(categoryField.getText());
                product.setPrice(new BigDecimal(priceField.getText()));
                product.setStockQuantity(Integer.parseInt(stockField.getText()));
                product.setMinimumStock(Integer.parseInt(minStockField.getText()));

                if (productDAO.addProduct(product)) {
                    JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add product!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditProductDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit");
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        Product product = productDAO.getProductById(productId);

        if (product == null) return;

        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(product.getName(), 20);
        JTextArea descArea = new JTextArea(product.getDescription(), 3, 20);
        JTextField categoryField = new JTextField(product.getCategory(), 20);
        JTextField priceField = new JTextField(product.getPrice().toString(), 20);
        JTextField stockField = new JTextField(String.valueOf(product.getStockQuantity()), 20);
        JTextField minStockField = new JTextField(String.valueOf(product.getMinimumStock()), 20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Minimum Stock:"), gbc);
        gbc.gridx = 1;
        panel.add(minStockField, gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                product.setName(nameField.getText());
                product.setDescription(descArea.getText());
                product.setCategory(categoryField.getText());
                product.setPrice(new BigDecimal(priceField.getText()));
                product.setStockQuantity(Integer.parseInt(stockField.getText()));
                product.setMinimumStock(Integer.parseInt(minStockField.getText()));

                if (productDAO.updateProduct(product)) {
                    JOptionPane.showMessageDialog(dialog, "Product updated successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update product!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete");
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productDAO.deleteProduct(productId)) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product!");
            }
        }
    }

    private void showTransactionDialog(String type) {
        JDialog dialog = new JDialog(this, "Record " + type, true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Product> products = productDAO.getAllProducts();
        JComboBox<String> productCombo = new JComboBox<>();
        for (Product p : products) {
            productCombo.addItem(p.getId() + " - " + p.getName() + " (Stock: " + p.getStockQuantity() + ")");
        }

        JTextField quantityField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JTextArea notesArea = new JTextArea(3, 20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Product:"), gbc);
        gbc.gridx = 1;
        panel.add(productCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        panel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Price per Unit:"), gbc);
        gbc.gridx = 1;
        panel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        panel.add(new JScrollPane(notesArea), gbc);

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                String selected = (String) productCombo.getSelectedItem();
                int productId = Integer.parseInt(selected.split(" - ")[0]);
                int quantity = Integer.parseInt(quantityField.getText());
                BigDecimal pricePerUnit = new BigDecimal(priceField.getText());
                BigDecimal totalAmount = pricePerUnit.multiply(new BigDecimal(quantity));
                BigDecimal taxAmount = type.equals("SALE") ? totalAmount.multiply(TAX_RATE) : BigDecimal.ZERO;

                Transaction transaction = new Transaction();
                transaction.setProductId(productId);
                transaction.setUserId(currentUser.getId());
                transaction.setTransactionType(type);
                transaction.setQuantity(quantity);
                transaction.setPricePerUnit(pricePerUnit);
                transaction.setTotalAmount(totalAmount.add(taxAmount));
                transaction.setTaxAmount(taxAmount);
                transaction.setNotes(notesArea.getText());

                if (transactionDAO.addTransaction(transaction)) {
                    JOptionPane.showMessageDialog(dialog,
                        type + " recorded successfully!\nTotal: $" + totalAmount.add(taxAmount));
                    loadTransactions();
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to record transaction! Check stock availability.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}

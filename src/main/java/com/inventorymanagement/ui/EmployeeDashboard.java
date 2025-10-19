package com.inventorymanagement.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.inventorymanagement.dao.ProductDAO;
import com.inventorymanagement.dao.TransactionDAO;
import com.inventorymanagement.models.Product;
import com.inventorymanagement.models.Transaction;
import com.inventorymanagement.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class EmployeeDashboard extends JFrame {
    private User currentUser;
    private ProductDAO productDAO;
    private TransactionDAO transactionDAO;
    private JTable productTable;
    private JTable transactionTable;
    private DefaultTableModel productTableModel;
    private DefaultTableModel transactionTableModel;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    public EmployeeDashboard(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.transactionDAO = new TransactionDAO();
        initializeUI();
        loadProducts();
        loadTransactions();
    }

    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Employee Dashboard - " + currentUser.getFullName());
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel with Welcome and Logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (Employee)");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginUI();
        });

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Products", createProductsPanel());
        tabbedPane.addTab("Transactions", createTransactionsPanel());
        tabbedPane.addTab("Reports", createReportsPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
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
        String[] columns = {"ID", "Product", "Type", "Quantity", "Price/Unit", "Total", "Tax", "Date"};
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

        JLabel titleLabel = new JLabel("Sales Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        // Total Sales
        JLabel totalSalesLabel = new JLabel("Total Sales:");
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

        // Low Stock Products
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lowStockLabel = new JLabel("Low Stock Products:");
        lowStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lowStockLabel, gbc);

        gbc.gridy++;
        JTextArea lowStockArea = new JTextArea(10, 40);
        lowStockArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(lowStockArea);
        panel.add(scrollPane, gbc);

        // Refresh Button
        gbc.gridy++;
        JButton refreshButton = new JButton("Refresh Reports");
        refreshButton.addActionListener(e -> {
            BigDecimal totalSales = transactionDAO.getTotalSales();
            BigDecimal totalTax = transactionDAO.getTotalTaxCollected();
            totalSalesValue.setText("$" + totalSales.toString());
            totalTaxValue.setText("$" + totalTax.toString());

            List<Product> lowStockProducts = productDAO.getLowStockProducts();
            StringBuilder sb = new StringBuilder();
            for (Product p : lowStockProducts) {
                sb.append(String.format("%s - Stock: %d (Min: %d)\n", 
                    p.getName(), p.getStockQuantity(), p.getMinimumStock()));
            }
            lowStockArea.setText(sb.toString());
        });
        panel.add(refreshButton, gbc);

        return panel;
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
                t.getId(), t.getProductName(), t.getTransactionType(),
                t.getQuantity(), "$" + t.getPricePerUnit(),
                "$" + t.getTotalAmount(), "$" + t.getTaxAmount(),
                t.getTransactionDate()
            });
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
                    loadProducts(); // Refresh to show updated stock
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

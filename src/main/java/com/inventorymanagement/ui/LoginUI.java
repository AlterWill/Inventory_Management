package com.inventorymanagement.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.inventorymanagement.Database;
import com.inventorymanagement.dao.UserDAO;
import com.inventorymanagement.models.User;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {
    private UserDAO userDAO;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginUI() {
        userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Inventory Management System - Login");
        setSize(450, Database.isUsingFallbackMode() ? 400 : 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("Inventory Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);
        
        // Add fallback mode warning if applicable
        if (Database.isUsingFallbackMode()) {
            JLabel warningLabel = new JLabel("⚠ IN-MEMORY MODE - Data will not be saved");
            warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            warningLabel.setForeground(new Color(255, 165, 0));
            warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titlePanel.add(Box.createVerticalStrut(5));
            titlePanel.add(warningLabel);
        }

        // Login Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcomeLabel = new JLabel("Please login to continue");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        formPanel.add(welcomeLabel, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Username:"), gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(120, 35));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JButton exitButton = new JButton("Exit");
        exitButton.setPreferredSize(new Dimension(120, 35));

        loginButton.addActionListener(e -> handleLogin());
        exitButton.addActionListener(e -> System.exit(0));

        // Enter key support
        passwordField.addActionListener(e -> handleLogin());

        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);

        // Info Panel
        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("<html><center>Default Admin: username=<b>admin</b>, password=<b>admin123</b></center></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(infoLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

        add(mainPanel);
        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            String welcomeMessage = "Welcome, " + user.getFullName() + "!";
            if (Database.isUsingFallbackMode()) {
                welcomeMessage += "\n\n⚠ Running in IN-MEMORY mode\nData will not be persisted!";
            }
            
            JOptionPane.showMessageDialog(this,
                    welcomeMessage,
                    "Login Successful",
                    Database.isUsingFallbackMode() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

            // Open appropriate dashboard based on role
            if (user.isAdmin()) {
                new AdminDashboard(user);
            } else {
                new EmployeeDashboard(user);
            }

            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}

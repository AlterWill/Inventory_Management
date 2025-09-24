package com.app;

import javax.swing.*;
import com.inventorymanagement.Authenticator;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.ui.FlatButtonUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Authenticator;

public class LoginUI extends JFrame implements ActionListener {
  public static String state = "login";
  JButton submitButton;
  JTextField usernameField;
  JPasswordField passwordField;
  JButton registerButton;
  JTextField newUsernameField;
  JPasswordField newPasswordField;
  JPasswordField confirmPasswordField;

  public LoginUI() {

    try {
      UIManager.setLookAndFeel(new FlatDarkLaf());
    } catch (Exception e) {
      e.printStackTrace();
    }
    setLayout(new BorderLayout());
    setTitle("Inventory Management System");
    setSize(400, 300);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel nav = new JPanel();
    // nav.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
    nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
    nav.setBackground(UIManager.getColor("TitlePane.background"));

    JTabbedPane tabbedPane = new JTabbedPane();
    nav.add(Box.createVerticalGlue());
    JPanel tav = new JPanel();
    tav.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    tav.add(tabbedPane);
    tav.setBackground(UIManager.getColor("TitlePane.background"));
    nav.add(tav);

    // tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT,
    // FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
    add(nav, BorderLayout.CENTER);
    // Login Panel
    nav.add(Box.createVerticalGlue());

    JPanel loginPanel = new JPanel();
    loginPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    // add(loginPanel, BorderLayout.CENTER);
    JLabel welcomeLabel = new JLabel("Welcome to Inventory Management System");

    welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

    usernameField = new JTextField(15);
    passwordField = new JPasswordField(15);
    submitButton = new JButton("Submit");
    submitButton.addActionListener(this);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10, 10, 10, 10);

    loginPanel.add(welcomeLabel, gbc);
    gbc.gridwidth = 1;
    gbc.gridy++;
    loginPanel.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    loginPanel.add(usernameField, gbc);
    gbc.gridx = 0;
    gbc.gridy++;
    loginPanel.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    loginPanel.add(passwordField, gbc);
    gbc.gridy++;

    loginPanel.add(submitButton, gbc);

    tabbedPane.addTab("Login", loginPanel);

    JPanel signupPanel = new JPanel();
    signupPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.insets = new Insets(5, 5, 5, 5);
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    // add(signupPanel, BorderLayout.CENTER);
    signupPanel.setVisible(false);
    JLabel signupLabel = new JLabel("Create a new account");
    signupLabel.setHorizontalAlignment(SwingConstants.CENTER);
    JTextField newUsernameField = new JTextField(15);
    JPasswordField newPasswordField = new JPasswordField(15);
    JPasswordField confirmPasswordField = new JPasswordField(15);
    registerButton = new JButton("Register");
    gbc2.gridx = 0;
    gbc2.gridy = 0;
    gbc2.gridwidth = 2;
    gbc2.insets = new Insets(10, 10, 10, 10);
    signupPanel.add(signupLabel, gbc2);
    gbc2.gridwidth = 1;
    gbc2.gridy++;
    signupPanel.add(new JLabel("Username:"), gbc2);
    gbc2.gridx = 1;
    signupPanel.add(newUsernameField, gbc2);
    gbc2.gridx = 0;
    gbc2.gridy++;
    signupPanel.add(new JLabel("Password:"), gbc2);
    gbc2.gridx = 1;
    signupPanel.add(newPasswordField, gbc2);
    gbc2.gridx = 0;
    gbc2.gridy++;
    signupPanel.add(new JLabel("Confirm Password:"), gbc2);
    gbc2.gridx = 1;
    signupPanel.add(confirmPasswordField, gbc2);
    gbc2.gridy++;
    signupPanel.add(registerButton, gbc2);
    tabbedPane.addTab("SignUp", signupPanel);

    setVisible(true);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == submitButton) {
      String a = usernameField.getText();
      String b = new String(passwordField.getPassword());
      if (Authenticator.login(a, b)) {
        // go to dashboard here
        System.out.print("Worked");
        this.dispose();
      } else {
        // login error message
        usernameField.setText("");
        passwordField.setText("");
      }
    }
    if (e.getSource() == registerButton) {
      String a = newUsernameField.getText();
      String b = new String(newPasswordField.getPassword());
      String c = new String(confirmPasswordField.getPassword());
      if (b != c || a.trim() == "" || b.trim() == "") {
        // error message
        newUsernameField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        return;
      }
      if (Authenticator.signUp(a, b)) {
        // go to dashboard here or the login page
        System.out.print("Worked");
        this.dispose(); // if going to dashboard only
      } else {
        // signup failed message
        newUsernameField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
      }
    }
  }
}

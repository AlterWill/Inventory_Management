package com.inventorymanagement;

import java.sql.*;

public class Database implements AutoCloseable {
  private Connection conn;
  private final String url;
  private final String username;
  private final String password;
  private final String tableName;

  public Database(String url, String username, String password, String tableName) {
    this.url = url;
    this.username = username;
    this.password = password;
    this.tableName = tableName == null || tableName.isBlank() ? "file_storage" : tableName;
    connectAndInit();
  }

  private void connectAndInit() {
    try {
      // Load driver (modern driver auto-registers but loading explicitly is safe)
      Class.forName("com.mysql.cj.jdbc.Driver");
      conn = DriverManager.getConnection(url, username, password);
      createTableIfNotExists();
    } catch (ClassNotFoundException e) {
      System.err.println("JDBC Driver not found: " + e.getMessage());
    } catch (SQLException e) {
      System.err.println("Failed to connect or initialize database: " + e.getMessage());
    }
  }

  private void createTableIfNotExists() throws SQLException {
    String sql = "CREATE TABLE IF NOT EXISTS `" + tableName + "` ("
        + "`id` INT AUTO_INCREMENT PRIMARY KEY,"
        + "`path` VARCHAR(255) NOT NULL UNIQUE,"
        + "`content` LONGTEXT"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
    try (Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
    }
  }

  /**
   * Read content for a given path. Returns empty string if not present.
   */
  public String readContent(String path) {
    if (path == null)
      return "";
    String sql = "SELECT content FROM `" + tableName + "` WHERE path = ? LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, path);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String content = rs.getString("content");
          return content == null ? "" : content;
        }
      }
    } catch (SQLException e) {
      System.err.println("Error reading content for " + path + ": " + e.getMessage());
    }
    return "";
  }

  /**
   * Write (replace) content for a path. Creates the row if it doesn't exist.
   */
  public void writeContent(String path, String data) {
    if (path == null)
      return;
    String updateSql = "UPDATE `" + tableName + "` SET content = ? WHERE path = ?";
    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
      ps.setString(1, data);
      ps.setString(2, path);
      int rows = ps.executeUpdate();
      if (rows == 0) {
        // insert
        String insertSql = "INSERT INTO `" + tableName + "` (path, content) VALUES (?, ?)";
        try (PreparedStatement ip = conn.prepareStatement(insertSql)) {
          ip.setString(1, path);
          ip.setString(2, data);
          ip.executeUpdate();
        }
      }
    } catch (SQLException e) {
      System.err.println("Error writing content for " + path + ": " + e.getMessage());
    }
  }

  /**
   * Append data to existing content (or create new row if missing).
   */
  public void append(String path, String data) {
    if (path == null)
      return;
    String current = readContent(path);
    String combined = current + (data == null ? "" : data);
    writeContent(path, combined);
  }

  /**
   * Print content to stdout similar to DisplayContent in manageFiles
   */
  public void displayContent(String path) {
    String content = readContent(path);
    System.out.println("Contents of " + path + ":\n" + content);
  }

  @Override
  public void close() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        System.err.println("Error closing connection: " + e.getMessage());
      }
    }
  }
}

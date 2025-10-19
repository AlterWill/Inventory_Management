# Inventory Management System

A comprehensive inventory management software for small businesses built with Java Swing, FlatLaf, and PostgreSQL.

## Features

### For Employees:
- **Product Management**: Add, edit, and delete products with details like name, description, category, price, and stock levels
- **Transaction Recording**: Record sales, purchases, and returns with automatic stock updates
- **Tax Calculation**: Automatic 10% tax calculation on sales
- **Low Stock Alerts**: Visual indicators for products running low on stock
- **Reports**: View total sales, tax collected, and low stock items

### For Administrators:
- **All Employee Features** plus:
- **Employee Management**: Add, edit, activate/deactivate, and delete employee accounts
- **Dashboard**: Overview with statistics cards showing total products, sales, employees, and low stock items
- **Advanced Reports**: Net revenue calculations and comprehensive business analytics
- **Full System Control**: Complete access to all inventory operations

## Technology Stack

- **Language**: Java 24
- **GUI Framework**: Java Swing with FlatLaf Dark theme
- **Database**: PostgreSQL
- **JDBC**: Direct JDBC for database operations
- **Build Tool**: Maven

## Prerequisites

1. Java JDK 24 or higher
2. PostgreSQL database server
3. Maven

## Database Setup

1. Install PostgreSQL on your system
2. Create a database named `inventory_db`:
   ```sql
   CREATE DATABASE inventory_db;
   ```

3. Update database credentials in `Database.java` if needed:
   - Default URL: `jdbc:postgresql://localhost:5432/inventory_db`
   - Default User: `postgres`
   - Default Password: `postgres`

## Installation & Running

1. Clone or download the project

2. Compile the project:
   ```bash
   mvn clean compile
   ```

3. Run the application:
   ```bash
   mvn exec:java
   ```

## Default Login Credentials

**Admin Account:**
- Username: `admin`
- Password: `admin123`

After logging in as admin, you can create employee accounts.

## Database Schema

The application automatically creates the following tables:

### users
- id (PRIMARY KEY)
- username (UNIQUE)
- password
- full_name
- role (ADMIN/EMPLOYEE)
- created_at
- is_active

### products
- id (PRIMARY KEY)
- name
- description
- category
- price
- stock_quantity
- minimum_stock
- created_at
- updated_at

### transactions
- id (PRIMARY KEY)
- product_id (FOREIGN KEY)
- user_id (FOREIGN KEY)
- transaction_type (SALE/PURCHASE/RETURN)
- quantity
- price_per_unit
- total_amount
- tax_amount
- transaction_date
- notes

## Usage Guide

### Employee Workflow:
1. Login with employee credentials
2. **Products Tab**: Manage product inventory
3. **Transactions Tab**: Record sales, purchases, or returns
4. **Reports Tab**: View sales reports and low stock alerts

### Admin Workflow:
1. Login with admin credentials
2. **Dashboard Tab**: View system overview
3. **Employee Management Tab**: Manage employee accounts
4. **Products Tab**: Manage products
5. **Transactions Tab**: Record transactions
6. **Reports Tab**: View detailed business reports

## Features in Detail

### Stock Management
- Automatic stock updates when recording transactions
- Sales decrease stock quantity
- Purchases and returns increase stock quantity
- Low stock warnings when quantity <= minimum stock level

### Tax Calculation
- 10% tax automatically applied to all sales
- Tax amount tracked separately for reporting
- Total amount includes tax

### Transaction Types
- **SALE**: Decreases stock, applies tax
- **PURCHASE**: Increases stock (restocking)
- **RETURN**: Increases stock (customer returns)

## Project Structure

```
src/main/java/com/inventorymanagement/
├── Main.java                    # Application entry point
├── Database.java                # Database connection and initialization
├── models/                      # Data models
│   ├── User.java
│   ├── Product.java
│   └── Transaction.java
├── dao/                         # Data Access Objects
│   ├── UserDAO.java
│   ├── ProductDAO.java
│   └── TransactionDAO.java
└── ui/                          # User Interface
    ├── LoginUI.java
    ├── EmployeeDashboard.java
    └── AdminDashboard.java
```

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running
- Verify database name, username, and password in `Database.java`
- Check if PostgreSQL is listening on port 5432

### Compilation Errors
- Ensure Java 24 is installed: `java -version`
- Clean and rebuild: `mvn clean install`

### UI Issues
- FlatLaf theme should load automatically
- If UI looks incorrect, check FlatLaf dependency in `pom.xml`

## Future Enhancements

Potential features for future versions:
- Password encryption
- User roles and permissions
- Product barcode scanning
- Invoice generation
- Export reports to PDF/Excel
- Multi-warehouse support
- Product images
- Customer management

## License

This project is created for educational and small business use.

## Support

For issues or questions, please check the database connection and ensure all dependencies are properly installed.

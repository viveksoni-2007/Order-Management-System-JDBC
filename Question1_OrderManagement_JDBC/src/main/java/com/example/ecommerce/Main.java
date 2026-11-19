package com.example.ecommerce;

import java.sql.*;
import java.util.Scanner;

public class Main {
    // Update username/password according to your MySQL installation.
    private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Vivek@12345";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert at least three orders");
                System.out.println("2. Search by status (quantity > 2), price descending");
                System.out.println("3. Increase quantity of an order");
                System.out.println("4. Update status (Pending/Shipped/Delivered)");
                System.out.println("5. Display total order value");
                System.out.println("6. Delete an order only if Cancelled");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");

                int choice = readInt(sc);
                try {
                    switch (choice) {
                        case 1 -> insertThreeOrders();
                        case 2 -> {
                            System.out.print("Enter status: ");
                            displayByStatus(sc.nextLine().trim());
                        }
                        case 3 -> {
                            System.out.print("Enter order ID: ");
                            int id = readInt(sc);
                            System.out.print("Enter additional quantity: ");
                            int qty = readInt(sc);
                            increaseQuantity(id, qty);
                        }
                        case 4 -> {
                            System.out.print("Enter order ID: ");
                            int id = readInt(sc);
                            System.out.print("Enter new status (Pending/Shipped/Delivered): ");
                            updateStatus(id, sc.nextLine().trim());
                        }
                        case 5 -> displayTotalValue();
                        case 6 -> {
                            System.out.print("Enter order ID to delete: ");
                            deleteCancelledOrder(readInt(sc));
                        }
                        case 0 -> { System.out.println("Thank you!"); return; }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            }
        }
    }

    private static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("Please enter a valid integer: ");
            sc.next();
        }
        int value = sc.nextInt();
        sc.nextLine(); // consume newline
        return value;
    }

    // Q1(a): PreparedStatement + duplicate primary-key handling.
    private static void insertThreeOrders() throws SQLException {
        String sql = "INSERT INTO Orders(order_id, customer_name, product_name, quantity, unit_price, status) VALUES(?,?,?,?,?,?)";
        Object[][] orders = {
            {101, "Aman", "Keyboard", 3, 799.0, "Pending"},
            {102, "Riya", "Mouse", 5, 499.0, "Shipped"},
            {103, "Karan", "Monitor", 2, 8500.0, "Cancelled"}
        };
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Object[] o : orders) {
                ps.setInt(1, (Integer) o[0]);
                ps.setString(2, (String) o[1]);
                ps.setString(3, (String) o[2]);
                ps.setInt(4, (Integer) o[3]);
                ps.setDouble(5, (Double) o[4]);
                ps.setString(6, (String) o[5]);
                try {
                    ps.executeUpdate();
                    System.out.println("Inserted order ID " + o[0]);
                } catch (SQLIntegrityConstraintViolationException ex) {
                    System.out.println("Duplicate order ID " + o[0] + " - record skipped.");
                }
            }
        }
    }

    // Q1(b): Filter by status and quantity, sort unit_price descending.
    private static void displayByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM Orders WHERE status = ? AND quantity > 2 ORDER BY unit_price DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    printOrder(rs);
                }
                if (!found) System.out.println("No matching orders found.");
            }
        }
    }

    // Q1(c): Add supplied quantity to the existing quantity.
    private static void increaseQuantity(int id, int additionalQty) throws SQLException {
        if (additionalQty <= 0) {
            System.out.println("Additional quantity must be greater than zero.");
            return;
        }
        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, additionalQty);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Quantity updated successfully." : "Order does not exist.");
        }
    }

    // Q1(d): Validate status and update only an existing order.
    private static void updateStatus(int id, String newStatus) throws SQLException {
        if (!newStatus.equalsIgnoreCase("Pending")
                && !newStatus.equalsIgnoreCase("Shipped")
                && !newStatus.equalsIgnoreCase("Delivered")) {
            System.out.println("Invalid status. Use Pending, Shipped, or Delivered.");
            return;
        }
        String checkSql = "SELECT * FROM Orders WHERE order_id = ?";
        try (Connection con = getConnection();
             PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, id);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Order does not exist.");
                    return;
                }
            }
            String updateSql = "UPDATE Orders SET status = ? WHERE order_id = ?";
            try (PreparedStatement update = con.prepareStatement(updateSql)) {
                update.setString(1, capitalize(newStatus));
                update.setInt(2, id);
                update.executeUpdate();
            }
            try (PreparedStatement get = con.prepareStatement(checkSql)) {
                get.setInt(1, id);
                try (ResultSet rs = get.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Updated order details:");
                        printOrder(rs);
                    }
                }
            }
        }
    }

    // Q1(e): SUM(quantity * unit_price), then delete only a Cancelled order.
    private static void displayTotalValue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) AS total_value FROM Orders";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) System.out.printf("Total order value: %.2f%n", rs.getDouble("total_value"));
        }
    }

    private static void deleteCancelledOrder(int id) throws SQLException {
        String sql = "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "Cancelled order deleted successfully."
                    : "Order not found or its status is not Cancelled. Cannot delete.");
        }
    }

    private static void printOrder(ResultSet rs) throws SQLException {
        System.out.printf("ID: %d | Customer: %s | Product: %s | Qty: %d | Price: %.2f | Status: %s%n",
                rs.getInt("order_id"), rs.getString("customer_name"),
                rs.getString("product_name"), rs.getInt("quantity"),
                rs.getDouble("unit_price"), rs.getString("status"));
    }

    private static String capitalize(String value) {
        String v = value.toLowerCase();
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }
}

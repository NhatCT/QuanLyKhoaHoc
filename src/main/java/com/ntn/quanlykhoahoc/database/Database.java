package com.ntn.quanlykhoahoc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database utility class for managing connections and queries.
 * @author Thanh Nhat
 */
public class Database {
    private static final String URL = "jdbc:mysql://localhost/quanlykhoahoc";
    private static final String USER = "root";
    private static final String PASSWORD = "Nhat#1908";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found", ex);
        }
    }

    /**
     * Establishes a connection to the database.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);

    }

    /**
     * Retrieves the user's full name by email.
     * @param email The user's email
     * @return The user's full name, or "Student" if not found
     */
    public static String getUserNameByEmail(String email) {
        String query = "SELECT ho_ten FROM nguoidung WHERE email = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ho_ten");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Student";
    }

    // Remove unused method
    // static Connection connect() {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }
}
package com.ntn.quanlykhoahoc.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database utility class for managing connections and queries.
 * @author Thanh Nhat
 */
public class Database {
    private static final String URL = "jdbc:mysql://localhost/quanlykhoahoc1";
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
    String sql = "SELECT ho, ten FROM nguoidung WHERE email = ?";
    try (Connection conn = getConn();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            String ho = rs.getString("ho");
            String ten = rs.getString("ten");
            return (ho != null ? ho : "") + " " + (ten != null ? ten : "");
        }
    } catch (SQLException e) {
        Logger.getLogger(Database.class.getName()).log(Level.SEVERE, "Lỗi khi lấy tên người dùng", e);
    }
    return null;
}
     public static int getUserIdByEmail(String email) {
        String query = "SELECT id FROM nguoidung WHERE email = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Remove unused method
    // static Connection connect() {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }
}
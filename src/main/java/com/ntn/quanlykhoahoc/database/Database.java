/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.database;

import com.ntn.quanlykhoahoc.models.KhoaHoc;
 import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
 import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Thanh Nhat
 */
public class Database {
     static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/quanlykhoahoc", "root", "Nhat#1908");
    }


    static Connection connect() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
     
public static String getUserNameByEmail(String email) {
        String query = "SELECT ho_ten FROM nguoidung WHERE email = ?";
        try (Connection conn = getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("ho_ten");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Student";
    }

}

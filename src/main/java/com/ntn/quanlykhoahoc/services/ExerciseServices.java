/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.BaiTap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public class ExerciseServices {
     public List<BaiTap> getBaiTapTheoKhoaHocID(int i) throws SQLException {
         List<BaiTap> bt = new ArrayList<>();
         Connection conn = Database.getConn();
        String sql = "SELECT * FROM baitap WHERE khoaHocID = ?";
         PreparedStatement stm = conn.prepareStatement(sql);
        stm.setInt(1, i);
         ResultSet rs = stm.executeQuery();

        while (rs.next()) {
            BaiTap c = new BaiTap(
                    rs.getInt("id"),
                    rs.getInt("khoaHocID"),
                    rs.getString("tenBaiTap"),
                    rs.getDate("deadline")
                  
            );
            bt.add(c);
        }

        // Đóng kết nối để tránh rò rỉ bộ nhớ
        rs.close();
        stm.close();
        conn.close();

        return bt;
    }
    
    
}

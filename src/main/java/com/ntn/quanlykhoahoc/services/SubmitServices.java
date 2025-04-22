/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
public class SubmitServices {
    public int submitSQL(int hocHocVienID, int baiTapID, int diem) throws SQLException {
       Connection conn = Database.getConn();
        String sql = "INSERT INTO hocvien_baitap (hocVienID, baiTapID, diem, ngayNop) VALUES (?, ?, ?, ?)";
        PreparedStatement stm = conn.prepareStatement(sql);
        stm.setInt(1, hocHocVienID); 
        stm.setInt(2, baiTapID); 
        stm.setDouble(3, diem); 
        stm.setTimestamp(4, new Timestamp(System.currentTimeMillis())); 
        int soDong = stm.executeUpdate();
        return soDong;

    }
}

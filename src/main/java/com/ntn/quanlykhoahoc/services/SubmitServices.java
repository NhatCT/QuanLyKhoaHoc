/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.HocVien_BaiTap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
    
    
    //test
    public List<HocVien_BaiTap> getSubmits() throws SQLException {
        List<HocVien_BaiTap> kh = new ArrayList<>();
        Connection conn = Database.getConn();
        String sql = "SELECT * FROM hocvien_baitap";
        PreparedStatement stm = conn.prepareStatement(sql);

        ResultSet rs = stm.executeQuery();

        while (rs.next()) {
            HocVien_BaiTap c = new HocVien_BaiTap(
                    rs.getInt("id"),
                    rs.getInt("hocVienID"),
                    rs.getInt("baiTapID"),
                    rs.getInt("diem"),
                    rs.getTimestamp("ngayNop")
            );
            kh.add(c);
        }
        return kh;
    }

    // lay de kiem tra xem co lam 1 lan khong
    public List<HocVien_BaiTap> getSubmitHocVienIDBaiTapID(int hocVienID, int baiTapID) throws SQLException {
        List<HocVien_BaiTap> kh = new ArrayList<>();
        Connection conn = Database.getConn();
        String sql = "SELECT * FROM hocvien_baitap WHERE hocVienID = ? and baiTapID = ?";
        PreparedStatement stm = conn.prepareStatement(sql);
        stm.setInt(1, hocVienID);
        stm.setInt(2, baiTapID);

        ResultSet rs = stm.executeQuery();

        while (rs.next()) {
            HocVien_BaiTap c = new HocVien_BaiTap(
                    rs.getInt("id"),
                    rs.getInt("hocVienID"),
                    rs.getInt("baiTapID"),
                    rs.getInt("diem"),
                    rs.getTimestamp("ngayNop")
            );
            kh.add(c);
        }
        return kh;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.BaiTap;
import com.ntn.quanlykhoahoc.pojo.HocVien_BaiTap;
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

    public List<BaiTap> getBaiTapsTheoKhoaHocID(int i) throws SQLException {
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
                    rs.getTimestamp("deadline")
            );
            bt.add(c);
        }

        // Đóng kết nối để tránh rò rỉ bộ nhớ
        rs.close();
        stm.close();
        conn.close();

        return bt;
    }
    
    
    public BaiTap getBaiTapTheoKhoaHocID(int i) throws SQLException {
        BaiTap bt = new BaiTap();
        Connection conn = Database.getConn();
        String sql = "SELECT * FROM baitap WHERE id = ?";
        PreparedStatement stm = conn.prepareStatement(sql);
        stm.setInt(1, i);
        ResultSet rs = stm.executeQuery();

        if (rs.next()) {
            BaiTap c = new BaiTap(
                    rs.getInt("id"),
                    rs.getInt("khoaHocID"),
                    rs.getString("tenBaiTap"),
                    rs.getTimestamp("deadline")
            );
            bt =c;
        }

        // Đóng kết nối để tránh rò rỉ bộ nhớ
        rs.close();
        stm.close();
        conn.close();

        return bt;
    }
    
    
    public List<HocVien_BaiTap> countSubbmitted(int khoaHocID, int hocVienID,int baiTapID) throws SQLException {
        List<HocVien_BaiTap> bt = new ArrayList<>();
        Connection conn = Database.getConn();
        String sql = "SELECT * FROM hocvien_baitap h_b "
                + "JOIN baitap bt ON bt.id = h_b.baiTapID "
                + "WHERE bt.khoaHocID = ? and h_b.hocVienID = ? and h_b.baiTapID = ?";
        PreparedStatement stm = conn.prepareStatement(sql);
        stm.setInt(1, khoaHocID);
        stm.setInt(2, hocVienID);
        stm.setInt(3, baiTapID);
        ResultSet rs = stm.executeQuery();

        while (rs.next()) {
            HocVien_BaiTap c = new HocVien_BaiTap(
                    rs.getInt("id"),
                    rs.getInt("hocVienID"),
                    rs.getInt("baiTapID"),
                    rs.getInt("diem"),
                    rs.getTimestamp("deadline")
            );
            bt.add(c);
        }

        // Đóng kết nối để tránh rò rỉ bộ nhớ
        rs.close();
        stm.close();
        conn.close();

        return bt;
    }
    //test

    public BaiTap getBaiTapByID(int i) throws SQLException {
        List<BaiTap> bt = new ArrayList<>();
        Connection conn = Database.getConn();
        String sql = "SELECT * FROM baitap WHERE id = ?";
        PreparedStatement stm = conn.prepareStatement(sql);
        stm.setInt(1, i);
        ResultSet rs = stm.executeQuery();

        if (rs.next()) {
            BaiTap c = new BaiTap(
                    rs.getInt("id"),
                    rs.getInt("khoaHocID"),
                    rs.getString("tenBaiTap"),
                    rs.getTimestamp("deadline")
            );
            return c;
        } else {
            return null;
        }

    }

}

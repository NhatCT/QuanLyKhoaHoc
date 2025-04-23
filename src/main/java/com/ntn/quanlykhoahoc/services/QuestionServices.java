/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.services;


import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.CauHoi;
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
public class QuestionServices {
        public List<CauHoi> getCauHoiTheoBaiTapID(int id) throws SQLException {
        List<CauHoi> ch = new ArrayList<>();
        Connection conn =Database.getConn();
        String s = " SELECT * FROM cauhoi WHERE baiTapID = ? ";

        PreparedStatement stm = conn.prepareStatement(s);
        stm.setInt(1, id);
        ResultSet rs = stm.executeQuery();
        while (rs.next()) {
            CauHoi c = new CauHoi(
                    rs.getInt("id"),
                    rs.getString("noiDung"),
                    rs.getInt("baiTapID")
            );
            ch.add(c);
        }
        return ch;
    }
}

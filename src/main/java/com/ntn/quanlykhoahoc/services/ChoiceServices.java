/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.quanlykhoahoc.services;


import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.DapAn;
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
public class ChoiceServices {
       public List<DapAn> getDapAnTheoCauHoiID(int id) throws SQLException {
        List<DapAn> da = new ArrayList<>();
        Connection conn = Database.getConn();
        String s = " SELECT * FROM dapan WHERE cauHoiID = ? ";

        PreparedStatement stm = conn.prepareStatement(s);
        stm.setInt(1, id);
        ResultSet rs = stm.executeQuery();
        while (rs.next()) {
            DapAn c = new DapAn(
                    rs.getInt("id"),
                    rs.getString("noiDung"),
                    rs.getBoolean("dapAnDung"),
                    rs.getInt("cauHoiID")
            );
            da.add(c);
        }
        return da;
    }
}

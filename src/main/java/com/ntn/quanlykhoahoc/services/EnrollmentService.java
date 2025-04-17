package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EnrollmentService {
    private static final Logger LOGGER = Logger.getLogger(EnrollmentService.class.getName());

    public List<KhoaHocHocVien> getPendingKhoaHocHocVien() throws SQLException {
        List<KhoaHocHocVien> enrollments = new ArrayList<>();
        String sql = "SELECT id, hoc_vien_id, khoa_hoc_id, ngay_dang_ky, trang_thai " +
                     "FROM khoahochocvien WHERE trang_thai = 'PENDING'";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                enrollments.add(new KhoaHocHocVien(
                        rs.getInt("id"),
                        rs.getInt("hoc_vien_id"),
                        rs.getInt("khoa_hoc_id"),
                        rs.getString("ngay_dang_ky"),
                        rs.getString("trang_thai")
                ));
            }
        }
        return enrollments;
    }

    public void updateKhoaHocHocVienStatus(int enrollmentId, String status) throws SQLException {
        String sql = "UPDATE khoahochocvien SET trang_thai = ? WHERE id = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, enrollmentId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Không tìm thấy bản ghi đăng ký với ID: " + enrollmentId);
            }
        }
    }
}
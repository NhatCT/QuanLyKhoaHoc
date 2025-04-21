package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.LichHoc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimetableService {

    private static final Logger LOGGER = Logger.getLogger(TimetableService.class.getName());

    public List<LichHoc> getTimetableForStudent(String email) throws SQLException {
        List<LichHoc> timetable = new ArrayList<>();
        String sql = """
        SELECT DISTINCT lh.id, lh.khoaHocId, lh.ngay_hoc, lh.gio_bat_dau, lh.gio_ket_thuc, 
               lh.giangVienId, lh.lien_ket, kh.ten_khoa_hoc, 
               CONCAT(nd.ho, ' ', nd.ten) AS giang_vien
        FROM lich_hoc lh
        JOIN khoahoc kh ON lh.khoaHocId = kh.id
        JOIN khoahoc_hocvien khhv ON kh.id = khhv.khoaHocID
        JOIN hocvien hv ON khhv.hocVienID = hv.id
        JOIN nguoidung nd_hv ON hv.nguoiDungID = nd_hv.id
        JOIN giangvien gv ON lh.giangVienId = gv.id
        JOIN nguoidung nd ON gv.id = nd.id
        WHERE nd_hv.email = ? AND kh.active = 1 AND khhv.trang_thai = 'APPROVED'
        ORDER BY lh.ngay_hoc, lh.gio_bat_dau
    """;
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LichHoc lichHoc = new LichHoc(
                            rs.getInt("id"),
                            rs.getInt("khoaHocId"),
                            rs.getString("ten_khoa_hoc"),
                            rs.getDate("ngay_hoc").toLocalDate(),
                            rs.getTime("gio_bat_dau").toLocalTime(),
                            rs.getTime("gio_ket_thuc").toLocalTime(),
                            rs.getInt("giangVienId"),
                            rs.getString("giang_vien"),
                            rs.getString("lien_ket")
                    );
                    timetable.add(lichHoc);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy thời khóa biểu cho học viên: " + email, e);
            throw new SQLException("Không thể lấy thời khóa biểu: " + e.getMessage(), e);
        }
        LOGGER.info("Đã lấy " + timetable.size() + " lịch học cho học viên: " + email);
        return timetable;
    }

    public List<LichHoc> getTimetableForStudentByDateRange(String email, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<LichHoc> timetable = new ArrayList<>();
        String sql = """
        SELECT DISTINCT lh.id, lh.khoaHocId, lh.ngay_hoc, lh.gio_bat_dau, lh.gio_ket_thuc, 
               lh.giangVienId, lh.lien_ket, kh.ten_khoa_hoc, 
               CONCAT(nd.ho, ' ', nd.ten) AS giang_vien
        FROM lich_hoc lh
        JOIN khoahoc kh ON lh.khoaHocId = kh.id
        JOIN khoahoc_hocvien khhv ON kh.id = khhv.khoaHocID
        JOIN hocvien hv ON khhv.hocVienID = hv.id
        JOIN nguoidung nd_hv ON hv.nguoiDungID = nd_hv.id
        JOIN giangvien gv ON lh.giangVienId = gv.id
        JOIN nguoidung nd ON gv.id = nd.id
        WHERE nd_hv.email = ? 
              AND kh.active = 1 
              AND khhv.trang_thai = 'APPROVED'
              AND lh.ngay_hoc BETWEEN ? AND ?
        ORDER BY lh.ngay_hoc, lh.gio_bat_dau
    """;
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setDate(2, java.sql.Date.valueOf(startDate));
            stmt.setDate(3, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LichHoc lichHoc = new LichHoc(
                            rs.getInt("id"),
                            rs.getInt("khoaHocId"),
                            rs.getString("ten_khoa_hoc"),
                            rs.getDate("ngay_hoc").toLocalDate(),
                            rs.getTime("gio_bat_dau").toLocalTime(),
                            rs.getTime("gio_ket_thuc").toLocalTime(),
                            rs.getInt("giangVienId"),
                            rs.getString("giang_vien"),
                            rs.getString("lien_ket")
                    );
                    timetable.add(lichHoc);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy thời khóa biểu theo khoảng thời gian cho học viên: " + email, e);
            throw new SQLException("Không thể lấy thời khóa biểu: " + e.getMessage(), e);
        }
        LOGGER.info("Đã lấy " + timetable.size() + " lịch học trong khoảng " + startDate + " đến " + endDate + " cho học viên: " + email);
        return timetable;
    }

    public List<LichHoc> getScheduleForCourse(int courseId) throws SQLException {
        List<LichHoc> schedule = new ArrayList<>();
        String sql = """
            SELECT lh.id, lh.khoaHocId, lh.ngay_hoc, lh.gio_bat_dau, lh.gio_ket_thuc, 
                   lh.giangVienId, lh.lien_ket, kh.ten_khoa_hoc, 
                   CONCAT(nd.ho, ' ', nd.ten) AS giang_vien
            FROM lich_hoc lh
            JOIN khoahoc kh ON lh.khoaHocId = kh.id
            JOIN giangvien gv ON lh.giangVienId = gv.id
            JOIN nguoidung nd ON gv.id = nd.id
            WHERE lh.khoaHocId = ?
            ORDER BY lh.ngay_hoc, lh.gio_bat_dau
        """;

        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LichHoc lichHoc = new LichHoc(
                            rs.getInt("id"),
                            rs.getInt("khoaHocId"),
                            rs.getString("ten_khoa_hoc"),
                            rs.getDate("ngay_hoc").toLocalDate(),
                            rs.getTime("gio_bat_dau").toLocalTime(),
                            rs.getTime("gio_ket_thuc").toLocalTime(),
                            rs.getInt("giangVienId"),
                            rs.getString("giang_vien"),
                            rs.getString("lien_ket")
                    );
                    schedule.add(lichHoc);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy lịch học cho khóa học ID: " + courseId, e);
            throw new SQLException("Không thể lấy lịch học: " + e.getMessage(), e);
        }
        LOGGER.info("Đã lấy " + schedule.size() + " lịch học cho khóa học ID: " + courseId);
        return schedule;
    }
}

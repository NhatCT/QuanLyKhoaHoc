package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    /**
     * Lấy danh sách tất cả các khóa học đang hoạt động (active = true) để hiển thị cho sinh viên.
     */
    public List<KhoaHoc> getAllActiveCourses() throws SQLException {
        List<KhoaHoc> khoaHocList = new ArrayList<>();
        String query = "SELECT k.id, k.ten_khoa_hoc, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, " +
                      "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien " +
                      "FROM khoahoc k " +
                      "LEFT JOIN nguoidung n ON k.giangVienID = n.id " +
                      "WHERE k.active = TRUE AND (n.loai_nguoi_dung_id = 2 OR n.loai_nguoi_dung_id IS NULL)";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                khoaHocList.add(new KhoaHoc(
                    rs.getInt("id"),
                    rs.getString("ten_khoa_hoc"),
                    rs.getString("mo_ta"),
                    rs.getDouble("gia"),
                    rs.getString("hinh_anh"),
                    rs.getString("ten_giang_vien") != null ? rs.getString("ten_giang_vien") : "Chưa có giảng viên",
                    rs.getBoolean("active"),
                    rs.getDate("ngay_bat_dau") != null ? rs.getDate("ngay_bat_dau").toLocalDate() : null,
                    rs.getDate("ngay_ket_thuc") != null ? rs.getDate("ngay_ket_thuc").toLocalDate() : null
                ));
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy danh sách khóa học đang hoạt động: " + e.getMessage(), e);
        }
        return khoaHocList;
    }

    /**
     * Lấy danh sách tất cả các khóa học (bao gồm cả khóa học không hoạt động) để hiển thị trong giao diện quản trị.
     */
    public List<KhoaHoc> getAllCourses() throws SQLException {
        List<KhoaHoc> khoaHocList = new ArrayList<>();
        String query = "SELECT k.id, k.ten_khoa_hoc, k.giangVienID, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, " +
                      "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien " +
                      "FROM khoahoc k " +
                      "LEFT JOIN nguoidung n ON k.giangVienID = n.id";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                KhoaHoc khoaHoc = new KhoaHoc();
                khoaHoc.setId(rs.getInt("id"));
                khoaHoc.setTenKhoaHoc(rs.getString("ten_khoa_hoc"));
                khoaHoc.setGiangVienId(rs.getInt("giangVienID"));
                khoaHoc.setMoTa(rs.getString("mo_ta"));
                khoaHoc.setGia(rs.getDouble("gia"));
                khoaHoc.setHinhAnh(rs.getString("hinh_anh"));
                khoaHoc.setTenGiangVien(rs.getString("ten_giang_vien") != null ? rs.getString("ten_giang_vien") : "Chưa có giảng viên");
                khoaHoc.setActive(rs.getBoolean("active"));
                khoaHoc.setNgayBatDau(rs.getDate("ngay_bat_dau") != null ? rs.getDate("ngay_bat_dau").toLocalDate() : null);
                khoaHoc.setNgayKetThuc(rs.getDate("ngay_ket_thuc") != null ? rs.getDate("ngay_ket_thuc").toLocalDate() : null);
                khoaHocList.add(khoaHoc);
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy danh sách tất cả khóa học: " + e.getMessage(), e);
        }
        return khoaHocList;
    }

    /**
     * Lấy thông tin chi tiết của một khóa học theo ID.
     */
    public KhoaHoc getCourseById(int id) throws SQLException {
        String query = "SELECT k.id, k.ten_khoa_hoc, k.giangVienID, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, " +
                      "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien " +
                      "FROM khoahoc k " +
                      "LEFT JOIN nguoidung n ON k.giangVienID = n.id " +
                      "WHERE k.id = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhoaHoc khoaHoc = new KhoaHoc();
                    khoaHoc.setId(rs.getInt("id"));
                    khoaHoc.setTenKhoaHoc(rs.getString("ten_khoa_hoc"));
                    khoaHoc.setGiangVienId(rs.getInt("giangVienID"));
                    khoaHoc.setMoTa(rs.getString("mo_ta"));
                    khoaHoc.setGia(rs.getDouble("gia"));
                    khoaHoc.setHinhAnh(rs.getString("hinh_anh"));
                    khoaHoc.setTenGiangVien(rs.getString("ten_giang_vien") != null ? rs.getString("ten_giang_vien") : "Chưa có giảng viên");
                    khoaHoc.setActive(rs.getBoolean("active"));
                    khoaHoc.setNgayBatDau(rs.getDate("ngay_bat_dau") != null ? rs.getDate("ngay_bat_dau").toLocalDate() : null);
                    khoaHoc.setNgayKetThuc(rs.getDate("ngay_ket_thuc") != null ? rs.getDate("ngay_ket_thuc").toLocalDate() : null);
                    return khoaHoc;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy thông tin khóa học ID " + id + ": " + e.getMessage(), e);
        }
        return null; // Trả về null nếu không tìm thấy
    }

    /**
     * Thêm một khóa học mới với hình ảnh.
     */
    public boolean addCourseWithImage(String tenKhoaHoc, int giangVienId, String moTa, LocalDate ngayBatDau, LocalDate ngayKetThuc, double hocPhi, String hinhAnh, boolean active) throws SQLException {
        // Kiểm tra dữ liệu đầu vào
        if (tenKhoaHoc == null || tenKhoaHoc.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khóa học không được để trống");
        }
        if (moTa == null || moTa.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả không được để trống");
        }
        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (hocPhi < 0) {
            throw new IllegalArgumentException("Học phí không được âm");
        }
        if (giangVienId <= 0 || !isGiangVienExist(giangVienId)) {
            throw new IllegalArgumentException("Giảng viên không tồn tại hoặc không hợp lệ");
        }

        String sql = "INSERT INTO khoahoc (ten_khoa_hoc, giangVienID, mo_ta, ngay_bat_dau, ngay_ket_thuc, gia, hinh_anh, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenKhoaHoc);
            stmt.setInt(2, giangVienId);
            stmt.setString(3, moTa);
            stmt.setDate(4, java.sql.Date.valueOf(ngayBatDau));
            stmt.setDate(5, java.sql.Date.valueOf(ngayKetThuc));
            stmt.setDouble(6, hocPhi);
            stmt.setString(7, hinhAnh != null && !hinhAnh.trim().isEmpty() ? hinhAnh : "default_course.jpg");
            stmt.setBoolean(8, active);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi thêm khóa học: " + e.getMessage(), e);
        }
    }

    /**
     * Thêm một khóa học mới không có hình ảnh (sử dụng ảnh mặc định).
     */
    public boolean addCourse(String tenKhoaHoc, int giangVienId, String moTa, LocalDate ngayBatDau, LocalDate ngayKetThuc, double hocPhi) throws SQLException {
        return addCourseWithImage(tenKhoaHoc, giangVienId, moTa, ngayBatDau, ngayKetThuc, hocPhi, null, true);
    }

    /**
     * Cập nhật thông tin một khóa học.
     */
    public boolean updateCourse(int id, String tenKhoaHoc, int giangVienId, String moTa, LocalDate ngayBatDau, LocalDate ngayKetThuc, double gia, String hinhAnh, boolean active) throws SQLException {
        // Kiểm tra dữ liệu đầu vào
        if (id <= 0) {
            throw new IllegalArgumentException("ID khóa học không hợp lệ");
        }
        if (tenKhoaHoc == null || tenKhoaHoc.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khóa học không được để trống");
        }
        if (moTa == null || moTa.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả không được để trống");
        }
        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }
        if (gia < 0) {
            throw new IllegalArgumentException("Học phí không được âm");
        }
        if (giangVienId <= 0 || !isGiangVienExist(giangVienId)) {
            throw new IllegalArgumentException("Giảng viên không tồn tại hoặc không hợp lệ");
        }

        String sql = "UPDATE khoahoc SET ten_khoa_hoc = ?, giangVienID = ?, mo_ta = ?, ngay_bat_dau = ?, ngay_ket_thuc = ?, gia = ?, hinh_anh = ?, active = ? WHERE id = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenKhoaHoc);
            stmt.setInt(2, giangVienId);
            stmt.setString(3, moTa);
            stmt.setDate(4, java.sql.Date.valueOf(ngayBatDau));
            stmt.setDate(5, java.sql.Date.valueOf(ngayKetThuc));
            stmt.setDouble(6, gia);
            stmt.setString(7, hinhAnh != null && !hinhAnh.trim().isEmpty() ? hinhAnh : "default_course.jpg");
            stmt.setBoolean(8, active);
            stmt.setInt(9, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi cập nhật khóa học ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Lấy số thứ tự ảnh tiếp theo dựa trên số lượng khóa học hiện có.
     */
    public int getNextImageNumber() throws SQLException {
        String query = "SELECT COUNT(*) FROM khoahoc";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1; // Số thứ tự tiếp theo là số lượng hiện tại + 1
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy số thứ tự ảnh: " + e.getMessage(), e);
        }
        return 1; // Mặc định là 1 nếu không có khóa học nào
    }

    /**
     * Kiểm tra xem giảng viên có tồn tại trong bảng nguoidung hay không.
     */
    private boolean isGiangVienExist(int giangVienId) throws SQLException {
        String query = "SELECT COUNT(*) FROM nguoidung WHERE id = ? AND loai_nguoi_dung_id = 2";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, giangVienId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi kiểm tra giảng viên ID " + giangVienId + ": " + e.getMessage(), e);
        }
        return false;
    }
}
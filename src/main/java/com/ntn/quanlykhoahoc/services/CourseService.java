package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CourseService {

    private static final Logger LOGGER = Logger.getLogger(CourseService.class.getName());

    public List<KhoaHoc> getAllActiveCourses() throws SQLException {
        List<KhoaHoc> khoaHocList = new ArrayList<>();
        String query = "SELECT k.id, k.ten_khoa_hoc, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, k.so_luong_hoc_vien_toi_da, "
                + "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien "
                + "FROM khoahoc k "
                + "LEFT JOIN nguoidung n ON k.giangVienID = n.id "
                + "WHERE k.active = TRUE AND (n.loai_nguoi_dung_id = 2 OR n.loai_nguoi_dung_id IS NULL)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                khoaHocList.add(new KhoaHoc(
                        rs.getInt("id"),
                        rs.getString("ten_khoa_hoc"),
                        rs.getString("mo_ta"),
                        rs.getDouble("gia"),
                        rs.getInt("so_luong_hoc_vien_toi_da"),
                        rs.getString("ten_giang_vien") != null ? rs.getString("ten_giang_vien") : "Chưa có giảng viên",
                        rs.getString("hinh_anh"),
                        rs.getBoolean("active"),
                        rs.getDate("ngay_bat_dau") != null ? rs.getDate("ngay_bat_dau").toLocalDate() : null,
                        rs.getDate("ngay_ket_thuc") != null ? rs.getDate("ngay_ket_thuc").toLocalDate() : null
                ));
            }
            System.out.println("Retrieved " + khoaHocList.size() + " active courses from database");
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy danh sách khóa học đang hoạt động: " + e.getMessage(), e);
        }
        return khoaHocList;
    }

    public List<KhoaHoc> getAllCourses() throws SQLException {
        List<KhoaHoc> khoaHocList = new ArrayList<>();
        String query = "SELECT k.id, k.ten_khoa_hoc, k.giangVienID, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, k.so_luong_hoc_vien_toi_da, "
                + "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien "
                + "FROM khoahoc k "
                + "LEFT JOIN nguoidung n ON k.giangVienID = n.id";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                KhoaHoc khoaHoc = new KhoaHoc();
                khoaHoc.setId(rs.getInt("id"));
                khoaHoc.setTenKhoaHoc(rs.getString("ten_khoa_hoc"));
                khoaHoc.setGiangVienId(rs.getInt("giangVienID"));
                khoaHoc.setMoTa(rs.getString("mo_ta"));
                khoaHoc.setGia(rs.getDouble("gia"));
                khoaHoc.setSoLuongHocVienToiDa(rs.getInt("so_luong_hoc_vien_toi_da"));
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

    public KhoaHoc getCourseById(int id) throws SQLException {
        String query = "SELECT k.id, k.ten_khoa_hoc, k.giangVienID, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, k.so_luong_hoc_vien_toi_da, "
                + "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien "
                + "FROM khoahoc k "
                + "LEFT JOIN nguoidung n ON k.giangVienID = n.id "
                + "WHERE k.id = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhoaHoc khoaHoc = new KhoaHoc();
                    khoaHoc.setId(rs.getInt("id"));
                    khoaHoc.setTenKhoaHoc(rs.getString("ten_khoa_hoc"));
                    khoaHoc.setGiangVienId(rs.getInt("giangVienID"));
                    khoaHoc.setMoTa(rs.getString("mo_ta"));
                    khoaHoc.setGia(rs.getDouble("gia"));
                    khoaHoc.setSoLuongHocVienToiDa(rs.getInt("so_luong_hoc_vien_toi_da"));
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
        return null;
    }

    public boolean addCourseWithImage(String tenKhoaHoc, int giangVienId, String moTa, LocalDate ngayBatDau, LocalDate ngayKetThuc, double hocPhi, String hinhAnh, boolean active) throws SQLException {
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

        String sql = "INSERT INTO khoahoc (ten_khoa_hoc, giangVienID, mo_ta, ngay_bat_dau, ngay_ket_thuc, gia, hinh_anh, active, so_luong_hoc_vien_toi_da) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenKhoaHoc);
            stmt.setInt(2, giangVienId);
            stmt.setString(3, moTa);
            stmt.setDate(4, java.sql.Date.valueOf(ngayBatDau));
            stmt.setDate(5, java.sql.Date.valueOf(ngayKetThuc));
            stmt.setDouble(6, hocPhi);
            stmt.setString(7, hinhAnh != null && !hinhAnh.trim().isEmpty() ? hinhAnh : "default_course.jpg");
            stmt.setBoolean(8, active);
            stmt.setInt(9, 40);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi thêm khóa học: " + e.getMessage(), e);
        }
    }

    public boolean addCourse(String tenKhoaHoc, int giangVienId, String moTa, LocalDate ngayBatDau, LocalDate ngayKetThuc, double hocPhi) throws SQLException {
        return addCourseWithImage(tenKhoaHoc, giangVienId, moTa, ngayBatDau, ngayKetThuc, hocPhi, null, true);
    }

    public boolean updateCourse(int id, String tenKhoaHoc, int giangVienId, String moTa, LocalDate ngayBatDau, LocalDate ngayKetThuc, double gia, String hinhAnh, boolean active) throws SQLException {
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

        String sql = "UPDATE khoahoc SET ten_khoa_hoc = ?, giangVienID = ?, mo_ta = ?, ngay_bat_dau = ?, ngay_ket_thuc = ?, gia = ?, hinh_anh = ?, active = ?, so_luong_hoc_vien_toi_da = ? WHERE id = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenKhoaHoc);
            stmt.setInt(2, giangVienId);
            stmt.setString(3, moTa);
            stmt.setDate(4, java.sql.Date.valueOf(ngayBatDau));
            stmt.setDate(5, java.sql.Date.valueOf(ngayKetThuc));
            stmt.setDouble(6, gia);
            stmt.setString(7, hinhAnh != null && !hinhAnh.trim().isEmpty() ? hinhAnh : "default_course.jpg");
            stmt.setBoolean(8, active);
            stmt.setInt(9, 40);
            stmt.setInt(10, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi cập nhật khóa học ID " + id + ": " + e.getMessage(), e);
        }
    }

    public int getNextImageNumber() throws SQLException {
        String query = "SELECT COUNT(*) FROM khoahoc";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi lấy số thứ tự ảnh: " + e.getMessage(), e);
        }
        return 1;
    }

    private boolean isGiangVienExist(int giangVienId) throws SQLException {
        String query = "SELECT COUNT(*) FROM nguoidung WHERE id = ? AND loai_nguoi_dung_id = 2";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
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

    public List<KhoaHoc> searchCourses(String keyword) throws SQLException {
        List<KhoaHoc> khoaHocList = new ArrayList<>();
        String query = "SELECT k.id, k.ten_khoa_hoc, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, k.so_luong_hoc_vien_toi_da, "
                + "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien "
                + "FROM khoahoc k "
                + "LEFT JOIN nguoidung n ON k.giangVienID = n.id "
                + "WHERE k.active = TRUE AND (n.loai_nguoi_dung_id = 2 OR n.loai_nguoi_dung_id IS NULL) AND k.ten_khoa_hoc LIKE ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    khoaHocList.add(new KhoaHoc(
                            rs.getInt("id"),
                            rs.getString("ten_khoa_hoc"),
                            rs.getString("mo_ta"),
                            rs.getDouble("gia"),
                            rs.getInt("so_luong_hoc_vien_toi_da"),
                            rs.getString("ten_giang_vien") != null ? rs.getString("ten_giang_vien") : "Chưa có giảng viên",
                            rs.getString("hinh_anh"),
                            rs.getBoolean("active"),
                            rs.getDate("ngay_bat_dau") != null ? rs.getDate("ngay_bat_dau").toLocalDate() : null,
                            rs.getDate("ngay_ket_thuc") != null ? rs.getDate("ngay_ket_thuc").toLocalDate() : null
                    ));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Lỗi khi tìm kiếm khóa học: " + e.getMessage(), e);
        }
        return khoaHocList;
    }

    public List<KhoaHoc> getEnrolledCourses(int hocVienId) throws SQLException {
        List<KhoaHoc> khoaHocList = new ArrayList<>();
        String query = "SELECT k.id, k.ten_khoa_hoc, k.mo_ta, k.gia, k.hinh_anh, k.active, k.ngay_bat_dau, k.ngay_ket_thuc, k.so_luong_hoc_vien_toi_da, "
                + "CONCAT(n.ho, ' ', n.ten) AS ten_giang_vien "
                + "FROM khoahoc k "
                + "LEFT JOIN nguoidung n ON k.giangVienID = n.id "
                + "JOIN khoahoc_hocvien kh ON k.id = kh.khoaHocID "
                + "WHERE kh.hocVienID = ? AND kh.trang_thai = 'APPROVED'";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hocVienId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    khoaHocList.add(new KhoaHoc(
                            rs.getInt("id"),
                            rs.getString("ten_khoa_hoc"),
                            rs.getString("mo_ta"),
                            rs.getDouble("gia"),
                            rs.getInt("so_luong_hoc_vien_toi_da"),
                            rs.getString("ten_giang_vien") != null ? rs.getString("ten_giang_vien") : "Chưa có giảng viên",
                            rs.getString("hinh_anh"),
                            rs.getBoolean("active"),
                            rs.getDate("ngay_bat_dau") != null ? rs.getDate("ngay_bat_dau").toLocalDate() : null,
                            rs.getDate("ngay_ket_thuc") != null ? rs.getDate("ngay_ket_thuc").toLocalDate() : null
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách khóa học đã đăng ký", e);
            throw new SQLException("Lỗi khi lấy danh sách khóa học đã đăng ký: " + e.getMessage(), e);
        }
        LOGGER.info("Tải " + khoaHocList.size() + " khóa học APPROVED cho hocVienID=" + hocVienId);
        return khoaHocList;
    }

    public boolean isCourseEnrolled(int hocVienId, int khoaHocId) throws SQLException {
        String query = "SELECT COUNT(*) FROM khoahoc_hocvien WHERE hocVienID = ? AND khoaHocID = ? AND trang_thai = 'APPROVED'";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hocVienId);
            stmt.setInt(2, khoaHocId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi kiểm tra trạng thái đăng ký", e);
            throw new SQLException("Lỗi khi kiểm tra trạng thái đăng ký: " + e.getMessage(), e);
        }
        return false;
    }

    public int getCurrentEnrollmentCount(int khoaHocId) throws SQLException {
        String query = "SELECT COUNT(*) FROM khoahoc_hocvien WHERE khoaHocID = ? AND trang_thai = 'APPROVED'";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, khoaHocId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy số lượng học viên hiện tại", e);
            throw new SQLException("Lỗi khi lấy số lượng học viên hiện tại: " + e.getMessage(), e);
        }
        return 0;
    }

    public void enrollCourse(int hocVienId, int khoaHocId) throws SQLException {
        String query = "INSERT INTO khoahoc_hocvien (hocVienID, khoaHocID, ngay_dang_ky, trang_thai) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hocVienId);
            stmt.setInt(2, khoaHocId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, "PENDING");
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đăng ký khóa học", e);
            throw new SQLException("Lỗi khi đăng ký khóa học: " + e.getMessage(), e);
        }
    }

    public List<KhoaHocHocVien> getKhoaHocHocVien(int hocVienId, int khoaHocId) throws SQLException {
        List<KhoaHocHocVien> enrollments = new ArrayList<>();
        String query = "SELECT id, hocVienID, khoaHocID, ngay_dang_ky, trang_thai FROM khoahoc_hocvien "
                + "WHERE hocVienID = ? AND khoaHocID = ? AND trang_thai = 'APPROVED'";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hocVienId);
            stmt.setInt(2, khoaHocId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    KhoaHocHocVien enrollment = new KhoaHocHocVien(
                            rs.getInt("id"),
                            rs.getInt("hocVienID"),
                            rs.getInt("khoaHocID"),
                            rs.getString("ngay_dang_ky"),
                            rs.getString("trang_thai")
                    );
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy thông tin đăng ký cho hocVienID=" + hocVienId + ", khoaHocID=" + khoaHocId, e);
            throw new SQLException("Lỗi khi lấy thông tin đăng ký khóa học: " + e.getMessage(), e);
        }
        LOGGER.info("Lấy được " + enrollments.size() + " bản ghi đăng ký cho hocVienID=" + hocVienId + ", khoaHocID=" + khoaHocId);
        return enrollments;
    }

    public void updateStatus(int hocVienId, int khoaHocId) throws SQLException {
        String query = "UPDATE khoahoc_hocvien SET trang_thai = 'CANCELLED' WHERE hocVienID = ? AND khoaHocID = ? AND trang_thai = 'APPROVED'";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, hocVienId);
            stmt.setInt(2, khoaHocId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                LOGGER.warning("Không tìm thấy bản ghi đăng ký APPROVED để hủy cho hocVienID=" + hocVienId + ", khoaHocID=" + khoaHocId);
                throw new SQLException("Không tìm thấy đăng ký khóa học được phê duyệt để hủy.");
            }
            LOGGER.info("Cập nhật trạng thái thành CANCELLED cho hocVienID=" + hocVienId + ", khoaHocID=" + khoaHocId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái đăng ký cho hocVienID=" + hocVienId + ", khoaHocID=" + khoaHocId, e);
            throw new SQLException("Lỗi khi cập nhật trạng thái đăng ký: " + e.getMessage(), e);
        }
    }
}

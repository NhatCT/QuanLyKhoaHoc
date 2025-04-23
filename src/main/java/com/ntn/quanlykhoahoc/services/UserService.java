package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String DEFAULT_AVATAR = "/com/ntn/images/avatars/default.jpg";
    private final EmailService emailService = new EmailService();

    // Kiểm tra xem khóa học mới có trùng lịch với các khóa học đã đăng ký
    public boolean hasOverlappingSchedule(int hocVienID, int newKhoaHocID) throws SQLException {
        String sql = """
            SELECT DISTINCT lh1.ngay_hoc, lh1.gio_bat_dau, lh1.gio_ket_thuc
            FROM lich_hoc lh1
            JOIN khoahoc_hocvien khv ON lh1.khoaHocId = khv.khoaHocID
            WHERE khv.hocVienID = ? AND khv.trang_thai IN ('ENROLLED', 'APPROVED')
            AND EXISTS (
                SELECT 1
                FROM lich_hoc lh2
                WHERE lh2.khoaHocId = ?
                AND lh2.ngay_hoc = lh1.ngay_hoc
                AND (
                    (lh2.gio_bat_dau <= lh1.gio_ket_thuc AND lh2.gio_ket_thuc >= lh1.gio_bat_dau)
                )
            )
        """;
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hocVienID);
            stmt.setInt(2, newKhoaHocID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Nếu có bản ghi, nghĩa là có trùng lịch
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra trùng lịch học: " + e.getMessage());
            throw e;
        }
    }

    public int getHocVienIDFromNguoiDung(int nguoiDungID) throws SQLException {
        String sql = "SELECT id FROM hocvien WHERE nguoiDungID = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, nguoiDungID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy hocVienID từ nguoiDungID: " + nguoiDungID + ", " + e.getMessage());
            throw e;
        }
    }

    private boolean isLoaiNguoiDungValid(int loaiNguoiDungId) throws SQLException {
        String sql = "SELECT id FROM loainguoidung WHERE id = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loaiNguoiDungId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra loại người dùng: " + e.getMessage());
            throw e;
        }
    }

    public List<String> getLoaiNguoiDungList() throws SQLException {
        List<String> loaiNguoiDungList = new ArrayList<>();
        String sql = "SELECT id, ten_loai FROM loainguoidung";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String tenLoai = rs.getString("ten_loai");
                    loaiNguoiDungList.add(id + " - " + tenLoai);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách loại người dùng: " + e.getMessage());
            throw e;
        }
        return loaiNguoiDungList;
    }

    public List<String> getGiangVienList() throws SQLException {
        List<String> giangVienList = new ArrayList<>();
        String sql = "SELECT id, ho, ten FROM nguoidung WHERE loai_nguoi_dung_id = 2";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String ho = rs.getString("ho");
                    String ten = rs.getString("ten");
                    giangVienList.add(id + " - " + ho + " " + ten);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách giảng viên: " + e.getMessage());
            throw e;
        }
        return giangVienList;
    }

    public boolean isEmailExists(String email) throws SQLException {
        if (!emailService.isValidEmail(email)) {
            return false;
        }

        String sql = "SELECT email FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra email tồn tại: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateUser(NguoiDung oldUser, NguoiDung updatedUser) throws SQLException {
        if (!isLoaiNguoiDungValid(updatedUser.getLoaiNguoiDungId())) {
            throw new SQLException("Loại người dùng không hợp lệ: " + updatedUser.getLoaiNguoiDungId());
        }

        if (!emailService.isValidEmail(updatedUser.getEmail())) {
            return false;
        }

        String query = "UPDATE nguoidung SET ho = ?, ten = ?, email = ?, mat_khau = ?, active = ?, loai_nguoi_dung_id = ?, avatar = ? WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, updatedUser.getHo());
            stmt.setString(2, updatedUser.getTen());
            stmt.setString(3, updatedUser.getEmail());
            stmt.setString(4, updatedUser.getMatKhau());
            stmt.setBoolean(5, updatedUser.isActive());
            stmt.setInt(6, updatedUser.getLoaiNguoiDungId());
            stmt.setString(7, updatedUser.getAvatar() != null && !updatedUser.getAvatar().isEmpty() ? updatedUser.getAvatar() : DEFAULT_AVATAR);
            stmt.setString(8, oldUser.getEmail());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Cập nhật người dùng: " + updatedUser.getEmail() + ", Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật người dùng: " + e.getMessage());
            throw e;
        }
    }

    public boolean registerUser(String ho, String ten, String email, String hashedPassword, int loaiNguoiDungId, String avatar, boolean active) throws SQLException {
        if (ho == null || ho.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ không được để trống");
        }
        if (ten == null || ten.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        if (!emailService.isValidEmail(email)) {
            return false;
        }

        if (!isLoaiNguoiDungValid(loaiNguoiDungId)) {
            throw new SQLException("Loại người dùng không hợp lệ: " + loaiNguoiDungId);
        }

        if (isEmailExists(email)) {
            throw new SQLException("Email đã tồn tại: " + email);
        }

        String sql = "INSERT INTO nguoidung (ho, ten, email, mat_khau, loai_nguoi_dung_id, active, avatar) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ho);
            stmt.setString(2, ten);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setInt(5, loaiNguoiDungId);
            stmt.setBoolean(6, active);
            stmt.setString(7, avatar != null && !avatar.trim().isEmpty() ? avatar : DEFAULT_AVATAR);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Đăng ký người dùng: " + email + ", Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi đăng ký người dùng: " + e.getMessage());
            throw e;
        }
    }

    public boolean updatePassword(String email, String hashedPassword) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        if (!emailService.isValidEmail(email)) {
            return false;
        }

        String query = "UPDATE nguoidung SET mat_khau = ? WHERE email = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Cập nhật mật khẩu cho: " + email + ", Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật mật khẩu: " + e.getMessage());
            throw e;
        }
    }

    public List<NguoiDung> getAllUsers() throws SQLException {
        List<NguoiDung> userList = new ArrayList<>();
        String sql = "SELECT id, ho, ten, email, active, loai_nguoi_dung_id, avatar FROM nguoidung";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NguoiDung user = new NguoiDung();
                    user.setId(rs.getInt("id"));
                    user.setHo(rs.getString("ho"));
                    user.setTen(rs.getString("ten"));
                    user.setEmail(rs.getString("email"));
                    user.setActive(rs.getBoolean("active"));
                    user.setLoaiNguoiDungId(rs.getInt("loai_nguoi_dung_id"));
                    user.setAvatar(rs.getString("avatar"));
                    userList.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
            throw e;
        }
        return userList;
    }

    public boolean deleteUser(String email) throws SQLException {
        String sql = "DELETE FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Xóa người dùng: " + email + ", Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa người dùng: " + e.getMessage());
            throw e;
        }
    }

    public boolean toggleUserStatus(String email) throws SQLException {
        String sql = "UPDATE nguoidung SET active = NOT active WHERE email = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Thay đổi trạng thái người dùng: " + email + ", Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thay đổi trạng thái người dùng: " + e.getMessage());
            throw e;
        }
    }

}
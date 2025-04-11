package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    // Regex để kiểm tra định dạng email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );
    private static final String DEFAULT_AVATAR = "/com/ntn/images/avatars/default.jpg";

    // Kiểm tra loại người dùng hợp lệ
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

    // Lấy danh sách loại người dùng
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

    // Lấy danh sách giảng viên
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

    // Kiểm tra email đã tồn tại
    public boolean isEmailExists(String email) throws SQLException {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ: " + email);
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

    // Cập nhật thông tin người dùng
    public boolean updateUser(NguoiDung oldUser, NguoiDung updatedUser) throws SQLException {
        if (!isLoaiNguoiDungValid(updatedUser.getLoaiNguoiDungId())) {
            throw new SQLException("Loại người dùng không hợp lệ: " + updatedUser.getLoaiNguoiDungId());
        }

        if (!EMAIL_PATTERN.matcher(updatedUser.getEmail()).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ: " + updatedUser.getEmail());
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

    // Đăng ký người dùng mới
    public boolean registerUser(String ho, String ten, String email, String hashedPassword, int loaiNguoiDungId, String avatar, boolean active) throws SQLException {
        // Kiểm tra dữ liệu đầu vào
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

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ: " + email);
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

    // Cập nhật mật khẩu
    public boolean updatePassword(String email, String hashedPassword) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ: " + email);
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

    // Lấy tất cả người dùng
    public List<NguoiDung> getAllUsers() throws SQLException {
        List<NguoiDung> userList = new ArrayList<>();
        String sql = "SELECT ho, ten, email, active, loai_nguoi_dung_id, avatar FROM nguoidung";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NguoiDung user = new NguoiDung();
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

    // Xóa người dùng
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

    // Bật/tắt trạng thái người dùng
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
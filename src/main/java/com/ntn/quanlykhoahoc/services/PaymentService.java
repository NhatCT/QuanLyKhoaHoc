package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import com.ntn.quanlykhoahoc.pojo.ThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    public int addPayment(int hocVienID, int khoaHocID, double soTien, LocalDateTime ngayThanhToan, String phuongThuc) throws SQLException {
        String sql = "INSERT INTO lichsu_thanhtoan (hocVienID, khoaHocID, so_tien, ngay_thanh_toan, phuong_thuc) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, hocVienID);
            stmt.setInt(2, khoaHocID);
            stmt.setDouble(3, soTien);
            stmt.setTimestamp(4, Timestamp.valueOf(ngayThanhToan));
            stmt.setString(5, phuongThuc);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    private int addToLichSuThanhToan(Connection conn, Integer hocVienID, Integer khoaHocID, double soTien, LocalDateTime ngayThanhToan, String phuongThuc) throws SQLException {
        String sql = "INSERT INTO lichsu_thanhtoan (hocVienID, khoaHocID, so_tien, ngay_thanh_toan, phuong_thuc) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            if (hocVienID != null) {
                stmt.setInt(1, hocVienID);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            if (khoaHocID != null) {
                stmt.setInt(2, khoaHocID);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setDouble(3, soTien);
            stmt.setTimestamp(4, Timestamp.valueOf(ngayThanhToan));
            stmt.setString(5, phuongThuc);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
        }
    }

    public int addToKhoaHocHocVien(Connection conn, int hocVienID, int khoaHocID, LocalDateTime ngayDangKy) throws SQLException {
        String checkSql = "SELECT id FROM khoahoc_hocvien WHERE hocVienID = ? AND khoaHocID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, hocVienID);
            checkStmt.setInt(2, khoaHocID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    LOGGER.warning("Bản ghi đã tồn tại trong khoahoc_hocvien: hocVienID=" + hocVienID + ", khoaHocID=" + khoaHocID);
                    throw new SQLException("Duplicate entry for hocVienID and khoaHocID");
                }
            }
        }
        String sql = "INSERT INTO khoahoc_hocvien (hocVienID, khoaHocID, ngay_dang_ky, trang_thai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, hocVienID);
            stmt.setInt(2, khoaHocID);
            stmt.setTimestamp(3, Timestamp.valueOf(ngayDangKy));
            stmt.setString(4, "PENDING");
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
        }
    }

    public void notifyAdmin(int hocVienID, int khoaHocID, int thanhToanID) throws SQLException {
        String sqlHocVien = "SELECT n.ho, n.ten FROM hocvien h JOIN nguoidung n ON h.nguoiDungID = n.id WHERE h.id = ?";
        String sqlKhoaHoc = "SELECT ten_khoa_hoc FROM khoahoc WHERE id = ?";
        String tenHocVien = "N/A";
        String tenKhoaHoc = "N/A";

        try (Connection conn = Database.getConn()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlHocVien)) {
                stmt.setInt(1, hocVienID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        tenHocVien = rs.getString("ho") + " " + rs.getString("ten");
                    }
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlKhoaHoc)) {
                stmt.setInt(1, khoaHocID);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        tenKhoaHoc = rs.getString("ten_khoa_hoc");
                    }
                }
            }
            String noiDung = String.format("Học viên %s (ID=%d) đã thanh toán cho khóa học %s (ID=%d, ThanhToanID=%d). Vui lòng xét duyệt.",
                    tenHocVien, hocVienID, tenKhoaHoc, khoaHocID, thanhToanID);
            String sql = "INSERT INTO thongbao (noi_dung, nguoi_nhan_id, ngay_gui, trang_thai) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, noiDung);
                stmt.setInt(2, getAdminId());
                stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(4, "UNREAD");
                int rowsAffected = stmt.executeUpdate();
                LOGGER.info("Đã chèn thông báo cho admin: " + noiDung + ", rowsAffected=" + rowsAffected);
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi gửi thông báo đến quản trị viên: " + e.getMessage());
            throw e;
        }
    }

    private void notifyStudent(int hocVienID, int khoaHocID, String trangThai) throws SQLException {
        String noiDung = String.format("Yêu cầu tham gia khóa học ID=%d của bạn đã được %s.",
                khoaHocID, trangThai.equals("APPROVED") ? "duyệt" : "từ chối");
        String sql = "INSERT INTO thongbao (noi_dung, nguoi_nhan_id, ngay_gui, trang_thai) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, noiDung);
            stmt.setInt(2, hocVienID);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, "UNREAD");
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Đã gửi thông báo đến học viên ID=" + hocVienID + ": " + noiDung + ", rowsAffected=" + rowsAffected);
        }
    }

    private int getAdminId() throws SQLException {
        String sql = "SELECT id FROM nguoidung WHERE loai_nguoi_dung_id = 1 LIMIT 1";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
            LOGGER.severe("Không tìm thấy quản trị viên trong bảng nguoidung với loai_nguoi_dung_id=1");
            throw new SQLException("Không tìm thấy quản trị viên.");
        }
    }

    public boolean updatePayment(int transactionId, Integer hocVienID, Integer khoaHocID, double soTien, LocalDateTime ngayThanhToan, String phuongThuc) throws SQLException {
        if (transactionId <= 0 || soTien <= 0 || ngayThanhToan == null || phuongThuc == null || phuongThuc.trim().isEmpty()) {
            LOGGER.warning("Invalid update details: transactionId=" + transactionId);
            return false;
        }
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE lichsu_thanhtoan SET hocVienID = ?, khoaHocID = ?, so_tien = ?, ngay_thanh_toan = ?, phuong_thuc = ? WHERE id = ?")) {
            if (hocVienID != null) {
                stmt.setInt(1, hocVienID);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            if (khoaHocID != null) {
                stmt.setInt(2, khoaHocID);
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER);
            }
            stmt.setDouble(3, soTien);
            stmt.setTimestamp(4, Timestamp.valueOf(ngayThanhToan));
            stmt.setString(5, phuongThuc);
            stmt.setInt(6, transactionId);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Updated payment: transactionId=" + transactionId + ", rowsAffected=" + rowsAffected);
            return rowsAffected > 0;
        }
    }

    public boolean deletePayment(String thanhToanID) throws SQLException {
        try {
            int id = Integer.parseInt(thanhToanID);
            try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM lichsu_thanhtoan WHERE id = ?")) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                LOGGER.info("Deleted payment: thanhToanID=" + thanhToanID + ", rowsAffected=" + rowsAffected);
                return rowsAffected > 0;
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid thanhToanID format: " + thanhToanID);
            return false;
        }
    }

    public List<ThanhToan> getAllPayments() throws SQLException {
        List<ThanhToan> payments = new ArrayList<>();
        String sql = "SELECT id, ngay_thanh_toan, so_tien, phuong_thuc, hocVienID, khoaHocID FROM lichsu_thanhtoan";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String hocVienID = rs.getString("hocVienID");
                String khoaHocID = rs.getString("khoaHocID");
                String ngayThanhToan = rs.getTimestamp("ngay_thanh_toan") != null ? rs.getTimestamp("ngay_thanh_toan").toString() : "";
                ngayThanhToan = ngayThanhToan.replaceAll("\\.0$", "");
                payments.add(new ThanhToan(
                        String.valueOf(rs.getInt("id")),
                        ngayThanhToan,
                        String.valueOf(rs.getDouble("so_tien")),
                        rs.getString("phuong_thuc") != null ? rs.getString("phuong_thuc") : "",
                        hocVienID != null ? hocVienID : "",
                        khoaHocID != null ? khoaHocID : ""
                ));
            }
            LOGGER.info("Retrieved " + payments.size() + " payment records");
            return payments;
        }
    }

    public ThanhToan getPaymentByTransactionId(int transactionId) throws SQLException {
        String sql = "SELECT id, ngay_thanh_toan, so_tien, phuong_thuc, hocVienID, khoaHocID FROM lichsu_thanhtoan WHERE id = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hocVienID = rs.getString("hocVienID");
                    String khoaHocID = rs.getString("khoaHocID");
                    String ngayThanhToan = rs.getTimestamp("ngay_thanh_toan") != null ? rs.getTimestamp("ngay_thanh_toan").toString() : "";
                    ngayThanhToan = ngayThanhToan.replaceAll("\\.0$", "");
                    return new ThanhToan(
                            String.valueOf(rs.getInt("id")),
                            ngayThanhToan,
                            String.valueOf(rs.getDouble("so_tien")),
                            rs.getString("phuong_thuc") != null ? rs.getString("phuong_thuc") : "",
                            hocVienID != null ? hocVienID : "",
                            khoaHocID != null ? khoaHocID : ""
                    );
                }
            }
            LOGGER.info("No payment found for transactionId=" + transactionId);
            return null;
        }
    }

    public boolean isValidHocVien(int hocVienID) throws SQLException {
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM hocvien WHERE id = ?")) {
            stmt.setInt(1, hocVienID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isValidNguoiDung(int nguoiDungID) throws SQLException {
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM nguoidung WHERE id = ?")) {
            stmt.setInt(1, nguoiDungID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isValidCourse(int khoaHocID) throws SQLException {
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM khoahoc WHERE id = ?")) {
            stmt.setInt(1, khoaHocID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updateKhoaHocHocVienStatus(int id, String trangThai) throws SQLException {
        if (id <= 0 || trangThai == null || !trangThai.matches("PENDING|APPROVED|REJECTED")) {
            LOGGER.warning("Thông tin không hợp lệ: id=" + id + ", trangThai=" + trangThai);
            return false;
        }
        Connection conn = null;
        try {
            conn = Database.getConn();
            conn.setAutoCommit(false);

            // Lấy hocVienID và khoaHocID từ khoahoc_hocvien
            String selectSql = "SELECT hocVienID, khoaHocID FROM khoahoc_hocvien WHERE id = ?";
            int hocVienID = 0;
            int khoaHocID = 0;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, id);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        hocVienID = rs.getInt("hocVienID");
                        khoaHocID = rs.getInt("khoaHocID");
                    } else {
                        LOGGER.warning("Không tìm thấy bản ghi khoahoc_hocvien với id=" + id);
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Cập nhật trạng thái khoahoc_hocvien
            String updateSql = "UPDATE khoahoc_hocvien SET trang_thai = ? WHERE id = ?";
            int rowsAffected = 0;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, trangThai);
                updateStmt.setInt(2, id);
                rowsAffected = updateStmt.executeUpdate();
            }

            if (rowsAffected == 0) {
                LOGGER.warning("Không cập nhật được bản ghi khoahoc_hocvien với id=" + id);
                conn.rollback();
                return false;
            }

            // Nếu trạng thái là REJECTED, xóa bản ghi lichsu_thanhtoan tương ứng
            if ("REJECTED".equals(trangThai)) {
                String deletePaymentSql = "DELETE FROM lichsu_thanhtoan WHERE hocVienID = ? AND khoaHocID = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deletePaymentSql)) {
                    deleteStmt.setInt(1, hocVienID);
                    deleteStmt.setInt(2, khoaHocID);
                    int paymentRowsAffected = deleteStmt.executeUpdate();
                    if (paymentRowsAffected > 0) {
                        LOGGER.info("Đã xóa " + paymentRowsAffected + " bản ghi lichsu_thanhtoan cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHocID);
                    } else {
                        LOGGER.info("Không tìm thấy bản ghi lichsu_thanhtoan để xóa cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHocID);
                    }
                }
            }

            // Gửi thông báo cho học viên
            notifyStudent(hocVienID, khoaHocID, trangThai);

            conn.commit();
            LOGGER.info("Cập nhật trạng thái khoahoc_hocvien: id=" + id + ", trangThai=" + trangThai + ", rowsAffected=" + rowsAffected);
            return true;

        } catch (SQLException e) {
            LOGGER.severe("Lỗi cập nhật trạng thái khoahoc_hocvien hoặc xóa lichsu_thanhtoan: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Lỗi khi rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe("Lỗi khi đóng kết nối: " + e.getMessage());
                }
            }
        }
    }

    public List<KhoaHocHocVien> getPendingKhoaHocHocVien() throws SQLException {
        List<KhoaHocHocVien> result = new ArrayList<>();
        String sql = "SELECT id, hocVienID, khoaHocID, ngay_dang_ky, trang_thai FROM khoahoc_hocvien WHERE trang_thai = 'PENDING'";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String ngayDangKy = rs.getTimestamp("ngay_dang_ky") != null ? rs.getTimestamp("ngay_dang_ky").toString() : "";
                ngayDangKy = ngayDangKy.replaceAll("\\.0$", "");
                result.add(new KhoaHocHocVien(
                        rs.getInt("id"),
                        rs.getInt("hocVienID"),
                        rs.getInt("khoaHocID"),
                        ngayDangKy,
                        rs.getString("trang_thai")
                ));
            }
        }
        return result;
    }
}
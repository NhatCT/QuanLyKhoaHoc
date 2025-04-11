package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.ThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentService {
    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    /**
     * Adds a new payment record to thanhtoan and logs it in lichsu_thanhtoan.
     */
    public int addPayment(int hocVienID, int khoaHocID, double soTien, LocalDate ngayThanhToan, 
                          String phuongThuc) throws SQLException {
        if (soTien <= 0 || ngayThanhToan == null || phuongThuc == null || phuongThuc.trim().isEmpty()) {
            LOGGER.warning("Invalid payment details");
            return 0;
        }

        Connection conn = null;
        try {
            conn = Database.getConn();
            conn.setAutoCommit(false);

            // Step 1: Add to thanhtoan
            int thanhToanID = addToThanhToan(conn, hocVienID, khoaHocID, soTien, ngayThanhToan);
            if (thanhToanID <= 0) {
                conn.rollback();
                return 0;
            }

            // Step 2: Add to lichsu_thanhtoan
            int transactionId = addToLichSuThanhToan(conn, hocVienID, khoaHocID, soTien, ngayThanhToan, 
                                                    phuongThuc, thanhToanID);
            if (transactionId <= 0) {
                conn.rollback();
                return 0;
            }

            conn.commit();
            LOGGER.info("Added payment: thanhToanID=" + thanhToanID + ", transactionId=" + transactionId);
            return transactionId;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            LOGGER.log(Level.SEVERE, "Error adding payment", e);
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private int addToThanhToan(Connection conn, int hocVienID, int khoaHocID, double soTien, 
                              LocalDate ngayThanhToan) throws SQLException {
        if (!isValidHocVien(hocVienID) || !isValidCourse(khoaHocID)) {
            LOGGER.warning("Invalid hocVienID=" + hocVienID + " or khoaHocID=" + khoaHocID);
            return 0;
        }

        String sql = "INSERT INTO thanhtoan (hocVienID, khoaHocID, soTien, ngayThanhToan) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, hocVienID);
            stmt.setInt(2, khoaHocID);
            stmt.setDouble(3, soTien);
            stmt.setTimestamp(4, Timestamp.valueOf(ngayThanhToan.atStartOfDay()));
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            return 0;
        }
    }

    private int addToLichSuThanhToan(Connection conn, Integer hocVienID, Integer khoaHocID, double soTien, 
                                    LocalDate ngayThanhToan, String phuongThuc, int thanhToanID) 
                                    throws SQLException {
        String sql = "INSERT INTO lichsu_thanhtoan (hocVienID, khoaHocID, so_tien, ngay_thanh_toan, " +
                     "phuong_thuc, thanhToanID) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            if (hocVienID != null) stmt.setInt(1, hocVienID); else stmt.setNull(1, java.sql.Types.INTEGER);
            if (khoaHocID != null) stmt.setInt(2, khoaHocID); else stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setDouble(3, soTien);
            stmt.setTimestamp(4, Timestamp.valueOf(ngayThanhToan.atStartOfDay()));
            stmt.setString(5, phuongThuc);
            stmt.setInt(6, thanhToanID);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            return 0;
        }
    }

    /**
     * Updates a lichsu_thanhtoan record.
     */
    public boolean updatePayment(int transactionId, Integer hocVienID, Integer khoaHocID, double soTien, 
                                LocalDate ngayThanhToan, String phuongThuc, int thanhToanID) 
                                throws SQLException {
        if (transactionId <= 0 || soTien <= 0 || ngayThanhToan == null || phuongThuc == null || 
            phuongThuc.trim().isEmpty()) {
            LOGGER.warning("Invalid update details: transactionId=" + transactionId);
            return false;
        }

        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE lichsu_thanhtoan SET hocVienID = ?, khoaHocID = ?, so_tien = ?, " +
                     "ngay_thanh_toan = ?, phuong_thuc = ?, thanhToanID = ? WHERE id = ?")) {
            if (hocVienID != null) stmt.setInt(1, hocVienID); else stmt.setNull(1, java.sql.Types.INTEGER);
            if (khoaHocID != null) stmt.setInt(2, khoaHocID); else stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.setDouble(3, soTien);
            stmt.setTimestamp(4, Timestamp.valueOf(ngayThanhToan.atStartOfDay()));
            stmt.setString(5, phuongThuc);
            stmt.setInt(6, thanhToanID);
            stmt.setInt(7, transactionId);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Updated payment: transactionId=" + transactionId + ", rowsAffected=" + rowsAffected);
            return rowsAffected > 0;
        }
    }

    /**
     * Deletes a lichsu_thanhtoan record.
     */
    public boolean deletePayment(int transactionId) throws SQLException {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM lichsu_thanhtoan WHERE id = ?")) {
            stmt.setInt(1, transactionId);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Deleted payment: transactionId=" + transactionId + ", rowsAffected=" + rowsAffected);
            return rowsAffected > 0;
        }
    }

    /**
     * Retrieves all payment history records.
     */
    public List<ThanhToan> getAllPayments() throws SQLException {
        List<ThanhToan> payments = new ArrayList<>();
        String sql = "SELECT id, thanhToanID, ngay_thanh_toan, so_tien, phuong_thuc, " +
                     "hocVienID, khoaHocID " +
                     "FROM lichsu_thanhtoan";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                payments.add(new ThanhToan(
                        String.valueOf(rs.getInt("thanhToanID")),
                        rs.getTimestamp("ngay_thanh_toan").toString(),
                        String.valueOf(rs.getDouble("so_tien")),
                        rs.getString("phuong_thuc"),
                        rs.getString("hocVienID"),
                        rs.getString("khoaHocID"),
                        String.valueOf(rs.getInt("id"))
                ));
            }
            LOGGER.info("Retrieved " + payments.size() + " payment records");
            return payments;
        }
    }

    /**
     * Retrieves a payment by transaction ID.
     */
    public ThanhToan getPaymentByTransactionId(int transactionId) throws SQLException {
        String sql = "SELECT id, thanhToanID, ngay_thanh_toan, so_tien, phuong_thuc, " +
                     "hocVienID, khoaHocID " +
                     "FROM lichsu_thanhtoan WHERE id = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ThanhToan(
                            String.valueOf(rs.getInt("thanhToanID")),
                            rs.getTimestamp("ngay_thanh_toan").toString(),
                            String.valueOf(rs.getDouble("so_tien")),
                            rs.getString("phuong_thuc"),
                            rs.getString("hocVienID"),
                            rs.getString("khoaHocID"),
                            String.valueOf(rs.getInt("id"))
                    );
                }
            }
            LOGGER.info("No payment found for transactionId=" + transactionId);
            return null;
        }
    }

    /**
     * Validates hocvien ID for thanhtoan.
     */
    public boolean isValidHocVien(int hocVienID) throws SQLException {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM hocvien WHERE id = ?")) {
            stmt.setInt(1, hocVienID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Validates nguoidung ID for lichsu_thanhtoan.
     */
    public boolean isValidNguoiDung(int nguoiDungID) throws SQLException {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM nguoidung WHERE id = ?")) {
            stmt.setInt(1, nguoiDungID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Validates course ID.
     */
    public boolean isValidCourse(int khoaHocID) throws SQLException {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM khoahoc WHERE id = ?")) {
            stmt.setInt(1, khoaHocID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
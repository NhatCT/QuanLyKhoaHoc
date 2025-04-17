package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.services.PaymentService;
import com.ntn.quanlykhoahoc.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentDetailsController {
    private static final Logger LOGGER = Logger.getLogger(PaymentDetailsController.class.getName());

    @FXML private Label statusLabel;
    @FXML private Text contentText;
    @FXML private ImageView qrImage;
    @FXML private Button payButton;
    @FXML private Button cancelButton;

    private Consumer<Void> successCallback;
    private int hocVienID;
    private int khoaHocID;
    private double soTien;
    private String username;

    private final PaymentService paymentService = new PaymentService();
    private final UserService userService = new UserService();

    public void initData(Consumer<Void> successCallback, String username, int nguoiDungID, int khoaHocID, double soTien) {
        LOGGER.info("Khởi tạo PaymentDetailsController cho khoaHocID=" + khoaHocID);
        this.successCallback = successCallback;
        this.khoaHocID = khoaHocID;
        this.soTien = soTien;
        this.username = username != null && !username.isEmpty() ? username : "Người dùng";

        // Kiểm tra FXML injection
        if (statusLabel == null || contentText == null || qrImage == null || payButton == null || cancelButton == null) {
            LOGGER.log(Level.SEVERE, "Các thành phần FXML không được tiêm đúng cách.");
            showErrorAlert("Lỗi", "Không thể khởi tạo giao diện thanh toán. Vui lòng liên hệ quản trị viên.");
            return;
        }

        // Lấy hocVienID
        try {
            this.hocVienID = userService.getHocVienIDFromNguoiDung(nguoiDungID);
            if (hocVienID == -1) {
                LOGGER.log(Level.SEVERE, "Không tìm thấy hocVienID cho nguoiDungID: " + nguoiDungID);
                showErrorAlert("Lỗi", "Không tìm thấy thông tin học viên. Vui lòng liên hệ quản trị viên.");
                return;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy hocVienID: " + e.getMessage(), e);
            showErrorAlert("Lỗi", "Không thể lấy thông tin học viên: " + e.getMessage());
            return;
        }

        // Cập nhật giao diện
        statusLabel.setText("Vui lòng chuyển khoản theo thông tin bên dưới:");
        contentText.setText("Nội dung: Thanh toán khóa học " + this.username);

        // Tải mã QR
        try (InputStream qrStream = getClass().getResourceAsStream("/com/ntn/images/qr/qr.jpg")) {
            if (qrStream != null) {
                Image image = new Image(qrStream, 541, 473, true, true);
                qrImage.setImage(image);
                LOGGER.info("Tải mã QR thành công.");
            } else {
                LOGGER.log(Level.SEVERE, "Không tìm thấy mã QR tại /com/ntn/images/qr/qr.jpg");
                statusLabel.setText("Lỗi: Không thể tải mã QR. Vui lòng liên hệ quản trị viên.");
                payButton.setDisable(true);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải mã QR: " + e.getMessage(), e);
            statusLabel.setText("Lỗi: Không thể tải mã QR. Vui lòng liên hệ quản trị viên.");
            payButton.setDisable(true);
            showErrorAlert("Lỗi tải mã QR", "Không thể tải mã QR: " + e.getMessage());
        }
    }

    @FXML
    private void processPayment() {
        LOGGER.info("Nút 'Xác nhận thanh toán' được nhấn cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHocID);
        Connection conn = null;
        try {
            conn = Database.getConn();
            conn.setAutoCommit(false);
            LOGGER.info("Kết nối cơ sở dữ liệu thành công.");

            // Thêm bản ghi thanh toán
            int transactionId = paymentService.addPayment(hocVienID, khoaHocID, soTien, LocalDate.now(), "Chuyển khoản");
            if (transactionId <= 0) {
                LOGGER.log(Level.SEVERE, "Không thể tạo bản ghi thanh toán.");
                conn.rollback();
                showErrorAlert("Lỗi", "Không thể xử lý thanh toán. Vui lòng thử lại.");
                return;
            }
            LOGGER.info("Bản ghi thanh toán tạo thành công: transactionId=" + transactionId);

            // Thêm bản ghi đăng ký
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO khoahoc_hocvien (hocVienID, khoaHocID, ngay_dang_ky, trang_thai) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, hocVienID);
                stmt.setInt(2, khoaHocID);
                stmt.setString(3, LocalDate.now().toString());
                stmt.setString(4, "PENDING");
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (!rs.next()) {
                        LOGGER.log(Level.SEVERE, "Không thể tạo bản ghi đăng ký.");
                        conn.rollback();
                        showErrorAlert("Lỗi", "Không thể tạo bản ghi đăng ký. Vui lòng thử lại.");
                        return;
                    }
                }
            }
            LOGGER.info("Bản ghi đăng ký tạo thành công.");

            // Gửi thông báo cho admin
            try (PreparedStatement notifyStmt = conn.prepareStatement(
                    "INSERT INTO thongbao (nguoi_nhan_id, noi_dung, trang_thai, ngay_gui) VALUES (?, ?, ?, ?)")) {
                int adminId = getAdminId(conn);
                if (adminId == -1) {
                    LOGGER.log(Level.SEVERE, "Không tìm thấy quản trị viên.");
                    conn.rollback();
                    showErrorAlert("Lỗi", "Không tìm thấy quản trị viên để gửi thông báo.");
                    return;
                }
                String courseName = getCourseName(khoaHocID, conn);
                String content = "Học viên " + username + " yêu cầu duyệt đăng ký khóa học: " + courseName;
                notifyStmt.setInt(1, adminId);
                notifyStmt.setString(2, content);
                notifyStmt.setString(3, "UNREAD");
                notifyStmt.setString(4, LocalDateTime.now().toString());
                notifyStmt.executeUpdate();
                LOGGER.info("Thông báo cho admin gửi thành công.");
            }

            conn.commit();
            statusLabel.setText("Đã xác nhận thanh toán! Chờ quản trị viên xét duyệt.");
            payButton.setDisable(true);
            cancelButton.setDisable(true);
            successCallback.accept(null);
            closeWindow();
            LOGGER.info("Thanh toán hoàn tất: transactionId=" + transactionId + ", hocVienID=" + hocVienID + ", khoaHocID=" + khoaHocID);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý thanh toán: " + e.getMessage(), e);
            showErrorAlert("Lỗi", "Không thể xử lý thanh toán: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi rollback: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi đóng kết nối: " + e.getMessage(), e);
                }
            }
        }
    }

    private int getAdminId(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM nguoidung WHERE loai_nguoi_dung_id = 1 LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt("id") : -1;
        }
    }

    private String getCourseName(int khoaHocID, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT ten_khoa_hoc FROM khoahoc WHERE id = ?")) {
            stmt.setInt(1, khoaHocID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("ten_khoa_hoc") : "Khóa học không xác định";
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        LOGGER.info("Đóng cửa sổ thanh toán chi tiết.");
    }
}
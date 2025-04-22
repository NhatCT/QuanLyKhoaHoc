package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.PaymentService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentDashboardController {

    private static final Logger LOGGER = Logger.getLogger(PaymentDashboardController.class.getName());

    @FXML
    private Label totalLabel;
    @FXML
    private TableView<KhoaHoc> orderTable;
    @FXML
    private TableColumn<KhoaHoc, String> courseColumn;
    @FXML
    private TableColumn<KhoaHoc, String> priceColumn;
    @FXML
    private Button confirmPaymentButton;
    @FXML
    private Button cancelButton;

    private int userId;
    private ObservableList<KhoaHoc> courses;
    private Consumer<Void> successCallback;
    private double totalAmount;
    private String username = "Người dùng";
    private boolean isProcessing = false;

    private PaymentService paymentService = new PaymentService();

    public void initData(int userId, double totalAmount, ObservableList<KhoaHoc> courses, Consumer<Void> successCallback, String username) {
        LOGGER.info("Khởi tạo PaymentDashboardController cho userId=" + userId + ", số khóa học: " + courses.size() + ", thời gian: " + System.currentTimeMillis());
        this.userId = userId;
        this.courses = courses;
        this.successCallback = successCallback;
        this.totalAmount = totalAmount;
        this.username = username != null && !username.isEmpty() ? username : "Người dùng";

        courseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenKhoaHoc() != null ? data.getValue().getTenKhoaHoc() : "Không xác định"));
        priceColumn.setCellValueFactory(data -> {
            double price = courses.size() >= 3 ? data.getValue().getGia() * 0.7 : data.getValue().getGia();
            return new SimpleStringProperty(String.format("%,.0f VNĐ", price));
        });
        orderTable.setItems(courses);

        // Áp dụng giảm giá chỉ khi có 3 khóa học trở lên
        if (courses.size() >= 3) {
            double discount = totalAmount * 0.3; // Giảm 30%
            double finalAmount = totalAmount - discount;
            totalLabel.setText(String.format("Tổng cộng: %,d VNĐ (Giảm giá 30%%: %,d VNĐ)", (long) finalAmount, (long) discount));
        } else {
            totalLabel.setText(String.format("Tổng cộng: %,d VNĐ (Không đủ 3 khóa học để giảm giá)", (long) totalAmount));
        }

        confirmPaymentButton.setDisable(courses.isEmpty());
        LOGGER.info("PaymentDashboardController initialized with totalAmount=" + totalAmount);
    }

    private int getHocVienIDFromNguoiDung(int nguoiDungID) throws SQLException {
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("SELECT id FROM hocvien WHERE nguoiDungID = ?")) {
            stmt.setInt(1, nguoiDungID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    @FXML
    private void confirmPayment() {
        if (isProcessing) {
            LOGGER.info("Thanh toán đang được xử lý, bỏ qua yêu cầu mới.");
            return;
        }
        isProcessing = true;
        confirmPaymentButton.setDisable(true);

        try {
            int hocVienID = getHocVienIDFromNguoiDung(userId);
            if (hocVienID == -1) {
                LOGGER.log(Level.SEVERE, "No hocVienID found for nguoiDungID: " + userId);
                showErrorAlert("Lỗi", "Không tìm thấy thông tin học viên. Vui lòng liên hệ quản trị viên.");
                return;
            }

            openPaymentDetails(hocVienID);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error processing payment: " + e.getMessage(), e);
            showErrorAlert("Lỗi", "Không thể xử lý thanh toán: " + e.getMessage());
        } finally {
            isProcessing = false;
            confirmPaymentButton.setDisable(courses.isEmpty());
        }
    }

    private void openPaymentDetails(int hocVienID) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/payment_details.fxml"));
            Parent paymentDetailsPane = loader.load();
            PaymentDetailsController controller = loader.getController();

            // Áp dụng giảm giá chỉ khi có 3 khóa học trở lên
            double finalAmount = courses.size() >= 3 ? totalAmount * 0.7 : totalAmount;

            KhoaHoc representativeCourse = courses.get(0);
            controller.initData(unused -> {
                Platform.runLater(() -> {
                    processAllPayments(hocVienID);
                    successCallback.accept(null);
                    closeWindow();
                });
            }, username, userId, representativeCourse.getId(), finalAmount);

            Stage paymentStage = new Stage();
            paymentStage.initModality(Modality.APPLICATION_MODAL);
            paymentStage.setTitle("Thông tin thanh toán");
            paymentStage.setScene(new Scene(paymentDetailsPane));
            paymentStage.setResizable(false);
            paymentStage.showAndWait();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening payment details: " + e.getMessage(), e);
            showErrorAlert("Lỗi", "Không thể mở cửa sổ thông tin thanh toán: " + e.getMessage());
        }
    }

    private void processAllPayments(int hocVienID) {
        Connection conn = null;
        try {
            conn = Database.getConn();
            conn.setAutoCommit(false);

            for (KhoaHoc khoaHoc : courses) {
                // Kiểm tra trùng lặp
                try (PreparedStatement checkPaymentStmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM lichsu_thanhtoan WHERE hocVienID = ? AND khoaHocID = ?")) {
                    checkPaymentStmt.setInt(1, hocVienID);
                    checkPaymentStmt.setInt(2, khoaHoc.getId());
                    ResultSet rs = checkPaymentStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        LOGGER.warning("Bản ghi thanh toán đã tồn tại cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHoc.getId());
                        Platform.runLater(() -> showErrorAlert("Cảnh báo", "Khóa học " + khoaHoc.getTenKhoaHoc() + " đã được thanh toán trước đó."));
                        continue;
                    }
                }
                // Xử lý thanh toán: Chỉ giảm giá nếu có 3 khóa học trở lên
                double coursePrice = courses.size() >= 3 ? khoaHoc.getGia() * 0.7 : khoaHoc.getGia();
                if (coursePrice <= 0) {
                    LOGGER.warning("Số tiền không hợp lệ cho khoaHocID=" + khoaHoc.getId());
                    continue;
                }
                String sqlPayment = "INSERT INTO lichsu_thanhtoan (hocVienID, khoaHocID, so_tien, ngay_thanh_toan, phuong_thuc) VALUES (?, ?, ?, ?, ?)";
                int thanhToanID;
                try (PreparedStatement stmt = conn.prepareStatement(sqlPayment, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, hocVienID);
                    stmt.setInt(2, khoaHoc.getId());
                    stmt.setDouble(3, coursePrice);
                    stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.setString(5, "Chuyển khoản");
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    thanhToanID = rs.next() ? rs.getInt(1) : 0;
                    LOGGER.info("Đã chèn thanh toán cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHoc.getId() + ", thanhToanID=" + thanhToanID);
                }

                // Xử lý đăng ký khóa học
                try {
                    paymentService.addToKhoaHocHocVien(conn, hocVienID, khoaHoc.getId(), LocalDateTime.now());
                    LOGGER.info("Đã chèn đăng ký cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHoc.getId());
                } catch (SQLException e) {
                    if (e.getMessage().contains("Duplicate entry")) {
                        LOGGER.warning("Bản ghi đăng ký đã tồn tại cho hocVienID=" + hocVienID + ", khoaHocID=" + khoaHoc.getId());
                        continue;
                    }
                    throw e;
                }
            }

            conn.commit();
            Platform.runLater(() -> showErrorAlert("Thành công", "Thanh toán đã được ghi nhận!"));

            // Gửi thông báo cho admin
            for (KhoaHoc khoaHoc : courses) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id FROM lichsu_thanhtoan WHERE hocVienID = ? AND khoaHocID = ? ORDER BY id DESC LIMIT 1")) {
                    stmt.setInt(1, hocVienID);
                    stmt.setInt(2, khoaHoc.getId());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int thanhToanID = rs.getInt("id");
                        paymentService.notifyAdmin(hocVienID, khoaHoc.getId(), thanhToanID);
                    }
                }
            }
            courses.clear();
            LOGGER.info("Thanh toán hoàn tất cho hocVienID=" + hocVienID);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý thanh toán: SQLState=" + e.getSQLState() + ", ErrorCode=" + e.getErrorCode() + ", Message=" + e.getMessage(), e);
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

    @FXML
    private void cancelPayment() {
        closeWindow();
        LOGGER.info("Payment cancelled by user");
    }

    private void closeWindow() {
        Stage stage = (Stage) totalLabel.getScene().getWindow();
        stage.close();
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
}

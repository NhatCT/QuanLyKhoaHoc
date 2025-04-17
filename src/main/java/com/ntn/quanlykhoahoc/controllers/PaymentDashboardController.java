package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class PaymentDashboardController {
    private static final Logger LOGGER = Logger.getLogger(PaymentDashboardController.class.getName());

    @FXML private Label totalLabel;
    @FXML private TableView<KhoaHoc> orderTable;
    @FXML private TableColumn<KhoaHoc, String> courseColumn;
    @FXML private TableColumn<KhoaHoc, String> priceColumn;
    @FXML private Button showPaymentDetailsButton;
    @FXML private Button cancelButton;

    private int userId;
    private ObservableList<KhoaHoc> courses;
    private Consumer<Void> successCallback;
    private double totalAmount;
    private String username = "Người dùng";

    public void initData(int userId, double totalAmount, ObservableList<KhoaHoc> courses, Consumer<Void> successCallback, String username) {
        this.userId = userId;
        this.courses = courses;
        this.successCallback = successCallback;
        this.totalAmount = totalAmount;
        this.username = username != null && !username.isEmpty() ? username : "Người dùng";

        courseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenKhoaHoc() != null ? data.getValue().getTenKhoaHoc() : "Không xác định"));
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(String.format("%,.0f VNĐ", data.getValue().getGia())));
        orderTable.setItems(courses);

        totalLabel.setText(String.format("Tổng cộng: %,d VNĐ", (long) totalAmount));

        LOGGER.info("PaymentDashboardController initialized with userId=" + userId);
    }

    private int getHocVienIDFromNguoiDung(int nguoiDungID) throws SQLException {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM hocvien WHERE nguoiDungID = ?")) {
            stmt.setInt(1, nguoiDungID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    @FXML
    private void showPaymentDetails() throws IOException {
        try {
            int hocVienID = getHocVienIDFromNguoiDung(userId);
            if (hocVienID == -1) {
                LOGGER.log(Level.SEVERE, "No hocVienID found for nguoiDungID: " + userId);
                showErrorAlert("Lỗi", "Không tìm thấy thông tin học viên. Vui lòng liên hệ quản trị viên.");
                return;
            }

            for (KhoaHoc khoaHoc : courses) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/payment_details.fxml"));
                Parent paymentDetailsPane = loader.load();
                PaymentDetailsController controller = loader.getController();
                controller.initData(successCallback, username, userId, khoaHoc.getId(), khoaHoc.getGia());

                Stage paymentStage = new Stage();
                paymentStage.initModality(Modality.APPLICATION_MODAL);
                paymentStage.setTitle("Thông tin thanh toán: " + khoaHoc.getTenKhoaHoc());
                paymentStage.setScene(new Scene(paymentDetailsPane));
                paymentStage.setResizable(false);
                paymentStage.showAndWait();
            }
            successCallback.accept(null);
            orderTable.getScene().getWindow().hide();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error opening payment details: " + e.getMessage(), e);
            showErrorAlert("Lỗi", "Không thể mở cửa sổ thông tin thanh toán: " + e.getMessage());
        }
    }

    @FXML
    private void cancelPayment() {
        orderTable.getScene().getWindow().hide();
        LOGGER.info("Payment cancelled by user");
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
package com.ntn.quanlykhoahoc.controllers;

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
import java.sql.SQLException;
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
    private String username;
    private boolean isProcessing = false;

    private final UserService userService = new UserService();

    public void initData(Consumer<Void> successCallback, String username, int nguoiDungID, int khoaHocID, double soTien) {
        LOGGER.info("Khởi tạo PaymentDetailsController cho nguoiDungID=" + nguoiDungID + ", soTien=" + soTien);
        this.successCallback = successCallback;
        this.username = username != null && !username.isEmpty() ? username : "Người dùng";

        if (statusLabel == null || contentText == null || qrImage == null || payButton == null || cancelButton == null) {
            LOGGER.log(Level.SEVERE, "Các thành phần FXML không được tiêm đúng cách.");
            showErrorAlert("Lỗi", "Không thể khởi tạo giao diện thanh toán. Vui lòng liên hệ quản trị viên.");
            return;
        }

        try {
            int hocVienID = userService.getHocVienIDFromNguoiDung(nguoiDungID);
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

        statusLabel.setText("Vui lòng chuyển khoản theo thông tin bên dưới:");
        contentText.setText(String.format("Nội dung: Thanh toán khóa học %s\nSố tiền: %,d VNĐ (Giảm 30%% từ %,d VNĐ)", 
            this.username, (long) soTien, (long) (soTien / 0.7)));

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
        if (isProcessing) {
            LOGGER.info("Thanh toán đang được xử lý, bỏ qua yêu cầu mới.");
            return;
        }
        isProcessing = true;
        payButton.setDisable(true);

        statusLabel.setText("Đã xác nhận thanh toán! Chờ quản trị viên xét duyệt.");
        cancelButton.setDisable(true);
        successCallback.accept(null);
        LOGGER.info("Xác nhận thanh toán thành công, gọi successCallback.");
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        LOGGER.info("Đóng cửa sổ PaymentDetailsController.");
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
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.pojo.ThanhToan;
import com.ntn.quanlykhoahoc.services.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditPaymentController {
    @FXML private TextField paymentIdTextField;
    @FXML private TextField hocVienIDField;
    @FXML private TextField khoaHocIDField;
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextField amountTextField;
    @FXML private ComboBox<String> methodComboBox;

    private ThanhToan currentPayment;
    private PaymentService paymentService;

    @FXML
    public void initialize() {
        paymentService = new PaymentService();
        methodComboBox.setItems(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
        paymentIdTextField.setEditable(false);
    }

    public void setPayment(ThanhToan payment) {
        this.currentPayment = payment;
        if (payment != null) {
            paymentIdTextField.setText(payment.getThanhToanID());
            hocVienIDField.setText(payment.getHocVienID());
            khoaHocIDField.setText(payment.getKhoaHocID());
            amountTextField.setText(payment.getSoTien());
            methodComboBox.setValue(payment.getPhuongThuc());

            try {
                String ngayThanhToan = payment.getNgayThanhToan();
                if (ngayThanhToan != null && !ngayThanhToan.isEmpty()) {
                    LocalDateTime dateTime = LocalDateTime.parse(
                            ngayThanhToan,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    );
                    paymentDatePicker.setValue(dateTime.toLocalDate());
                } else {
                    paymentDatePicker.setValue(LocalDate.now());
                }
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                paymentDatePicker.setValue(LocalDate.now());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentPayment == null) {
            showAlert("Lỗi", "Không có thông tin thanh toán để cập nhật!", Alert.AlertType.ERROR);
            return;
        }

        String paymentIdText = paymentIdTextField.getText();
        String hocVienIDText = hocVienIDField.getText();
        String khoaHocIDText = khoaHocIDField.getText();
        LocalDate ngayThanhToanLocalDate = paymentDatePicker.getValue();
        String soTienText = amountTextField.getText();
        String phuongThuc = methodComboBox.getValue();

        if (paymentIdText.isEmpty() || ngayThanhToanLocalDate == null || 
            soTienText.isEmpty() || phuongThuc == null || phuongThuc.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ các trường bắt buộc!", Alert.AlertType.WARNING);
            return;
        }

        try {
            int transactionId = Integer.parseInt(paymentIdText);
            Integer hocVienID = hocVienIDText.isEmpty() ? null : Integer.parseInt(hocVienIDText);
            Integer khoaHocID = khoaHocIDText.isEmpty() ? null : Integer.parseInt(khoaHocIDText);
            double soTien = Double.parseDouble(soTienText);

            if (soTien <= 0) {
                showAlert("Cảnh báo", "Số tiền phải lớn hơn 0!", Alert.AlertType.WARNING);
                return;
            }

            LocalDateTime ngayThanhToan = ngayThanhToanLocalDate.atStartOfDay();

            if (hocVienID != null && !paymentService.isValidHocVien(hocVienID)) {
                showAlert("Cảnh báo", "ID Học Viên không tồn tại!", Alert.AlertType.WARNING);
                return;
            }
            if (khoaHocID != null && !paymentService.isValidCourse(khoaHocID)) {
                showAlert("Cảnh báo", "ID Khóa Học không tồn tại!", Alert.AlertType.WARNING);
                return;
            }

            boolean success = paymentService.updatePayment(transactionId, hocVienID, khoaHocID, soTien, 
                                                           ngayThanhToan, phuongThuc);
            if (success) {
                showAlert("Thành công", "Đã cập nhật lịch sử thanh toán!", Alert.AlertType.INFORMATION);
                handleCancel();
            } else {
                showAlert("Thông báo", "Không có bản ghi nào được cập nhật.", Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Cảnh báo", "Số tiền và các ID (nếu có) phải là số!", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật lịch sử thanh toán: " + e.getMessage(), 
                      Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) paymentIdTextField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
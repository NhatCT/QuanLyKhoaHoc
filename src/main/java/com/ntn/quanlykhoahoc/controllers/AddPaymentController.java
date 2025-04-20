package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.services.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;

public class AddPaymentController {
    @FXML private TextField hocVienIDField;
    @FXML private TextField khoaHocIDField;
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextField amountTextField;
    @FXML private ComboBox<String> methodComboBox;

    private PaymentService paymentService;

    @FXML
    public void initialize() {
        paymentService = new PaymentService();
        methodComboBox.setItems(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
        methodComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAdd() {
        String hocVienIDText = hocVienIDField.getText();
        String khoaHocIDText = khoaHocIDField.getText();
        LocalDate ngayThanhToanLocalDate = paymentDatePicker.getValue();
        String soTienText = amountTextField.getText();
        String phuongThuc = methodComboBox.getValue();

        if (hocVienIDText.isEmpty() || khoaHocIDText.isEmpty() || ngayThanhToanLocalDate == null || 
            soTienText.isEmpty() || phuongThuc == null || phuongThuc.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        try {
            int hocVienID = Integer.parseInt(hocVienIDText);
            int khoaHocID = Integer.parseInt(khoaHocIDText);
            double soTien = Double.parseDouble(soTienText);

            // Kiểm tra hợp lệ
            if (!paymentService.isValidHocVien(hocVienID)) {
                showAlert("Cảnh báo", "ID Học Viên không tồn tại$$$!", Alert.AlertType.WARNING);
                return;
            }
            if (!paymentService.isValidCourse(khoaHocID)) {
                showAlert("Cảnh báo", "ID Khóa Học không tồn tại!", Alert.AlertType.WARNING);
                return;
            }

            int thanhToanID = paymentService.addPayment(hocVienID, khoaHocID, soTien, 
                                                        ngayThanhToanLocalDate, phuongThuc);
            if (thanhToanID > 0) {
                showAlert("Thành công", "Đã thêm lịch sử thanh toán (ID Thanh Toán: " + thanhToanID + ")!", 
                          Alert.AlertType.INFORMATION);
                handleCancel();
            } else {
                showAlert("Lỗi", "Không thể thêm thanh toán!", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Cảnh báo", "ID Học Viên, ID Khóa Học và Số Tiền phải là số!", 
                      Alert.AlertType.WARNING);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể thêm thanh toán: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) hocVienIDField.getScene().getWindow();
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
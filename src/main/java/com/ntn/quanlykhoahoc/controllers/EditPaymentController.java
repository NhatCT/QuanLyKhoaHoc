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

public class EditPaymentController {
    @FXML private TextField transactionIdTextField;  // lichsu_thanhtoan.id
    @FXML private TextField paymentIdTextField;     // lichsu_thanhtoan.thanhToanID (read-only)
    @FXML private TextField hocVienIDField;         // lichsu_thanhtoan.hocVienID
    @FXML private TextField khoaHocIDField;         // lichsu_thanhtoan.khoaHocID
    @FXML private DatePicker paymentDatePicker;     // lichsu_thanhtoan.ngay_thanh_toan
    @FXML private TextField amountTextField;        // lichsu_thanhtoan.so_tien
    @FXML private ComboBox<String> methodComboBox;  // lichsu_thanhtoan.phuong_thuc

    private ThanhToan currentPayment;
    private PaymentService paymentService = new PaymentService();

    @FXML
    public void initialize() {
        // Populate payment method options
        methodComboBox.setItems(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
        // Make transactionId and paymentId read-only
        transactionIdTextField.setEditable(false);
        paymentIdTextField.setEditable(false);
    }

    /**
     * Sets the payment data to be edited.
     * @param payment The ThanhToan object representing the lichsu_thanhtoan record.
     */
    public void setPayment(ThanhToan payment) {
        this.currentPayment = payment;
        if (payment != null) {
            transactionIdTextField.setText(payment.getTransactionId());
            paymentIdTextField.setText(payment.getThanhToanID());
            hocVienIDField.setText(payment.getHocVienID());
            khoaHocIDField.setText(payment.getKhoaHocID());
            amountTextField.setText(payment.getSoTien());
            methodComboBox.setValue(payment.getPhuongThuc());

            try {
                // Parse the datetime string from the database (e.g., "2025-01-02 10:00:00")
                LocalDateTime dateTime = LocalDateTime.parse(
                        payment.getNgayThanhToan(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                paymentDatePicker.setValue(dateTime.toLocalDate());
            } catch (Exception e) {
                e.printStackTrace();
                paymentDatePicker.setValue(LocalDate.now()); // Fallback to current date
            }
        }
    }

    @FXML
    private void handleSave() {
        if (currentPayment == null) {
            showAlert("Lỗi", "Không có thông tin thanh toán để cập nhật!", Alert.AlertType.ERROR);
            return;
        }

        String transactionIdText = transactionIdTextField.getText();
        String paymentIdText = paymentIdTextField.getText();
        String hocVienIDText = hocVienIDField.getText();
        String khoaHocIDText = khoaHocIDField.getText();
        LocalDate ngayThanhToanLocalDate = paymentDatePicker.getValue();
        String soTienText = amountTextField.getText();
        String phuongThuc = methodComboBox.getValue();

        // Validate required fields
        if (transactionIdText.isEmpty() || paymentIdText.isEmpty() || ngayThanhToanLocalDate == null || 
            soTienText.isEmpty() || phuongThuc == null || phuongThuc.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ các trường bắt buộc!", Alert.AlertType.WARNING);
            return;
        }

        try {
            int transactionId = Integer.parseInt(transactionIdText);
            int thanhToanID = Integer.parseInt(paymentIdText);
            Integer hocVienID = hocVienIDText.isEmpty() ? null : Integer.parseInt(hocVienIDText);
            Integer khoaHocID = khoaHocIDText.isEmpty() ? null : Integer.parseInt(khoaHocIDText);
            double soTien = Double.parseDouble(soTienText);

            // Validate optional fields
            if (hocVienID != null && !paymentService.isValidNguoiDung(hocVienID)) {
                showAlert("Cảnh báo", "ID Học Viên không tồn tại trong bảng nguoidung!", 
                          Alert.AlertType.WARNING);
                return;
            }
            if (khoaHocID != null && !paymentService.isValidCourse(khoaHocID)) {
                showAlert("Cảnh báo", "ID Khóa Học không tồn tại!", Alert.AlertType.WARNING);
                return;
            }

            // Update the payment record in lichsu_thanhtoan
            boolean success = paymentService.updatePayment(transactionId, hocVienID, khoaHocID, soTien, 
                                                          ngayThanhToanLocalDate, phuongThuc, thanhToanID);
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
        Stage stage = (Stage) transactionIdTextField.getScene().getWindow();
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
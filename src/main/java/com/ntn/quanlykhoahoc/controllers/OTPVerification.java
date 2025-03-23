package com.ntn.quanlykhoahoc.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class OTPVerification {
    @FXML
    private TextField otpField;
    @FXML
    private Button verifyButton;

    private String correctOTP;
    private String email;

    public void setEmailAndOTP(String email, String otp) {
        this.email = email;
        this.correctOTP = otp;
    }

    @FXML
    private void handleVerifyOTP() {
        String enteredOTP = otpField.getText().trim();

        if (enteredOTP.equals(correctOTP)) {
            showAlert("Thành công", "OTP hợp lệ! Hãy đặt lại mật khẩu.", Alert.AlertType.INFORMATION);
            openResetPasswordWindow(email);
            closeWindow();
        } else {
            showAlert("Lỗi", "Mã OTP không chính xác. Vui lòng thử lại.", Alert.AlertType.ERROR);
        }
    }

    private void openResetPasswordWindow(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/reset_password.fxml"));
            Parent root = loader.load();
            ResetPassword controller = loader.getController();
            controller.setEmail(email);
            Stage stage = new Stage();
            stage.setTitle("Đặt lại mật khẩu");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) verifyButton.getScene().getWindow();
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

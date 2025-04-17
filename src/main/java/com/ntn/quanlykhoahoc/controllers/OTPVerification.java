package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.services.NavigationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;

public class OTPVerification {

    @FXML private TextField otpField;
    @FXML private Button verifyButton;

    private String correctOTP;
    private LocalDateTime otpExpiryTime;
    private String email;

    private final NavigationService navigationService = new NavigationService();

    public void setEmailAndOTP(String email, String otp, LocalDateTime expiryTime) {
        this.email = email;
        this.correctOTP = otp;
        this.otpExpiryTime = expiryTime;
    }

    @FXML
    private void handleVerifyOTP() {
        String enteredOTP = otpField.getText().trim();

        if (enteredOTP.isEmpty()) {
            navigationService.showAlert("Lỗi", "Vui lòng nhập mã OTP.", Alert.AlertType.ERROR);
            return;
        }

        if (correctOTP == null || LocalDateTime.now().isAfter(otpExpiryTime)) {
            navigationService.showAlert("Lỗi", "OTP hết hạn hoặc không hợp lệ.", Alert.AlertType.ERROR);
            clearOTP();
            return;
        }

        if (enteredOTP.equals(correctOTP)) {
            navigationService.showAlert("Thành công", "OTP hợp lệ. Đặt lại mật khẩu.", Alert.AlertType.INFORMATION);
            navigationService.openResetPasswordWindow(email, verifyButton);
        } else {
            navigationService.showAlert("Lỗi", "OTP không đúng.", Alert.AlertType.ERROR);
        }
    }

    private void clearOTP() {
        correctOTP = null;
        otpExpiryTime = null;
    }
}
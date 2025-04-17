package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.services.*;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import javafx.scene.control.Alert;

public class ForgotPassword {

    @FXML
    private TextField emailField;
    @FXML
    private Button sendEmailButton;

    private final UserService userService = new UserService();
    private final OTPService otpService = new OTPService();
    private final EmailService emailService = new EmailService();
    private final NavigationService navigationService = new NavigationService();

    @FXML
    private void handleSendEmail() throws SQLException {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            navigationService.showAlert("Lỗi", "Vui lòng nhập email.", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra email hợp lệ trước
        if (!emailService.isValidEmail(email)) {
            navigationService.showAlert("Lỗi", "Email không hợp lệ. Vui lòng nhập email đúng định dạng (ví dụ: user@domain.com).", Alert.AlertType.ERROR);
            return;
        }

        if (!userService.isEmailExists(email)) {
            navigationService.showAlert("Lỗi", "Email không tồn tại.", Alert.AlertType.ERROR);
            return;
        }

        if (!otpService.canRequestOTP(email)) {
            navigationService.showAlert("Lỗi", "Quá số lần yêu cầu OTP. Hãy thử lại sau 10 phút.", Alert.AlertType.ERROR);
            return;
        }

        String otp = otpService.generateOTP();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        if (emailService.sendOtpEmail(email, otp)) {
            navigationService.showAlert("Thành công", "Mã OTP đã được gửi.", Alert.AlertType.INFORMATION);
            navigationService.openOTPVerificationWindow(email, otp, expiry, sendEmailButton);
        } else {
            navigationService.showAlert("Lỗi", "Không gửi được email.", Alert.AlertType.ERROR);
        }
    }
}
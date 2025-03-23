package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.Random;

public class ForgotPassword {
    @FXML
    private TextField emailField;
    @FXML
    private Button sendEmailButton;

    private String generatedOTP;

    @FXML
    private void handleSendEmail() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập email.", Alert.AlertType.WARNING);
            return;
        }

        if (!isEmailRegistered(email)) {
            showAlert("Lỗi", "Email không tồn tại trong hệ thống.", Alert.AlertType.ERROR);
            return;
        }

        generatedOTP = generateOTP();
        System.out.println("OTP tạo ra: " + generatedOTP);

        boolean emailSent = sendEmail(email, generatedOTP);

        if (emailSent) {
            showAlert("Thành công", "Mã OTP đã được gửi đến email của bạn.", Alert.AlertType.INFORMATION);
            openOTPVerificationWindow(email);
        } else {
            showAlert("Lỗi", "Không thể gửi email. Vui lòng thử lại.", Alert.AlertType.ERROR);
        }
    }

    private boolean isEmailRegistered(String email) {
        String query = "SELECT id FROM nguoidung WHERE email = ?";

        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private boolean sendEmail(String toEmail, String otp) {
        final String fromEmail = "nhatlovely2017@gmail.com";
        final String password = "wtld wofv rezg vccz";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Khôi phục mật khẩu - Quản Lý Khóa Học");
            message.setText("Mã OTP của bạn là: " + otp);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openOTPVerificationWindow(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/otp_verification.fxml"));
            Parent root = loader.load();
            
            OTPVerification controller = loader.getController();
            controller.setEmailAndOTP(email, generatedOTP);
            
            Stage stage = new Stage();
            stage.setTitle("Xác thực OTP");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ xác thực OTP!", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

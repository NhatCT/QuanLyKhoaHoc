package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.services.EmailService;
import com.ntn.quanlykhoahoc.services.NavigationService;
import com.ntn.quanlykhoahoc.services.PasswordService;

import java.io.IOException;
import java.sql.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class Register {

    @FXML private TextField hoField, tenField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;

    private final PasswordService passwordService = new PasswordService();
    private final EmailService emailService = new EmailService();
    private final NavigationService navigationService = new NavigationService();

    @FXML
    private void handleRegister() {
        if (registerButton.getScene() != null) {
            registerButton.getScene().getRoot().requestFocus();
        }

        String ho = hoField.getText().trim().replaceAll("\\s+", " ");
        String ten = tenField.getText().trim().replaceAll("\\s+", " ");
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (ho.isEmpty() || ten.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            navigationService.showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin.", Alert.AlertType.WARNING);
            return;
        }

        if (!ho.matches("^[\\p{L} .'-]+$") || !ten.matches("^[\\p{L} .'-]+$")) {
            navigationService.showAlert("Lỗi", "Họ và tên không được chứa ký tự đặc biệt.", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            navigationService.showAlert("Lỗi", "Mật khẩu không trùng khớp.", Alert.AlertType.ERROR);
            return;
        }

        String passwordValidation = passwordService.validatePassword(password);
        if (passwordValidation != null) {
            navigationService.showAlert("Lỗi", passwordValidation, Alert.AlertType.ERROR);
            return;
        }

        if (!emailService.isValidEmail(email)) {
            navigationService.showAlert("Lỗi", "Email không hợp lệ.", Alert.AlertType.ERROR);
            return;
        }

        if (isEmailExists(email)) {
            navigationService.showAlert("Lỗi", "Email này đã được sử dụng.", Alert.AlertType.ERROR);
            return;
        }

        int loaiNguoiDungID = 3;
        String hashedPassword = passwordService.hashPassword(password);

        if (registerUser(ho, ten, email, hashedPassword, loaiNguoiDungID)) {
            navigationService.showAlert("Thành công", "Đăng ký thành công!", Alert.AlertType.INFORMATION);
            try {
                handleLogin(new ActionEvent());
            } catch (Exception e) {
                e.printStackTrace();
                navigationService.showAlert("Lỗi", "Không thể chuyển đến màn hình đăng nhập: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            navigationService.showAlert("Lỗi", "Đã có lỗi xảy ra khi đăng ký.", Alert.AlertType.ERROR);
        }
    }

    private boolean isEmailExists(String email) {
        String sql = "SELECT email FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean registerUser(String ho, String ten, String email, String hashedPassword, int loaiNguoiDungID) {
        String sql = "INSERT INTO nguoidung (ho, ten, email, mat_khau, loai_nguoi_dung_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ho);
            stmt.setString(2, ten);
            stmt.setString(3, email);
            stmt.setString(4, hashedPassword);
            stmt.setInt(5, loaiNguoiDungID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        navigationService.openWindow("/com/ntn/views/login.fxml", "Đăng Nhập", 600, 500, emailField);
    }
}
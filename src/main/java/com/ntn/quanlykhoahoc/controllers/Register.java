package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.database.Database;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import javafx.event.ActionEvent;

public class Register {

    @FXML
    private TextField fullNameField, emailField;
    @FXML
    private PasswordField passwordField, confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Hyperlink loginLink;

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu không trùng khớp.");
            return;
        }

        if (!isValidPassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải từ 8-16 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email không hợp lệ.");
            return;
        }

        // Mặc định loại người dùng là "Học viên" (ID = 3)
        int loaiNguoiDungID = 3;
        String hashedPassword = hashPassword(password);

        if (registerUser(fullName, email, hashedPassword, loaiNguoiDungID)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email đã tồn tại hoặc có lỗi xảy ra.");
        }
    }

    private boolean registerUser(String fullName, String email, String hashedPassword, int loaiNguoiDungID) {
        String sql = "INSERT INTO nguoidung (ho_ten, email, mat_khau, loai_nguoi_dung_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setInt(4, loaiNguoiDungID);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W]{8,16}$");
    }

    public boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        App.setRoot("login");
    }
}

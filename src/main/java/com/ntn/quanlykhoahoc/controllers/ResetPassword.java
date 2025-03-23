package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResetPassword {
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button resetButton;

    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Kiểm tra mật khẩu nhập vào
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.", Alert.AlertType.WARNING);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Lỗi", "Mật khẩu không khớp.", Alert.AlertType.ERROR);
            return;
        }

        if (!isValidPassword(newPassword)) {
            showAlert("Lỗi", "Mật khẩu phải có ít nhất 8 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt.", Alert.AlertType.ERROR);
            return;
        }

        // Cập nhật mật khẩu trong CSDL
        if (updatePasswordInDatabase(email, newPassword)) {
            showAlert("Thành công", "Mật khẩu đã được cập nhật thành công!", Alert.AlertType.INFORMATION);
            closeWindow();
        } else {
            showAlert("Lỗi", "Không thể cập nhật mật khẩu. Vui lòng thử lại.", Alert.AlertType.ERROR);
        }
    }

    private boolean updatePasswordInDatabase(String email, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12)); // Băm mật khẩu trước khi lưu

        String query = "UPDATE nguoidung SET mat_khau = ? WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W]{8,}$");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) resetButton.getScene().getWindow();
        stage.close();
    }
}

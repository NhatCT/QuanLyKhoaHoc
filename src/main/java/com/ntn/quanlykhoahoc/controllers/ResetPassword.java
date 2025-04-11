package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.services.UserService;
import com.ntn.quanlykhoahoc.services.NavigationService;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class ResetPassword {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;

    private String email;
    private final UserService userService = new UserService();
    private final NavigationService navigationService = new NavigationService();

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword() throws SQLException {
        String newPass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (newPass.isEmpty() || confirm.isEmpty()) {
            navigationService.showAlert("Lỗi", "Vui lòng nhập đủ thông tin.", Alert.AlertType.ERROR);
            return;
        }

        if (!newPass.equals(confirm)) {
            navigationService.showAlert("Lỗi", "Mật khẩu không khớp.", Alert.AlertType.ERROR);
            return;
        }

        if (userService.updatePassword(email, newPass)) {
            navigationService.showAlertAndRedirect("Thành công", "Mật khẩu đã cập nhật!", Alert.AlertType.INFORMATION, "login");
        } else {
            navigationService.showAlert("Lỗi", "Cập nhật thất bại.", Alert.AlertType.ERROR);
        }
    }
}

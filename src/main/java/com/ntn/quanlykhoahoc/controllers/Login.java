package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.database.Database;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Login {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("Học viên", "Giảng viên", "Quản trị viên");
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        if (authenticate(email, password, role)) {
            showAlert("Thành công", "Đăng nhập thành công!", Alert.AlertType.INFORMATION);
            navigateToDashboard(role);
        } else {
            showAlert("Thất bại", "Email, mật khẩu hoặc vai trò không đúng!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) throws IOException {
        App.setRoot("register");
    }

    public boolean authenticate(String email, String password, String role) {
        String query = "SELECT mat_khau FROM nguoidung WHERE email = ? " +
                       "AND loai_nguoi_dung_id = (SELECT id FROM loainguoidung WHERE ten_loai = ?)";

        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("mat_khau");
                return BCrypt.checkpw(password, hashedPassword);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    @FXML
private void handleForgotPassword() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/forgot_password.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Quên Mật Khẩu");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    private void navigateToDashboard(String role) {
        try {
            switch (role) {
                case "Học viên":
                    App.setRoot("dashboard_student");
                    break;
                case "Giảng viên":
                    App.setRoot("dashboard_teacher");
                    break;
                case "Quản trị viên":
                    App.setRoot("dashboard_admin");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể tải giao diện!", Alert.AlertType.ERROR);
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

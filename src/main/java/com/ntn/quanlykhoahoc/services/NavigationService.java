package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.controllers.OTPVerification;
import com.ntn.quanlykhoahoc.controllers.ResetPassword;
import com.ntn.quanlykhoahoc.controllers.ResetPassword;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class NavigationService {
    public void showAlert(String title, String msg, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public void showAlertAndRedirect(String title, String msg, Alert.AlertType type, String fxml) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
            try {
                com.ntn.quanlykhoahoc.App.setRoot(fxml);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void openOTPVerificationWindow(String email, String otp, LocalDateTime expiry) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/otp_verification.fxml"));
            Parent root = loader.load();
            OTPVerification controller = loader.getController();
            controller.setEmailAndOTP(email, otp, expiry);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Xác minh OTP");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openResetPasswordWindow(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/reset_password.fxml"));
            Parent root = loader.load();
            ResetPassword controller = loader.getController();
            controller.setEmail(email);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Đặt lại mật khẩu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWindow(Node node) {
        Platform.runLater(() -> {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) stage.close();
        });
    }
}

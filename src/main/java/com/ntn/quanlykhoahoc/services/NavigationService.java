package com.ntn.quanlykhoahoc.services;

import com.ntn.quanlykhoahoc.controllers.OTPVerification;
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

    public void showAlertAndRedirect(String title, String msg, Alert.AlertType type, String fxmlPath, String windowTitle, double width, double height, Node sourceNode) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
            openWindow(fxmlPath, windowTitle, width, height, sourceNode);
        });
    }

    public void openWindow(String fxmlPath, String title, double width, double height, Node sourceNode) {
        try {
            System.out.println("Opening window: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.show();
            System.out.println("Window opened: " + title);
            if (sourceNode != null) {
                closeWindow(sourceNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở giao diện: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void openOTPVerificationWindow(String email, String otp, LocalDateTime expiry, Node sourceNode) {
        try {
            System.out.println("Opening OTP Verification window for email: " + email);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/otp_verification.fxml"));
            Parent root = loader.load();
            OTPVerification controller = loader.getController();
            controller.setEmailAndOTP(email, otp, expiry);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 500, 400));
            stage.setTitle("Xác minh OTP");
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.show();
            System.out.println("OTP Verification window opened");
            if (sourceNode != null) {
                closeWindow(sourceNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở giao diện xác minh OTP: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void openResetPasswordWindow(String email, Node sourceNode) {
        try {
            System.out.println("Opening Reset Password window for email: " + email);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/reset_password.fxml"));
            Parent root = loader.load();
            ResetPassword controller = loader.getController();
            controller.setEmail(email);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 500, 400));
            stage.setTitle("Đặt lại mật khẩu");
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.show();
            System.out.println("Reset Password window opened");
            if (sourceNode != null) {
                closeWindow(sourceNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở giao diện đặt lại mật khẩu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void closeWindow(Node node) {
        Platform.runLater(() -> {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) {
                System.out.println("Closing window: " + stage.getTitle());
                stage.close();
            }
        });
    }
}
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.services.NavigationService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class Login {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 10;

    private final NavigationService navigationService = new NavigationService();

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
            navigationService.showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        if (!isEmailExists(email)) {
            navigationService.showAlert("Lỗi", "Email không tồn tại trong hệ thống!", Alert.AlertType.ERROR);
            return;
        }

        if (isAccountLocked(email)) {
            navigationService.showAlert("Lỗi", "Tài khoản của bạn đã bị khóa. Vui lòng thử lại sau " + LOCKOUT_MINUTES + " phút!", Alert.AlertType.ERROR);
            return;
        }

        if (authenticate(email, password, role)) {
            resetLoginAttempts(email);
            SessionManager.setLoggedInEmail(email);
            navigationService.showAlert("Thành công", "Đăng nhập thành công!", Alert.AlertType.INFORMATION);
            navigateToDashboard(role);
        } else {
            incrementLoginAttempts(email);
            int attempts = getLoginAttempts(email);
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                lockAccount(email);
                navigationService.showAlert("Lỗi", "Bạn đã nhập sai mật khẩu quá " + MAX_LOGIN_ATTEMPTS + " lần. Tài khoản đã bị khóa trong " + LOCKOUT_MINUTES + " phút!", Alert.AlertType.ERROR);
            } else {
                navigationService.showAlert("Thất bại", "Email, mật khẩu hoặc vai trò không đúng! Bạn còn " + (MAX_LOGIN_ATTEMPTS - attempts) + " lần thử.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        navigationService.openWindow("/com/ntn/views/register.fxml", "Đăng ký", 600, 500, emailField);
    }

    @FXML
    private void handleForgotPassword() {
        navigationService.openWindow("/com/ntn/views/forgot_password.fxml", "Quên Mật Khẩu", 500, 400, emailField);
    }

    private boolean isEmailExists(String email) {
        String query = "SELECT 1 FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean authenticate(String email, String password, String role) {
        String query = "SELECT mat_khau FROM nguoidung WHERE email = ? "
                + "AND loai_nguoi_dung_id = (SELECT id FROM loainguoidung WHERE ten_loai = ? LIMIT 1)";

        try (Connection conn = Database.getConn()) {
            if (conn == null) {
                navigationService.showAlert("Lỗi kết nối", "Không thể kết nối đến cơ sở dữ liệu!", Alert.AlertType.ERROR);
                return false;
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                stmt.setString(2, role);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String hashedPassword = rs.getString("mat_khau");
                    if (hashedPassword != null && !hashedPassword.isEmpty()) {
                        return BCrypt.checkpw(password, hashedPassword);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            navigationService.showAlert("Lỗi", "Có lỗi xảy ra khi kiểm tra đăng nhập!", Alert.AlertType.ERROR);
        }
        return false;
    }

    private boolean isAccountLocked(String email) {
        String query = "SELECT thoi_gian_khoa_tai_khoan FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp lockoutTime = rs.getTimestamp("thoi_gian_khoa_tai_khoan");
                if (lockoutTime != null) {
                    LocalDateTime lockoutDateTime = lockoutTime.toLocalDateTime();
                    LocalDateTime now = LocalDateTime.now();
                    long minutesSinceLockout = ChronoUnit.MINUTES.between(lockoutDateTime, now);
                    if (minutesSinceLockout < LOCKOUT_MINUTES) {
                        return true;
                    } else {
                        resetLoginAttempts(email);
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getLoginAttempts(String email) {
        String query = "SELECT so_lan_thu_dang_nhap_sai FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("so_lan_thu_dang_nhap_sai");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void incrementLoginAttempts(String email) {
        String query = "UPDATE nguoidung SET so_lan_thu_dang_nhap_sai = so_lan_thu_dang_nhap_sai + 1 WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetLoginAttempts(String email) {
        String query = "UPDATE nguoidung SET so_lan_thu_dang_nhap_sai = 0, thoi_gian_khoa_tai_khoan = NULL WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void lockAccount(String email) {
        String query = "UPDATE nguoidung SET thoi_gian_khoa_tai_khoan = ? WHERE email = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(String role) {
        String fxmlFile;
        String title;
        switch (role) {
            case "Học viên":
                fxmlFile = "/com/ntn/views/dashboard_student.fxml";
                title = "Dashboard Học Viên";
                break;
            case "Giảng viên":
                fxmlFile = "/com/ntn/views/dashboard_teacher.fxml";
                title = "Dashboard Giảng Viên";
                break;
            case "Quản trị viên":
                fxmlFile = "/com/ntn/views/dashboard_admin.fxml";
                title = "Dashboard Quản Trị Viên";
                break;
            default:
                throw new IllegalArgumentException("Vai trò không hợp lệ: " + role);
        }
        navigationService.openWindow(fxmlFile, title, 800, 600, emailField);
        Platform.runLater(() -> {
            Stage stage = (Stage) emailField.getScene().getWindow();
            System.out.println("Dashboard opened - Maximized: " + stage.isMaximized() + ", Size: " + stage.getWidth() + "x" + stage.getHeight());
        });
    }
}
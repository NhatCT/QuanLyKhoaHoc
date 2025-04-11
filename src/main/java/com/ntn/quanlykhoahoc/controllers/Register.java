package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.database.Database;
import java.io.IOException;
import java.sql.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;
import javafx.event.ActionEvent;

public class Register {

    @FXML
    private TextField hoField;
    @FXML
    private TextField tenField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField, confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Hyperlink loginLink;

    @FXML
    private void handleRegister() {
        // Xóa tiêu điểm khỏi các ô nhập liệu
        if (registerButton.getScene() != null) {
            registerButton.getScene().getRoot().requestFocus();
        }

        String ho = hoField.getText().trim();
        String ten = tenField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Kiểm tra dữ liệu đầu vào
        if (ho.isEmpty() || ten.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        // Kiểm tra email đã tồn tại chưa
        if (isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email này đã được sử dụng.");
            return;
        }

        // Mặc định loại người dùng là "Học viên" (ID = 3)
        int loaiNguoiDungID = 3;
        String hashedPassword = hashPassword(password);

        if (registerUser(ho, ten, email, hashedPassword, loaiNguoiDungID)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công!");
            try {
                handleLogin(new ActionEvent()); // Chuyển về màn hình đăng nhập sau khi đăng ký thành công
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chuyển đến màn hình đăng nhập: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã có lỗi xảy ra khi đăng ký.");
        }
    }

    private boolean isEmailExists(String email) {
        String sql = "SELECT email FROM nguoidung WHERE email = ?";
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Nếu có email => đã tồn tại
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

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean isValidPassword(String password) {
        return password.matches("^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[A-Za-z\\d\\W]{8,16}$");
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            return false;
        }

        // Tách phần tên miền để kiểm tra
        String domain = email.substring(email.lastIndexOf("@") + 1);
        String topLevelDomain = domain.substring(domain.lastIndexOf(".") + 1);

        // Đảm bảo top-level domain chứa ít nhất một chữ cái (tránh các trường hợp như "1.2")
        return topLevelDomain.matches(".*[A-Za-z].*");
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

package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {
    @FXML private ImageView avatarImage;
    @FXML private Label hoLabel;
    @FXML private Label tenLabel;
    @FXML private Label emailLabel;
    @FXML private Label loaiNguoiDungLabel;
    @FXML private Label activeLabel;

    private String adminEmail = "admin1@example.com";

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT ho, ten, email, active, loai_nguoi_dung_id, avatar FROM nguoidung WHERE email = ?"
             )) {
            stmt.setString(1, adminEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hoLabel.setText(rs.getString("ho"));
                tenLabel.setText(rs.getString("ten"));
                emailLabel.setText(rs.getString("email"));
                activeLabel.setText(rs.getBoolean("active") ? "Hoạt động" : "Vô hiệu");
                int loaiNguoiDung = rs.getInt("loai_nguoi_dung_id");
                loaiNguoiDungLabel.setText(
                    loaiNguoiDung == 0 ? "Admin" :
                    loaiNguoiDung == 1 ? "Sinh viên" :
                    loaiNguoiDung == 2 ? "Giảng viên" : "Không xác định"
                );

                String avatarPath = rs.getString("avatar");
                Image image = null;
                if (avatarPath != null && !avatarPath.trim().isEmpty()) {
                    try {
                        InputStream imageStream = getClass().getResourceAsStream(avatarPath);
                        if (imageStream != null) {
                            image = new Image(imageStream, 100, 100, true, true);
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading avatar: " + avatarPath + ". Exception: " + e.getMessage());
                    }
                }

                if (image == null) {
                    try {
                        InputStream defaultStream = getClass().getResourceAsStream("/com/ntn/images/default.jpg");
                        if (defaultStream != null) {
                            image = new Image(defaultStream, 100, 100, true, true);
                        } else {
                            image = new Image("https://via.placeholder.com/100", 100, 100, true, true);
                        }
                    } catch (Exception e) {
                        image = new Image("https://via.placeholder.com/100", 100, 100, true, true);
                    }
                }
                avatarImage.setImage(image);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) hoLabel.getScene().getWindow();
        stage.close();
    }
}
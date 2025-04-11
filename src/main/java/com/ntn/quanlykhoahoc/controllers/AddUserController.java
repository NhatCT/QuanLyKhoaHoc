package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.services.PasswordService;
import com.ntn.quanlykhoahoc.services.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ntn.quanlykhoahoc.database.Database;

import javafx.scene.control.Button;

public class AddUserController implements Initializable {

    @FXML private TextField hoField;
    @FXML private TextField tenField;
    @FXML private TextField emailField;
    @FXML private TextField matKhauField;
    @FXML private ComboBox<String> loaiNguoiDungComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private TextField avatarField;
    @FXML private ImageView imagePreview;
    @FXML private Button chooseImageButton;
    @FXML private Button clearImageButton;
    @FXML private Label imageStatusLabel;

    private UserService userService = new UserService();
    private PasswordService passwordService = new PasswordService();
    private File selectedImageFile;
    private static final String LOCAL_IMAGE_DIR = "src/main/resources/com/ntn/images/avatars/";
    private static final String DEFAULT_AVATAR = "/com/ntn/images/avatars/default.jpg";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        File dir = new File(LOCAL_IMAGE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            loaiNguoiDungComboBox.setItems(FXCollections.observableArrayList(userService.getLoaiNguoiDungList()));
        } catch (SQLException ex) {
            Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (chooseImageButton != null) {
            chooseImageButton.setOnAction(e -> chooseImage());
        }
        if (clearImageButton != null) {
            clearImageButton.setOnAction(e -> clearImage());
        }

        loaiNguoiDungComboBox.setOnAction(e -> {
            String selected = loaiNguoiDungComboBox.getValue();
            if (selected != null) {
                if (selected.contains("Quản trị")) {
                    hoField.getScene().getRoot().setStyle("-fx-background-color: #f1c40f;");
                } else if (selected.contains("Học viên")) {
                    hoField.getScene().getRoot().setStyle("-fx-background-color: #2ecc71;");
                } else {
                    hoField.getScene().getRoot().setStyle("-fx-background-color: #ecf0f1;");
                }
            }
        });

        avatarField.setText(DEFAULT_AVATAR); // Mặc định hiển thị đường dẫn avatar mặc định
    }

    @FXML
    private void handleAdd() {
        String ho = hoField.getText().trim();
        String ten = tenField.getText().trim();
        String email = emailField.getText().trim();
        String matKhau = matKhauField.getText().trim();
        String loaiNguoiDungSelection = loaiNguoiDungComboBox.getValue();
        boolean active = activeCheckBox.isSelected();

        clearFieldHighlight(hoField);
        clearFieldHighlight(tenField);
        clearFieldHighlight(emailField);
        clearFieldHighlight(matKhauField);

        if (ho.isEmpty() || ten.isEmpty() || email.isEmpty() || matKhau.isEmpty() || loaiNguoiDungSelection == null) {
            if (ho.isEmpty()) highlightErrorField(hoField);
            if (ten.isEmpty()) highlightErrorField(tenField);
            if (email.isEmpty()) highlightErrorField(emailField);
            if (matKhau.isEmpty()) highlightErrorField(matKhauField);
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (!loaiNguoiDungSelection.contains(" - ")) {
                showAlert("Lỗi", "Dữ liệu loại người dùng không đúng định dạng!", Alert.AlertType.ERROR);
                return;
            }
            String loaiNguoiDung = loaiNguoiDungSelection.split(" - ")[0];
            int loaiNguoiDungId = Integer.parseInt(loaiNguoiDung);

            if (userService.isEmailExists(email)) {
                highlightErrorField(emailField);
                showAlert("Cảnh báo", "Email đã tồn tại!", Alert.AlertType.WARNING);
                return;
            }

            String avatarPath = DEFAULT_AVATAR; // Mặc định nếu không chọn ảnh
            if (selectedImageFile != null) {
                int nextImageNumber = getNextImageNumber();
                String avatarName = nextImageNumber + ".jpg";
                File destFile = new File(LOCAL_IMAGE_DIR + avatarName);
                Files.copy(selectedImageFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                avatarPath = "/com/ntn/images/avatars/" + avatarName; // Đường dẫn classpath
            }

            String hashedPassword = passwordService.hashPassword(matKhau);

            if (userService.registerUser(ho, ten, email, hashedPassword, loaiNguoiDungId, avatarPath, active)) {
                showAlert("Thành công", "Đã thêm người dùng!", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Lỗi", "Không thể thêm người dùng!", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Loại người dùng không hợp lệ!", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể thêm người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể lưu hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn avatar người dùng");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File newImageFile = fileChooser.showOpenDialog(hoField.getScene().getWindow());
        if (newImageFile != null) {
            selectedImageFile = newImageFile;
            avatarField.setText(selectedImageFile.getName());
            chooseImageButton.setText("Chọn Ảnh Khác");
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                if (imageStatusLabel != null) {
                    imageStatusLabel.setText("Ảnh đã chọn: " + selectedImageFile.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể hiển thị ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void clearImage() {
        selectedImageFile = null;
        avatarField.setText(DEFAULT_AVATAR); // Reset về ảnh mặc định
        imagePreview.setImage(new Image(getClass().getResource(DEFAULT_AVATAR).toString()));
        chooseImageButton.setText("Chọn Ảnh");
        if (imageStatusLabel != null) {
            imageStatusLabel.setText("Ảnh mặc định");
        }
    }

    private int getNextImageNumber() throws SQLException {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM nguoidung");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        }
        return 1;
    }

    private void closeWindow() {
        Stage stage = (Stage) hoField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void highlightErrorField(TextField field) {
        field.setStyle("-fx-border-color: red;");
    }

    private void clearFieldHighlight(TextField field) {
        field.setStyle("");
    }
}
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import com.ntn.quanlykhoahoc.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class EditUserController implements Initializable {
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

    private NguoiDung user;
    private UserService userService = new UserService();
    private File selectedImageFile;
    private static final String LOCAL_IMAGE_DIR = "src/main/resources/com/ntn/images/avatars/";
    private static final String DEFAULT_AVATAR = "/com/ntn/images/avatars/default.jpg";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            List<String> loaiNguoiDungList = userService.getLoaiNguoiDungList();
            loaiNguoiDungComboBox.getItems().addAll(loaiNguoiDungList);
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tải danh sách loại người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        chooseImageButton.setOnAction(e -> chooseImage());
        clearImageButton.setOnAction(e -> clearImage());
    }

    public void setUser(NguoiDung user) {
        this.user = user;
        hoField.setText(user.getHo());
        tenField.setText(user.getTen());
        emailField.setText(user.getEmail());
        matKhauField.setText(""); // Để trống để người dùng nhập nếu muốn đổi
        activeCheckBox.setSelected(user.isActive());
        avatarField.setText(user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : DEFAULT_AVATAR);
        try {
            String avatarPath = avatarField.getText();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                URL resource = getClass().getResource(avatarPath);
                if (resource != null) {
                    imagePreview.setImage(new Image(resource.toString()));
                } else {
                    imagePreview.setImage(new Image(getClass().getResource(DEFAULT_AVATAR).toString()));
                }
            } else {
                imagePreview.setImage(new Image(getClass().getResource(DEFAULT_AVATAR).toString()));
            }
        } catch (Exception e) {
            imagePreview.setImage(null);
        }

        try {
            List<String> loaiNguoiDungList = userService.getLoaiNguoiDungList();
            for (String loai : loaiNguoiDungList) {
                if (loai.contains(String.valueOf(user.getLoaiNguoiDungId()))) {
                    loaiNguoiDungComboBox.setValue(loai);
                    break;
                }
            }
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tải loại người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file avatar");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(hoField.getScene().getWindow());
        if (selectedImageFile != null) {
            avatarField.setText(selectedImageFile.getName());
            imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
            chooseImageButton.setText("Chọn Ảnh Khác");
        }
    }

    @FXML
    private void clearImage() {
        selectedImageFile = null;
        avatarField.setText(DEFAULT_AVATAR);
        imagePreview.setImage(new Image(getClass().getResource(DEFAULT_AVATAR).toString()));
        chooseImageButton.setText("Chọn Ảnh");
    }

    @FXML
    private void handleSave() {
        String ho = hoField.getText().trim();
        String ten = tenField.getText().trim();
        String email = emailField.getText().trim();
        String matKhau = matKhauField.getText().trim();
        String loaiNguoiDungSelection = loaiNguoiDungComboBox.getValue();
        boolean active = activeCheckBox.isSelected();

        if (ho.isEmpty() || ten.isEmpty() || email.isEmpty() || loaiNguoiDungSelection == null) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin bắt buộc!", Alert.AlertType.WARNING);
            return;
        }

        int loaiNguoiDungId;
        try {
            loaiNguoiDungId = Integer.parseInt(loaiNguoiDungSelection.split(" - ")[0]);
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Loại người dùng không hợp lệ!", Alert.AlertType.ERROR);
            return;
        }

        String avatarPath = user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : DEFAULT_AVATAR;
        if (selectedImageFile != null) {
            try {
                String avatarName = user.getId() + "_" + System.currentTimeMillis() + ".jpg"; // Đảm bảo tên file duy nhất
                File destFile = new File(LOCAL_IMAGE_DIR + avatarName);
                Files.copy(selectedImageFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                avatarPath = "/com/ntn/images/avatars/" + avatarName;
            } catch (IOException e) {
                showAlert("Lỗi", "Không thể lưu hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
        } else if (avatarField.getText().equals(DEFAULT_AVATAR)) {
            avatarPath = DEFAULT_AVATAR; // Giữ ảnh mặc định nếu không chọn ảnh mới
        }

        NguoiDung updatedUser = new NguoiDung(
            user.getId(), ho, ten, email, matKhau.isEmpty() ? user.getMatKhau() : matKhau,
            loaiNguoiDungId, active, avatarPath
        );

        try {
            boolean success = userService.updateUser(user, updatedUser);
            if (success) {
                showAlert("Thành công", "Đã cập nhật người dùng!", Alert.AlertType.INFORMATION);
                handleCancel();
            } else {
                showAlert("Lỗi", "Không thể cập nhật người dùng!", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            showAlert("Lỗi", "Lỗi hệ thống: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
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
}
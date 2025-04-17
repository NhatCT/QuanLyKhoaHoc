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
import java.util.logging.Logger;
import java.util.logging.Level;

public class EditUserController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(EditUserController.class.getName());

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
    private static final String AVATAR_BASE_PATH = "/com/ntn/images/avatars/";
    private static final String DEFAULT_AVATAR = AVATAR_BASE_PATH + "default.jpg";
    private static final String PLACEHOLDER_IMAGE = "https://via.placeholder.com/50";
    // Thư mục lưu ảnh trong môi trường phát triển
    private static final String DEV_UPLOAD_DIR = "src/main/resources/com/ntn/images/avatars/";
    // Thư mục lưu ảnh trong môi trường triển khai (bên ngoài JAR)
    private static final String DEPLOY_UPLOAD_DIR = "avatars/";
    // Đường dẫn lưu trong cơ sở dữ liệu
    private static final String AVATAR_STORAGE_PATH = "/com/ntn/images/avatars/";

    // Xác định thư mục lưu trữ dựa trên môi trường
    private static final String UPLOAD_DIR = isRunningFromJar() ? DEPLOY_UPLOAD_DIR : DEV_UPLOAD_DIR;

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

        // Tạo thư mục lưu trữ ảnh nếu chưa tồn tại
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                LOGGER.severe("Không thể tạo thư mục lưu trữ: " + UPLOAD_DIR);
            }
        }
    }

    public void setUser(NguoiDung user) {
        this.user = user;
        hoField.setText(user.getHo());
        tenField.setText(user.getTen());
        emailField.setText(user.getEmail());
        matKhauField.setText("");
        activeCheckBox.setSelected(user.isActive());
        avatarField.setText(user.getAvatar() != null && !user.getAvatar().isEmpty() ? user.getAvatar() : DEFAULT_AVATAR);
        imagePreview.setImage(loadImage(avatarField.getText(), DEFAULT_AVATAR).getImage());

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

    private ImageView loadImage(String imagePath, String defaultPath) {
        ImageView imageView = new ImageView();
        Image image = null;
        try {
            String effectivePath;
            if (imagePath == null || imagePath.trim().isEmpty()) {
                effectivePath = defaultPath;
            } else if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                effectivePath = imagePath;
            } else if (imagePath.startsWith("/com/ntn/images/")) {
                // Nếu đường dẫn trong classpath, thử tải từ tài nguyên
                URL resource = getClass().getResource(imagePath);
                if (resource != null) {
                    effectivePath = resource.toString();
                } else {
                    // Nếu không tìm thấy trong classpath, thử tải từ thư mục UPLOAD_DIR
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    File file = new File(UPLOAD_DIR + fileName);
                    if (file.exists()) {
                        effectivePath = file.toURI().toString();
                    } else {
                        effectivePath = defaultPath;
                    }
                }
            } else {
                // Giả định ảnh nằm trong thư mục UPLOAD_DIR
                File file = new File(UPLOAD_DIR + imagePath);
                if (file.exists()) {
                    effectivePath = file.toURI().toString();
                } else {
                    effectivePath = defaultPath;
                }
            }

            LOGGER.info("Đang thử tải ảnh: " + effectivePath);

            if (!effectivePath.equals(defaultPath) && 
                !effectivePath.toLowerCase().endsWith(".jpg") && 
                !effectivePath.toLowerCase().endsWith(".png") && 
                !effectivePath.toLowerCase().endsWith(".jpeg")) {
                LOGGER.warning("Định dạng ảnh không được hỗ trợ: " + effectivePath);
                image = loadDefaultImage(defaultPath);
            } else {
                image = new Image(effectivePath, 50, 50, true, true);
                if (image.isError()) {
                    LOGGER.warning("Không thể tải ảnh: " + effectivePath);
                    image = loadDefaultImage(defaultPath);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tải ảnh: " + (imagePath != null ? imagePath : "null"), e);
            image = loadDefaultImage(defaultPath);
        }
        imageView.setImage(image);
        return imageView;
    }

    private Image loadDefaultImage(String defaultPath) {
        try {
            Image image = new Image(defaultPath, 50, 50, true, true);
            if (!image.isError()) {
                return image;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi tải ảnh mặc định: " + defaultPath, e);
        }
        return new Image(PLACEHOLDER_IMAGE, 50, 50, true, true);
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file ảnh đại diện");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tệp ảnh", "*.png", "*.jpg", "*.jpeg")
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
        imagePreview.setImage(loadImage(null, DEFAULT_AVATAR).getImage());
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
                // Xóa ảnh đại diện cũ nếu tồn tại và không phải ảnh mặc định
                if (user.getAvatar() != null && !user.getAvatar().equals(DEFAULT_AVATAR) && user.getAvatar().startsWith(AVATAR_STORAGE_PATH)) {
                    try {
                        String fileName = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
                        File oldAvatarFile = new File(UPLOAD_DIR + fileName);
                        if (oldAvatarFile.exists()) {
                            boolean deleted = oldAvatarFile.delete();
                            if (deleted) {
                                LOGGER.info("Đã xóa ảnh đại diện cũ: " + oldAvatarFile.getAbsolutePath());
                            } else {
                                LOGGER.warning("Không thể xóa ảnh đại diện cũ: " + oldAvatarFile.getAbsolutePath());
                            }
                        } else {
                            LOGGER.info("Ảnh đại diện cũ không tồn tại: " + oldAvatarFile.getAbsolutePath());
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Lỗi khi xóa ảnh đại diện cũ: " + user.getAvatar(), e);
                    }
                }

                // Lưu ảnh mới vào thư mục UPLOAD_DIR
                String avatarName = user.getId() + "_" + System.currentTimeMillis() + getFileExtension(selectedImageFile);
                File destFile = new File(UPLOAD_DIR + avatarName);
                Files.copy(selectedImageFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                // Lưu đường dẫn trong classpath vào cơ sở dữ liệu
                avatarPath = AVATAR_STORAGE_PATH + avatarName;
                LOGGER.info("Đã lưu ảnh đại diện mới: " + avatarPath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi sao chép tệp ảnh: " + selectedImageFile.getAbsolutePath(), e);
                showAlert("Lỗi", "Không thể lưu hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
        } else if (avatarField.getText().equals(DEFAULT_AVATAR)) {
            // Nếu người dùng xóa ảnh, xóa ảnh đại diện cũ
            if (user.getAvatar() != null && !user.getAvatar().equals(DEFAULT_AVATAR) && user.getAvatar().startsWith(AVATAR_STORAGE_PATH)) {
                try {
                    String fileName = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
                    File oldAvatarFile = new File(UPLOAD_DIR + fileName);
                    if (oldAvatarFile.exists()) {
                        boolean deleted = oldAvatarFile.delete();
                        if (deleted) {
                            LOGGER.info("Đã xóa ảnh đại diện cũ (khi xóa): " + oldAvatarFile.getAbsolutePath());
                        } else {
                            LOGGER.warning("Không thể xóa ảnh đại diện cũ (khi xóa): " + oldAvatarFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Lỗi khi xóa ảnh đại diện cũ (khi xóa): " + user.getAvatar(), e);
                }
            }
            avatarPath = DEFAULT_AVATAR;
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

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ".jpg";
        }
        return name.substring(lastIndexOf);
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

    // Kiểm tra xem ứng dụng có đang chạy từ JAR không
    private static boolean isRunningFromJar() {
        String classPath = EditUserController.class.getProtectionDomain().getCodeSource().getLocation().toString();
        return classPath.endsWith(".jar");
    }
}
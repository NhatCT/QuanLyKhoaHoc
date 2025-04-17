package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import com.ntn.quanlykhoahoc.services.PasswordService;
import com.ntn.quanlykhoahoc.services.UserService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileController {

    @FXML private Label titleLabel;
    @FXML private ImageView avatarImage;
    @FXML private Label hoLabel;
    @FXML private Label tenLabel;
    @FXML private Label emailLabel;
    @FXML private Label loaiNguoiDungLabel;
    @FXML private Label activeLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private Button updatePasswordButton;
    @FXML private Button changeAvatarButton;
    @FXML private VBox infoBox;
    @FXML private HBox ngaySinhBox;
    @FXML private Label ngaySinhLabel;
    @FXML private HBox diaChiBox;
    @FXML private Label diaChiLabel;
    @FXML private HBox gioiTinhBox;
    @FXML private Label gioiTinhLabel;
    @FXML private HBox trinhDoBox;
    @FXML private Label trinhDoLabel;
    @FXML private HBox namKinhNghiemBox;
    @FXML private Label namKinhNghiemLabel;
    @FXML private HBox chuyenMonBox;
    @FXML private Label chuyenMonLabel;

    private static final Logger LOGGER = Logger.getLogger(ProfileController.class.getName());
    private final PasswordService passwordService = new PasswordService();
    private final UserService userService = new UserService();
    private static final String DEFAULT_AVATAR_IMAGE = "/com/ntn/images/avatars/default.jpg";
    private static final String AVATAR_STORAGE_PATH = "src/main/resources/com/ntn/images/avatars/";

    @FXML
    public void initialize() {
        loadProfile();
        updatePasswordButton.setOnAction(event -> handleUpdatePassword());
    }

    private void loadProfile() {
        String userEmail = SessionManager.getLoggedInEmail();
        try {
            List<NguoiDung> allUsers = userService.getAllUsers();
            NguoiDung user = allUsers.stream()
                    .filter(u -> u.getEmail().equals(userEmail))
                    .findFirst()
                    .orElse(null);

            if (user == null) {
                showAlert("Lỗi", "Không thể tải thông tin người dùng.", Alert.AlertType.ERROR);
                return;
            }

            hoLabel.setText(user.getHo());
            tenLabel.setText(user.getTen());
            emailLabel.setText(user.getEmail());
            loaiNguoiDungLabel.setText(getLoaiNguoiDung(user.getLoaiNguoiDungId()));
            activeLabel.setText(user.isActive() ? "Hoạt động" : "Không hoạt động");

            String loaiNguoiDung = getLoaiNguoiDung(user.getLoaiNguoiDungId());
            titleLabel.setText("Hồ Sơ " + loaiNguoiDung);

            if (user.getLoaiNguoiDungId() == 3) {
                loadHocVienInfo(user.getId());
            } else if (user.getLoaiNguoiDungId() == 2) {
                loadGiangVienInfo(user.getId());
            } 

            String avatarPath = (user.getAvatar() != null && !user.getAvatar().isEmpty())
                    ? user.getAvatar()
                    : DEFAULT_AVATAR_IMAGE;

            Image avatar;
            try {
                if (avatarPath.startsWith("/com/ntn/")) {
                    InputStream imageStream = getClass().getResourceAsStream(avatarPath);
                    if (imageStream != null) {
                        avatar = new Image(imageStream, 120, 120, true, true);
                    } else {
                        avatar = new Image(getClass().getResourceAsStream(DEFAULT_AVATAR_IMAGE), 120, 120, true, true);
                    }
                } else {
                    File file = new File(avatarPath);
                    if (file.exists()) {
                        avatar = new Image(file.toURI().toString(), 120, 120, true, true);
                    } else {
                        avatar = new Image(getClass().getResourceAsStream(DEFAULT_AVATAR_IMAGE), 120, 120, true, true);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Lỗi tải hình ảnh avatar", e);
                avatar = new Image(getClass().getResourceAsStream(DEFAULT_AVATAR_IMAGE), 120, 120, true, true);
            }
            avatarImage.setImage(avatar);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải thông tin người dùng", e);
            showAlert("Lỗi", "Không thể tải thông tin người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadHocVienInfo(int nguoiDungId) {
        try (Connection conn = Database.getConn()) {
            String query = "SELECT hv.ngay_sinh, hv.dia_chi, hv.gioi_tinh FROM hocvien hv WHERE hv.nguoiDungID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, nguoiDungId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ngaySinhLabel.setText(rs.getDate("ngay_sinh").toString());
                diaChiLabel.setText(rs.getString("dia_chi"));
                gioiTinhLabel.setText(rs.getString("gioi_tinh"));
                ngaySinhBox.setVisible(true);
                ngaySinhBox.setManaged(true);
                diaChiBox.setVisible(true);
                diaChiBox.setManaged(true);
                gioiTinhBox.setVisible(true);
                gioiTinhBox.setManaged(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải thông tin học viên", e);
            showAlert("Lỗi", "Không thể tải thông tin học viên: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadGiangVienInfo(int nguoiDungId) {
        try (Connection conn = Database.getConn()) {
            String query = "SELECT gv.trinh_do, gv.nam_kinh_nghiem, gv.linh_vuc_chuyen_mon FROM giangvien gv WHERE gv.id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, nguoiDungId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                trinhDoLabel.setText(rs.getString("trinh_do"));
                namKinhNghiemLabel.setText(String.valueOf(rs.getInt("nam_kinh_nghiem")));
                chuyenMonLabel.setText(rs.getString("linh_vuc_chuyen_mon"));
                trinhDoBox.setVisible(true);
                trinhDoBox.setManaged(true);
                namKinhNghiemBox.setVisible(true);
                namKinhNghiemBox.setManaged(true);
                chuyenMonBox.setVisible(true);
                chuyenMonBox.setManaged(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải thông tin giảng viên", e);
            showAlert("Lỗi", "Không thể tải thông tin giảng viên: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getLoaiNguoiDung(int loaiNguoiDungId) {
        try (Connection conn = Database.getConn()) {
            String query = "SELECT ten_loai FROM loainguoidung WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, loaiNguoiDungId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("ten_loai");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy loại người dùng", e);
        }
        return "Không xác định";
    }

    @FXML
    private void handleUpdatePassword() {
        String newPassword = newPasswordField.getText();
        if (newPassword.isEmpty()) {
            showAlert("Thông báo", "Không có mật khẩu mới được nhập. Mật khẩu giữ nguyên.", Alert.AlertType.INFORMATION);
            return;
        }

        String passwordValidation = passwordService.validatePassword(newPassword);
        if (passwordValidation != null) {
            showAlert("Lỗi", passwordValidation, Alert.AlertType.ERROR);
            return;
        }

        String userEmail = SessionManager.getLoggedInEmail();
        try {
            String hashedPassword = passwordService.hashPassword(newPassword);
            try (Connection conn = Database.getConn()) {
                String query = "UPDATE nguoidung SET mat_khau = ? WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, hashedPassword);
                stmt.setString(2, userEmail);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Thành công", "Mật khẩu đã được cập nhật thành công!", Alert.AlertType.INFORMATION);
                    newPasswordField.clear();
                } else {
                    showAlert("Lỗi", "Không thể cập nhật mật khẩu.", Alert.AlertType.ERROR);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật mật khẩu", e);
            showAlert("Lỗi", "Không thể cập nhật mật khẩu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String userEmail = SessionManager.getLoggedInEmail();
                int userId = Database.getUserIdByEmail(userEmail);
                if (userId == -1) {
                    showAlert("Lỗi", "Không thể xác định người dùng.", Alert.AlertType.ERROR);
                    return;
                }
                String newFileName = userId + "_" + System.currentTimeMillis() + "." + getFileExtension(selectedFile.getName());
                Path targetPath = Paths.get(AVATAR_STORAGE_PATH, newFileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                try (Connection conn = Database.getConn()) {
                    String query = "UPDATE nguoidung SET avatar = ? WHERE email = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, targetPath.toString());
                    stmt.setString(2, userEmail);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        Image newAvatar = new Image(selectedFile.toURI().toString(), 120, 120, true, true);
                        avatarImage.setImage(newAvatar);
                        showAlert("Thành công", "Avatar đã được cập nhật thành công!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Lỗi", "Không thể cập nhật avatar.", Alert.AlertType.ERROR);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật avatar", e);
                showAlert("Lỗi", "Không thể cập nhật avatar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) avatarImage.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex != -1 ? fileName.substring(lastIndex + 1) : "jpg";
    }
}
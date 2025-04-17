package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.services.CourseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AddCourseController {

    private static final Logger LOGGER = Logger.getLogger(AddCourseController.class.getName());
    private final CourseService courseService = new CourseService();
    private final Map<String, Integer> giangVienMap = new HashMap<>();
    private static final String COURSE_IMAGE_DIR = "src/main/resources/com/ntn/images/courses/";
    private static final String COURSE_IMAGE_PATH_PREFIX = "/com/ntn/images/courses/";

    @FXML
    private TextField tenKhoaHocField;

    @FXML
    private ComboBox<String> giangVienIdField;

    @FXML
    private TextArea moTaField;

    @FXML
    private DatePicker ngayBatDauField;

    @FXML
    private DatePicker ngayKetThucField;

    @FXML
    private TextField hocPhiField;

    @FXML
    private TextField hinhAnhField;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Button chooseImageButton;

    @FXML
    private Button clearImageButton;

    private File selectedImageFile;

    @FXML
    public void initialize() {
        loadGiangVienList();
        hinhAnhField.setText("Chưa chọn ảnh");
        imagePreview.setImage(null);

        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(COURSE_IMAGE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                LOGGER.info("Đã tạo thư mục: " + COURSE_IMAGE_DIR);
            } else {
                LOGGER.warning("Không thể tạo thư mục: " + COURSE_IMAGE_DIR);
            }
        }
    }

    private void loadGiangVienList() {
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, CONCAT(ho, ' ', ten) AS ten_giang_vien " +
                             "FROM nguoidung WHERE loai_nguoi_dung_id = 2");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String tenGiangVien = rs.getString("ten_giang_vien");
                giangVienMap.put(tenGiangVien, id);
                giangVienIdField.getItems().add(tenGiangVien);
            }
            giangVienIdField.getSelectionModel().selectFirst();
            LOGGER.info("Đã tải " + giangVienMap.size() + " giảng viên vào ComboBox.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách giảng viên", e);
            showAlert("Lỗi", "Không thể tải danh sách giảng viên: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Hình Ảnh Khóa Học");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            hinhAnhField.setText(file.getName());
            Image image = new Image(file.toURI().toString(), 160, 80, true, true);
            imagePreview.setImage(image);
            LOGGER.info("Đã chọn hình ảnh: " + file.getAbsolutePath());
        }
    }

    @FXML
    private void clearImage() {
        selectedImageFile = null;
        hinhAnhField.setText("Chưa chọn ảnh");
        imagePreview.setImage(null);
        LOGGER.info("Đã xóa hình ảnh khỏi lựa chọn.");
    }

    @FXML
    private void handleAdd() throws SQLException {
        String tenKhoaHoc = tenKhoaHocField.getText().trim();
        String selectedGiangVien = giangVienIdField.getValue();
        String moTa = moTaField.getText().trim();
        LocalDate ngayBatDau = ngayBatDauField.getValue();
        LocalDate ngayKetThuc = ngayKetThucField.getValue();
        String hocPhiText = hocPhiField.getText().trim();
        String hinhAnhPath = null;

        // Kiểm tra dữ liệu đầu vào
        if (tenKhoaHoc.isEmpty() || selectedGiangVien == null || moTa.isEmpty() || ngayBatDau == null || ngayKetThuc == null || hocPhiText.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        int giangVienId = giangVienMap.getOrDefault(selectedGiangVien, -1);
        if (giangVienId == -1) {
            showAlert("Cảnh báo", "Giảng viên không hợp lệ!", Alert.AlertType.WARNING);
            return;
        }

        double hocPhi;
        try {
            hocPhi = Double.parseDouble(hocPhiText);
            if (hocPhi < 0) {
                showAlert("Cảnh báo", "Học phí không thể âm!", Alert.AlertType.WARNING);
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Cảnh báo", "Học phí phải là một số hợp lệ!", Alert.AlertType.WARNING);
            return;
        }

        if (ngayKetThuc.isBefore(ngayBatDau)) {
            showAlert("Cảnh báo", "Ngày kết thúc phải sau ngày bắt đầu!", Alert.AlertType.WARNING);
            return;
        }

        // Xử lý hình ảnh
        if (selectedImageFile != null) {
            try {
                int nextImageNumber = courseService.getNextImageNumber();
                String fileExtension = getFileExtension(selectedImageFile.getName());
                String newFileName = "course_" + nextImageNumber + fileExtension;
                Path targetPath = Paths.get(COURSE_IMAGE_DIR, newFileName);
                Files.copy(selectedImageFile.toPath(), targetPath);
                hinhAnhPath = COURSE_IMAGE_PATH_PREFIX + newFileName;
                LOGGER.info("Đã sao chép hình ảnh đến: " + targetPath.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi sao chép hình ảnh", e);
                showAlert("Lỗi", "Không thể sao chép hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lấy số thứ tự hình ảnh", e);
                showAlert("Lỗi", "Không thể lấy số thứ tự hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
        }

        // Thêm khóa học bằng CourseService
        try {
            boolean success = courseService.addCourseWithImage(
                    tenKhoaHoc, giangVienId, moTa, ngayBatDau, ngayKetThuc, hocPhi, hinhAnhPath, true
            );
            if (success) {
                LOGGER.info("Đã thêm khóa học: " + tenKhoaHoc + " với hình ảnh: " + hinhAnhPath);
                showAlert("Thành công", "Khóa học đã được thêm!", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Lỗi", "Không thể thêm khóa học!", Alert.AlertType.ERROR);
            }
        } catch (IllegalArgumentException e) {
            showAlert("Cảnh báo", e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleCancel() {
        LOGGER.info("Hủy thêm khóa học.");
        closeWindow();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) tenKhoaHocField.getScene().getWindow();
        stage.close();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return ".jpg"; 
    }
}
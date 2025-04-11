package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

public class EditCourseController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(EditCourseController.class.getName());
    private final CourseService courseService = new CourseService();
    private final Map<String, Integer> giangVienMap = new HashMap<>();
    private static final String COURSE_IMAGE_DIR = "src/main/resources/com/ntn/images/courses/";
    private static final String COURSE_IMAGE_PATH_PREFIX = "/com/ntn/images/courses/";

    @FXML private TextField idField;
    @FXML private TextField tenKhoaHocField;
    @FXML private TextArea moTaField;
    @FXML private TextField giaField;
    @FXML private TextField hinhAnhField;
    @FXML private ImageView imagePreview;
    @FXML private ComboBox<String> giangVienComboBox;
    @FXML private DatePicker ngayBatDauField;
    @FXML private DatePicker ngayKetThucField;
    @FXML private CheckBox activeCheckBox;
    @FXML private Button chooseImageButton;
    @FXML private Button clearImageButton;

    private KhoaHoc course;
    private File selectedImageFile;
    private String originalImagePath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadGiangVienList();

        // Đảm bảo thư mục lưu ảnh tồn tại
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
        try (java.sql.Connection conn = com.ntn.quanlykhoahoc.database.Database.getConn();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, CONCAT(ho, ' ', ten) AS ten_giang_vien " +
                             "FROM nguoidung WHERE loai_nguoi_dung_id = 2");
             java.sql.ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String tenGiangVien = rs.getString("ten_giang_vien");
                giangVienMap.put(tenGiangVien, id);
                giangVienComboBox.getItems().add(tenGiangVien);
            }
            LOGGER.info("Đã tải " + giangVienMap.size() + " giảng viên vào ComboBox.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách giảng viên", e);
            showAlert("Lỗi", "Không thể tải danh sách giảng viên: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setCourse(KhoaHoc course) {
        this.course = course;
        idField.setText(String.valueOf(course.getId()));
        tenKhoaHocField.setText(course.getTenKhoaHoc());
        moTaField.setText(course.getMoTa());
        giaField.setText(String.valueOf(course.getGia()));
        hinhAnhField.setText(course.getHinhAnh() != null ? course.getHinhAnh() : "Chưa chọn ảnh");
        originalImagePath = course.getHinhAnh();
        activeCheckBox.setSelected(course.isActive());
        ngayBatDauField.setValue(course.getNgayBatDau());
        ngayKetThucField.setValue(course.getNgayKetThuc());

        // Tải ảnh ban đầu
        if (course.getHinhAnh() != null && !course.getHinhAnh().isEmpty()) {
            try {
                Image image = new Image(getClass().getResource(course.getHinhAnh()).toExternalForm(), 160, 80, true, true);
                imagePreview.setImage(image);
            } catch (Exception e) {
                LOGGER.warning("Không thể tải hình ảnh: " + course.getHinhAnh());
                imagePreview.setImage(null);
            }
        } else {
            imagePreview.setImage(null);
        }

        giangVienComboBox.setValue(course.getTenGiangVien());
    }

    @FXML
    private void handleSave() {
        String tenKhoaHoc = tenKhoaHocField.getText().trim();
        String selectedGiangVien = giangVienComboBox.getValue();
        String moTa = moTaField.getText().trim();
        String giaText = giaField.getText().trim();
        LocalDate ngayBatDau = ngayBatDauField.getValue();
        LocalDate ngayKetThuc = ngayKetThucField.getValue();
        boolean active = activeCheckBox.isSelected();
        String hinhAnhPath = originalImagePath;

        // Kiểm tra đầu vào
        if (tenKhoaHoc.isEmpty() || selectedGiangVien == null || moTa.isEmpty() || giaText.isEmpty() || ngayBatDau == null || ngayKetThuc == null) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        int giangVienId = giangVienMap.getOrDefault(selectedGiangVien, -1);
        if (giangVienId == -1) {
            showAlert("Cảnh báo", "Giảng viên không hợp lệ!", Alert.AlertType.WARNING);
            return;
        }

        double gia;
        try {
            gia = Double.parseDouble(giaText);
            if (gia < 0) {
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
                // Xóa ảnh cũ nếu tồn tại
                if (originalImagePath != null && !originalImagePath.isEmpty()) {
                    String oldImageFileName = originalImagePath.substring(COURSE_IMAGE_PATH_PREFIX.length());
                    Path oldImagePath = Paths.get(COURSE_IMAGE_DIR, oldImageFileName);
                    if (Files.exists(oldImagePath)) {
                        Files.delete(oldImagePath);
                        LOGGER.info("Đã xóa ảnh cũ: " + oldImagePath.toAbsolutePath());
                    }
                }

                // Sao chép ảnh mới
                int nextImageNumber = courseService.getNextImageNumber();
                String fileExtension = getFileExtension(selectedImageFile.getName());
                String newFileName = "course_" + nextImageNumber + fileExtension;
                Path targetPath = Paths.get(COURSE_IMAGE_DIR, newFileName);
                Files.copy(selectedImageFile.toPath(), targetPath);
                hinhAnhPath = COURSE_IMAGE_PATH_PREFIX + newFileName;
                LOGGER.info("Đã sao chép ảnh mới đến: " + targetPath.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi xử lý hình ảnh", e);
                showAlert("Lỗi", "Không thể xử lý hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lấy số thứ tự hình ảnh", e);
                showAlert("Lỗi", "Không thể lấy số thứ tự hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }
        } else if (hinhAnhField.getText().equals("Chưa chọn ảnh")) {
            // Xóa ảnh cũ nếu người dùng xóa ảnh
            if (originalImagePath != null && !originalImagePath.isEmpty()) {
                try {
                    String oldImageFileName = originalImagePath.substring(COURSE_IMAGE_PATH_PREFIX.length());
                    Path oldImagePath = Paths.get(COURSE_IMAGE_DIR, oldImageFileName);
                    if (Files.exists(oldImagePath)) {
                        Files.delete(oldImagePath);
                        LOGGER.info("Đã xóa ảnh cũ: " + oldImagePath.toAbsolutePath());
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi xóa ảnh cũ", e);
                    showAlert("Lỗi", "Không thể xóa ảnh cũ: " + e.getMessage(), Alert.AlertType.ERROR);
                    return;
                }
            }
            hinhAnhPath = null; // Dùng ảnh mặc định trong CourseService
        }

        // Cập nhật khóa học
        try {
            boolean success = courseService.updateCourse(
                    course.getId(), tenKhoaHoc, giangVienId, moTa, ngayBatDau, ngayKetThuc, gia, hinhAnhPath, active
            );
            if (success) {
                LOGGER.info("Đã cập nhật khóa học ID: " + course.getId());
                showAlert("Thành công", "Đã cập nhật khóa học!", Alert.AlertType.INFORMATION);
                handleCancel();
            } else {
                showAlert("Lỗi", "Không có bản ghi nào được cập nhật. Hãy kiểm tra ID khóa học.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật khóa học", e);
            showAlert("Lỗi", "Lỗi hệ thống: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) tenKhoaHocField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn hình ảnh khóa học");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File newImageFile = fileChooser.showOpenDialog(tenKhoaHocField.getScene().getWindow());
        if (newImageFile != null) {
            selectedImageFile = newImageFile;
            hinhAnhField.setText(selectedImageFile.getName());
            chooseImageButton.setText("Chọn Ảnh Khác");
            Image image = new Image(selectedImageFile.toURI().toString(), 160, 80, true, true);
            imagePreview.setImage(image);
            LOGGER.info("Đã chọn hình ảnh: " + newImageFile.getAbsolutePath());
        }
    }

    @FXML
    private void clearImage() {
        selectedImageFile = null;
        hinhAnhField.setText("Chưa chọn ảnh");
        chooseImageButton.setText("Chọn Ảnh");
        imagePreview.setImage(null);
        LOGGER.info("Đã xóa hình ảnh khỏi lựa chọn.");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return ".jpg"; // Mặc định là .jpg nếu không tìm thấy phần mở rộng
    }
}
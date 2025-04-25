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
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javafx.util.StringConverter;

public class AddCourseController {

    private static final Logger LOGGER = Logger.getLogger(AddCourseController.class.getName());
    private final CourseService courseService = new CourseService();
    private final Map<String, Integer> giangVienMap = new HashMap<>();
    private static final String COURSE_IMAGE_DIR = "src/main/resources/com/ntn/images/courses/";
    private static final String COURSE_IMAGE_PATH_PREFIX = "/com/ntn/images/courses/";
    // Regex chấp nhận chữ, số, dấu cách, gạch ngang, gạch dưới, dấu chấm, và ký tự tiếng Việt
    private static final Pattern COURSE_NAME_PATTERN = Pattern.compile("^[\\p{L}0-9\\s-_.]+$");
    // Định dạng ảnh hợp lệ
    private static final String[] VALID_IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg"};
    // Định dạng ngày
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

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
                showAlert("Lỗi", "Không thể tạo thư mục lưu ảnh: " + COURSE_IMAGE_DIR, Alert.AlertType.ERROR);
            }
        }

        // Tùy chỉnh DatePicker để ngăn chọn ngày không hợp lệ
        ngayBatDauField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        ngayKetThucField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = ngayBatDauField.getValue();
                setDisable(empty || (startDate != null && date.isBefore(startDate)));
            }
        });

        // Ngăn nhập ngày không hợp lệ qua bàn phím và hiển thị thông báo
        ngayBatDauField.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}(/\\d{0,2}(/\\d{0,4})?)?")) {
                ngayBatDauField.getEditor().setText(oldValue); // Chỉ revert nếu định dạng sai
            } else if (newValue.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                try {
                    LocalDate.parse(newValue, DATE_FORMATTER);
                    // Ngày hợp lệ, không làm gì
                } catch (DateTimeParseException e) {
                    showAlert("Cảnh báo", "Ngày bắt đầu không hợp lệ (ví dụ: 31/6 không tồn tại)! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                    // Giữ nguyên newValue để người dùng sửa
                }
            }
        });

        ngayKetThucField.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,2}(/\\d{0,2}(/\\d{0,4})?)?")) {
                ngayKetThucField.getEditor().setText(oldValue); // Chỉ revert nếu định dạng sai
            } else if (newValue.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                try {
                    LocalDate.parse(newValue, DATE_FORMATTER);
                    // Ngày hợp lệ, không làm gì
                } catch (DateTimeParseException e) {
                    showAlert("Cảnh báo", "Ngày kết thúc không hợp lệ (ví dụ: 31/6 không tồn tại)! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                    // Giữ nguyên newValue để người dùng sửa
                }
            }
        });

        // Tắt auto-parse của DatePicker để ngăn tự động chuyển ngày
        ngayBatDauField.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string, DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return null; // Không parse nếu ngày không hợp lệ
                }
            }
        });

        ngayKetThucField.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(string, DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return null; // Không parse nếu ngày không hợp lệ
                }
            }
        });
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
            // Kiểm tra định dạng ảnh ngay khi chọn
            if (!isValidImageExtension(file.getName())) {
                showAlert("Cảnh báo", "Chỉ chấp nhận file ảnh định dạng .png, .jpg, hoặc .jpeg!", Alert.AlertType.WARNING);
                selectedImageFile = null;
                hinhAnhField.setText("Chưa chọn ảnh");
                imagePreview.setImage(null);
                return;
            }
            selectedImageFile = file;
            hinhAnhField.setText(file.getName());
            try {
                Image image = new Image(file.toURI().toString(), 160, 80, true, true);
                imagePreview.setImage(image);
                LOGGER.info("Đã chọn hình ảnh: " + file.getAbsolutePath());
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Lỗi khi tải ảnh xem trước: " + file.getAbsolutePath(), e);
                showAlert("Cảnh báo", "Không thể tải ảnh xem trước: " + e.getMessage(), Alert.AlertType.WARNING);
                selectedImageFile = null;
                hinhAnhField.setText("Chưa chọn ảnh");
                imagePreview.setImage(null);
            }
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
    private void handleAdd() {
        String tenKhoaHoc = tenKhoaHocField.getText().trim();
        String selectedGiangVien = giangVienIdField.getValue();
        String moTa = moTaField.getText().trim();
        LocalDate ngayBatDau = null;
        LocalDate ngayKetThuc = null;
        String hocPhiText = hocPhiField.getText().trim();
        String hinhAnhPath = null;

        // Kiểm tra dữ liệu đầu vào cơ bản
        if (tenKhoaHoc.isEmpty() || selectedGiangVien == null || moTa.isEmpty() || hocPhiText.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin!", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra tên khóa học
        if (!COURSE_NAME_PATTERN.matcher(tenKhoaHoc).matches()) {
            showAlert("Cảnh báo", "Tên khóa học chỉ được chứa chữ cái, số, dấu cách, gạch ngang, gạch dưới, hoặc dấu chấm!", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra giảng viên
        int giangVienId = giangVienMap.getOrDefault(selectedGiangVien, -1);
        if (giangVienId == -1) {
            showAlert("Cảnh báo", "Giảng viên không hợp lệ!", Alert.AlertType.WARNING);
            return;
        }

        // Kiểm tra học phí
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

        // Kiểm tra ngày hợp lệ
        try {
            // Lấy text từ DatePicker để kiểm tra
            String startDateText = ngayBatDauField.getEditor().getText().trim();
            String endDateText = ngayKetThucField.getEditor().getText().trim();

            if (startDateText.isEmpty() || endDateText.isEmpty()) {
                showAlert("Cảnh báo", "Vui lòng nhập ngày bắt đầu và ngày kết thúc!", Alert.AlertType.WARNING);
                return;
            }

            // Parse ngày bắt đầu
            try {
                ngayBatDau = LocalDate.parse(startDateText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                showAlert("Cảnh báo", "Ngày bắt đầu không hợp lệ (ví dụ: 31/6 không tồn tại)! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                return;
            }

            // Parse ngày kết thúc
            try {
                ngayKetThuc = LocalDate.parse(endDateText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                showAlert("Cảnh báo", "Ngày kết thúc không hợp lệ (ví dụ: 31/6 không tồn tại)! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                return;
            }

            LocalDate today = LocalDate.now();
            if (ngayBatDau.isBefore(today)) {
                showAlert("Cảnh báo", "Ngày bắt đầu không được là ngày trong quá khứ! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                return;
            }
            if (ngayKetThuc.isBefore(ngayBatDau)) {
                showAlert("Cảnh báo", "Ngày kết thúc phải sau ngày bắt đầu! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                return;
            }
            if (ngayBatDau.isAfter(today.plusYears(10)) || ngayKetThuc.isAfter(today.plusYears(10))) {
                showAlert("Cảnh báo", "Ngày bắt đầu hoặc kết thúc không được quá 10 năm từ hiện tại! Vui lòng nhập lại.", Alert.AlertType.WARNING);
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi không xác định khi kiểm tra ngày", e);
            showAlert("Lỗi", "Lỗi khi xử lý ngày: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        // Xử lý hình ảnh
        if (selectedImageFile != null) {
            if (!isValidImageExtension(selectedImageFile.getName())) {
                showAlert("Cảnh báo", "File đã chọn không phải ảnh hợp lệ! Chỉ chấp nhận .png, .jpg, hoặc .jpeg.", Alert.AlertType.WARNING);
                return;
            }
            try {
                // Kiểm tra file tồn tại
                if (!selectedImageFile.exists()) {
                    showAlert("Cảnh báo", "File ảnh không tồn tại!", Alert.AlertType.WARNING);
                    return;
                }

                // Tạo tên file duy nhất bằng UUID
                String fileExtension = getFileExtension(selectedImageFile.getName());
                String newFileName = "course_" + UUID.randomUUID().toString() + fileExtension;
                Path targetPath = Paths.get(COURSE_IMAGE_DIR, newFileName);

                // Sao chép file, ghi đè nếu cần
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                hinhAnhPath = COURSE_IMAGE_PATH_PREFIX + newFileName;
                LOGGER.info("Đã sao chép hình ảnh đến: " + targetPath.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi sao chép hình ảnh", e);
                showAlert("Lỗi", "Không thể sao chép hình ảnh: " + e.getMessage(), Alert.AlertType.ERROR);
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi thêm khóa học", e);
            showAlert("Lỗi", "Không thể thêm khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
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
            return fileName.substring(lastDotIndex).toLowerCase();
        }
        return ".jpg"; // Mặc định trả về .jpg nếu không tìm thấy
    }

    private boolean isValidImageExtension(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        for (String validExt : VALID_IMAGE_EXTENSIONS) {
            if (extension.equals(validExt)) {
                return true;
            }
        }
        return false;
    }
}
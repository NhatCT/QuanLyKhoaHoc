package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.pojo.LichHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.TimetableService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CourseDetailsController {

    @FXML private ImageView courseImage;
    @FXML private Label courseNameLabel;
    @FXML private Label instructorLabel;
    @FXML private Label priceLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label maxStudentsLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private TableView<LichHoc> scheduleTable;
    @FXML private TableColumn<LichHoc, String> tenKhoaHocColumn;
    @FXML private TableColumn<LichHoc, LocalDate> ngayHocColumn;
    @FXML private TableColumn<LichHoc, LocalTime> gioBatDauColumn;
    @FXML private TableColumn<LichHoc, LocalTime> gioKetThucColumn;
    @FXML private TableColumn<LichHoc, String> giangVienColumn;
    @FXML private TableColumn<LichHoc, String> lienKetColumn;
    @FXML private TableColumn<LichHoc, String> statusColumn;
    @FXML private Button addToCartButton;
    @FXML private Button viewScheduleButton;
    @FXML private Button closeButton;

    private static final Logger LOGGER = Logger.getLogger(CourseDetailsController.class.getName());
    private KhoaHoc course;
    private final CourseService courseService = new CourseService();
    private final TimetableService timetableService = new TimetableService();
    private static final String IMAGE_PATH = "/com/ntn/images/courses/";
    private static final String DEFAULT_COURSE_IMAGE = IMAGE_PATH + "default.jpg";
    private static final String PLACEHOLDER_IMAGE = "https://via.placeholder.com/270x150";
    private static final String DEV_UPLOAD_DIR = "src/main/resources/com/ntn/images/courses/";
    private static final String DEPLOY_UPLOAD_DIR = "courses/";
    private static final String UPLOAD_DIR = isRunningFromJar() ? DEPLOY_UPLOAD_DIR : DEV_UPLOAD_DIR;
    private DashboardStudentController dashboardController;
    private HostServices hostServices;

    public void setCourse(KhoaHoc course) {
        this.course = course;
        loadCourseDetails();
    }

    public void setDashboardController(DashboardStudentController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private void loadCourseDetails() {
        courseNameLabel.textProperty().bind(course.tenKhoaHocProperty());
        instructorLabel.setText("Giảng viên: " + course.getTenGiangVien());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        priceLabel.setText( currencyFormat.format(course.getGia()));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        startDateLabel.setText(course.getNgayBatDau() != null ? course.getNgayBatDau().format(dateFormatter) : "N/A");
        endDateLabel.setText(course.getNgayKetThuc() != null ? course.getNgayKetThuc().format(dateFormatter) : "N/A");
        maxStudentsLabel.setText(String.valueOf(course.getSoLuongHocVienToiDa()));
        descriptionTextArea.setText(course.getMoTa() != null ? course.getMoTa() : "Chưa có mô tả.");

        // Load course image (copied from DashboardStudentController.loadCourseImage)
        loadCourseImage(courseImage, course.getHinhAnh());

        loadCourseSchedule();
    }

    private void loadCourseImage(ImageView imageView, String hinhAnh) {
        try {
            String imagePath = hinhAnh == null || hinhAnh.trim().isEmpty()
                    ? DEFAULT_COURSE_IMAGE
                    : IMAGE_PATH + hinhAnh.replaceAll("\\\\", "/").trim();
            LOGGER.info("Attempting to load image from path: " + imagePath);

            // Validate file extension
            if (!imagePath.matches(".*\\.(png|jpg|jpeg|gif)$")) {
                LOGGER.warning("Invalid image extension for: " + imagePath + ", using default image");
                imagePath = DEFAULT_COURSE_IMAGE;
            }

            Image image;
            try (InputStream imageStream = getClass().getResourceAsStream(imagePath)) {
                if (imageStream != null) {
                    image = new Image(imageStream, 270, 150, true, true);
                    LOGGER.info("Successfully loaded image from resources: " + imagePath);
                } else {
                    LOGGER.warning("Resource not found: " + imagePath);
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    File file = new File(UPLOAD_DIR + fileName);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString(), 270, 150, true, true);
                        LOGGER.info("Successfully loaded image from local file: " + file.getAbsolutePath());
                    } else {
                        LOGGER.warning("Local file not found: " + file.getAbsolutePath());
                        try (InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_COURSE_IMAGE)) {
                            image = defaultStream != null
                                    ? new Image(defaultStream, 270, 150, true, true)
                                    : new Image(PLACEHOLDER_IMAGE, 270, 150, true, true);
                            LOGGER.info(defaultStream != null
                                    ? "Successfully loaded default image: " + DEFAULT_COURSE_IMAGE
                                    : "Using placeholder image: " + PLACEHOLDER_IMAGE);
                        }
                    }
                }
            }
            imageView.setImage(image);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading image: " + hinhAnh, e);
            imageView.setImage(new Image(PLACEHOLDER_IMAGE, 270, 150, true, true));
            LOGGER.info("Using placeholder image due to error: " + PLACEHOLDER_IMAGE);
        }
    }

    private void loadCourseSchedule() {
        try {
            tenKhoaHocColumn.setCellValueFactory(cellData -> cellData.getValue().tenKhoaHocProperty());
            ngayHocColumn.setCellValueFactory(cellData -> cellData.getValue().ngayHocProperty());
            gioBatDauColumn.setCellValueFactory(cellData -> cellData.getValue().gioBatDauProperty());
            gioKetThucColumn.setCellValueFactory(cellData -> cellData.getValue().gioKetThucProperty());
            giangVienColumn.setCellValueFactory(cellData -> cellData.getValue().giangVienProperty());
            lienKetColumn.setCellValueFactory(cellData -> cellData.getValue().lienKetProperty());
            statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

            statusColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        switch (item) {
                            case "Sắp tới":
                                setStyle("-fx-text-fill: green;");
                                break;
                            case "Đang diễn ra":
                                setStyle("-fx-text-fill: blue;");
                                break;
                            case "Đã kết thúc":
                                setStyle("-fx-text-fill: gray;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });

            lienKetColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.trim().isEmpty()) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Hyperlink hyperlink = new Hyperlink(item);
                        hyperlink.setOnAction(e -> {
                            if (hostServices != null) {
                                hostServices.showDocument(item);
                            } else {
                                showAlert("Lỗi", "Không thể mở liên kết: HostServices chưa được khởi tạo.", Alert.AlertType.ERROR);
                            }
                        });
                        setGraphic(hyperlink);
                    }
                }
            });

            List<LichHoc> schedule = timetableService.getScheduleForCourse(course.getId());
            scheduleTable.setItems(FXCollections.observableArrayList(schedule));
            if (schedule.isEmpty()) {
                scheduleTable.setPlaceholder(new Label("Chưa có lịch học cho khóa này."));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải lịch học cho khóa học ID: " + course.getId(), e);
            scheduleTable.setPlaceholder(new Label("Lỗi khi tải lịch học: " + e.getMessage()));
        }
    }

    @FXML
    private void addToCart(ActionEvent event) {
        int userId = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
        if (userId == -1) {
            showAlert("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Alert.AlertType.ERROR);
            return;
        }
        try {
            List<KhoaHoc> enrolledCourses = courseService.getEnrolledCourses(userId);
            if (!canEnrollCourse(userId, enrolledCourses)) {
                return;
            }
            if (dashboardController != null && !dashboardController.getCartCourses().contains(course)) {
                dashboardController.getCartCourses().add(course);
                showAlert("Thành công", "Đã thêm khóa học " + course.getTenKhoaHoc() + " vào giỏ hàng!", Alert.AlertType.INFORMATION);
                close(event);
            } else {
                showAlert("Thông báo", "Khóa học " + course.getTenKhoaHoc() + " đã có trong giỏ hàng!", Alert.AlertType.WARNING);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi kiểm tra trạng thái khóa học", e);
            showAlert("Lỗi", "Không thể kiểm tra trạng thái khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void viewSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/timetable.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lịch học chi tiết - " + course.getTenKhoaHoc());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            TimetableController timetableController = loader.getController();
            List<LichHoc> schedule = timetableService.getScheduleForCourse(course.getId());
            timetableController.setTimetableData(schedule);
            timetableController.setHostServices(hostServices);

            stage.showAndWait();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi mở giao diện thời khóa biểu", e);
            showAlert("Lỗi", "Không thể mở lịch học chi tiết: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy lịch học cho khóa học ID: " + course.getId(), e);
            showAlert("Lỗi", "Không thể lấy lịch học: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean canEnrollCourse(int userId, List<KhoaHoc> enrolledCourses) throws SQLException {
        if (courseService.isCourseEnrolled(userId, course.getId())) {
            showAlert("Cảnh báo", "Bạn đã đăng ký khóa học này rồi!", Alert.AlertType.WARNING);
            return false;
        }

        int currentCount = courseService.getCurrentEnrollmentCount(course.getId());
        if (currentCount >= course.getSoLuongHocVienToiDa()) {
            showAlert("Cảnh báo", "Khóa học đã đủ số lượng học viên tối đa!", Alert.AlertType.WARNING);
            return false;
        }

        if (course.getNgayBatDau() != null) {
            long hoursUntilStart = java.time.Duration.between(LocalDateTime.now(), course.getNgayBatDau().atStartOfDay()).toHours();
            if (hoursUntilStart < 48) {
                showAlert("Cảnh báo", "Không thể đăng ký: Khóa học bắt đầu trong vòng 48 giờ!", Alert.AlertType.WARNING);
                return false;
            }
        }

        for (KhoaHoc enrolled : enrolledCourses) {
            LocalDate start1 = course.getNgayBatDau();
            LocalDate end1 = course.getNgayKetThuc();
            LocalDate start2 = enrolled.getNgayBatDau();
            LocalDate end2 = enrolled.getNgayKetThuc();
            if (start1 == null || end1 == null || start2 == null || end2 == null) {
                continue;
            }
            if (!(end1.isBefore(start2) || start1.isAfter(end2))) {
                showAlert("Cảnh báo", "Khóa học này xung đột với lịch học của khóa " + enrolled.getTenKhoaHoc() + "!", Alert.AlertType.WARNING);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private static boolean isRunningFromJar() {
        return CourseDetailsController.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar");
    }
}
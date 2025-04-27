package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import com.ntn.quanlykhoahoc.pojo.LichHoc;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.PaymentService;
import com.ntn.quanlykhoahoc.services.TimetableService;
import com.ntn.quanlykhoahoc.services.UserService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.collections.ListChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashboardStudentController {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private FlowPane courseFlowPane;
    @FXML
    private ScrollPane coursesScrollPane;
    @FXML
    private Button prevPageBtn, nextPageBtn, payButton, removeButton;
    @FXML
    private Label pageLabel;
    @FXML
    private TableView<KhoaHoc> cartTable;
    @FXML
    private TableColumn<KhoaHoc, String> courseColumn;
    @FXML
    private TableColumn<KhoaHoc, String> instructorColumn;
    @FXML
    private TableColumn<KhoaHoc, Double> priceColumn;
    @FXML
    private TableColumn<KhoaHoc, String> imageColumn;
    @FXML
    private Button dashboardBtn, coursesBtn, timetableBtn, profileBtn, logoutButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button clearFilterButton;
    @FXML
    private Label resultsLabel;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private VBox subjectFilterBox;
    @FXML
    private Button showMoreSubjectsButton;
    @FXML
    private Label totalPriceLabel;

    private static final Logger LOGGER = Logger.getLogger(DashboardStudentController.class.getName());
    private static final int ITEMS_PER_PAGE = 12;
    private static final String IMAGE_PATH = "/com/ntn/images/courses/";
    private static final String DEFAULT_COURSE_IMAGE = IMAGE_PATH + "default_course.jpg";
    private static final String DEFAULT_AVATAR_IMAGE = "/com/ntn/images/users/default_avatar.png";
    private static final String PLACEHOLDER_IMAGE = "https://via.placeholder.com/270x150";
    private static final String DEV_UPLOAD_DIR = "src/main/resources/com/ntn/images/courses/";
    private static final String DEPLOY_UPLOAD_DIR = "courses/";
    private static final String UPLOAD_DIR = isRunningFromJar() ? DEPLOY_UPLOAD_DIR : DEV_UPLOAD_DIR;

    private final CourseService courseService = new CourseService();
    private final UserService userService = new UserService();
    private final PaymentService paymentService = new PaymentService();
    private final TimetableService timetableService = new TimetableService();
    private final ObservableList<KhoaHoc> cartCourses = FXCollections.observableArrayList();
    private List<KhoaHoc> khoaHocList = new ArrayList<>();
    private ObservableList<KhoaHoc> filteredCourses = FXCollections.observableArrayList();
    private Map<String, CheckBox> subjectCheckBoxes = new HashMap<>();
    private boolean subjectsExpanded = false;
    private boolean isMyCoursesView = false;
    private int currentPage = 1;
    private boolean isGoToMyCourse = true;

    @FXML
    public void initialize() {
        setupCartTable();
        setupSortComboBox();
        loadUserAvatar();
        loadCoursesAsync();
        checkUpcomingSessions();
        checkNotifications();

        // Bind event handlers
        prevPageBtn.setOnAction(e -> changePage(-1));
        nextPageBtn.setOnAction(e -> changePage(1));
        removeButton.setOnAction(e -> handleRemove());
        searchButton.setOnAction(e -> handleSearch());
        payButton.setOnAction(e -> handlePayButton());
        dashboardBtn.setOnAction(e -> loadDashboard());
        coursesBtn.setOnAction(e -> loadMyCourses());
        timetableBtn.setOnAction(e -> loadTimetable());
        profileBtn.setOnAction(e -> loadProfile());

        cartCourses.addListener((ListChangeListener<KhoaHoc>) change -> updateTotalPrice());
        updateTotalPrice();
    }

    private void checkNotifications() {
        int userId = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
        if (userId == -1) {
            LOGGER.warning("Cannot determine userId for notifications.");
            return;
        }
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT noi_dung FROM thongbao WHERE nguoi_nhan_id = ? AND trang_thai = 'UNREAD'")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String noiDung = rs.getString("noi_dung");
                    showAlert("Thông báo", noiDung, Alert.AlertType.INFORMATION);
                }
            }
            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE thongbao SET trang_thai = 'READ' WHERE nguoi_nhan_id = ? AND trang_thai = 'UNREAD'")) {
                updateStmt.setInt(1, userId);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking notifications", e);
            showAlert("Lỗi", "Không thể kiểm tra thông báo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void checkUpcomingSessions() {
        try {
            String email = SessionManager.getLoggedInEmail();
            List<LichHoc> timetable = timetableService.getTimetableForStudent(email);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime soon = now.plusHours(24);

            for (LichHoc lichHoc : timetable) {
                LocalDateTime sessionTime = LocalDateTime.of(lichHoc.getNgayHoc(), lichHoc.getGioBatDau());
                if (sessionTime.isAfter(now) && sessionTime.isBefore(soon)) {
                    String thu = getVietnameseDayOfWeek(lichHoc.getNgayHoc());
                    showAlert("Nhắc nhở", "Bạn có buổi học '" + lichHoc.getTenKhoaHoc() + "' vào "
                            + thu + ", " + lichHoc.getNgayHoc() + " lúc " + lichHoc.getGioBatDau() + "!",
                            Alert.AlertType.INFORMATION);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking upcoming sessions", e);
        }
    }

    private String getVietnameseDayOfWeek(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY:
                return "Thứ 2";
            case TUESDAY:
                return "Thứ 3";
            case WEDNESDAY:
                return "Thứ 4";
            case THURSDAY:
                return "Thứ 5";
            case FRIDAY:
                return "Thứ 6";
            case SATURDAY:
                return "Thứ 7";
            case SUNDAY:
                return "Chủ nhật";
            default:
                return "";
        }
    }

    private void loadUserAvatar() {
        String userEmail = SessionManager.getLoggedInEmail();
        try {
            List<NguoiDung> allUsers = userService.getAllUsers();
            NguoiDung user = allUsers.stream()
                    .filter(u -> u.getEmail().equals(userEmail))
                    .findFirst()
                    .orElse(null);

            userNameLabel.setText(user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()
                    ? user.getFullName()
                    : "Không xác định");

            String avatarPath = user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()
                    ? "/com/ntn/images/avatars/" + user.getAvatar()
                    : DEFAULT_AVATAR_IMAGE;

            Image avatarImage;
            try (InputStream imageStream = getClass().getResourceAsStream(avatarPath)) {
                if (imageStream != null) {
                    avatarImage = new Image(imageStream, 60, 60, true, true);
                } else {
                    File file = new File("src/main/resources/com/ntn/images/avatars/" + avatarPath.substring(avatarPath.lastIndexOf("/") + 1));
                    avatarImage = file.exists()
                            ? new Image(file.toURI().toString(), 60, 60, true, true)
                            : new Image(getClass().getResourceAsStream(DEFAULT_AVATAR_IMAGE), 60, 60, true, true);
                }
            }
            avatarImageView.setImage(avatarImage);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading user info", e);
            showAlert("Lỗi", "Không thể tải thông tin người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
            setDefaultAvatar();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading avatar image", e);
            setDefaultAvatar();
        }
    }

    private void setDefaultAvatar() {
        userNameLabel.setText("Không xác định");
        try (InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_AVATAR_IMAGE)) {
            avatarImageView.setImage(new Image(defaultStream, 60, 60, true, true));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading default avatar", e);
        }
    }

    private void setupSortComboBox() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Phổ biến nhất", "Tên khóa học (A-Z)", "Giá (Thấp đến Cao)", "Giá (Cao đến Thấp)"
        ));
        sortComboBox.getSelectionModel().selectFirst();
        sortComboBox.setOnAction(e -> sortCourses());
    }

    private void loadCoursesAsync() {
        isMyCoursesView = false;
        Task<List<KhoaHoc>> task = new Task<>() {
            @Override
            protected List<KhoaHoc> call() throws Exception {
                List<KhoaHoc> courses = courseService.getAllActiveCourses();
                int userId = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
                if (userId != -1) {
                    List<KhoaHoc> enrolledCourses = courseService.getEnrolledCourses(userId);
                    courses = courses.stream()
                            .filter(khoaHoc -> canEnrollCourse(userId, khoaHoc, enrolledCourses))
                            .collect(Collectors.toList());
                }
                return courses;
            }
        };
        task.setOnSucceeded(event -> {
            khoaHocList = task.getValue();
            populateFilters();
            applyFilters();
        });
        task.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Failed to load courses", task.getException());
            Platform.runLater(() -> {
                showAlert("Lỗi", "Không thể tải danh sách khóa học: " + task.getException().getMessage(), Alert.AlertType.ERROR);
                courseFlowPane.getChildren().clear();
                courseFlowPane.getChildren().add(new Label("Lỗi tải dữ liệu. Vui lòng thử lại sau."));
            });
        });
        new Thread(task).start();
    }

    private void populateFilters() {
        subjectFilterBox.getChildren().clear();
        subjectCheckBoxes.clear();

        Set<String> subjects = khoaHocList.stream()
                .map(this::extractSubject)
                .collect(Collectors.toSet());
        List<String> subjectList = new ArrayList<>(subjects);
        Collections.sort(subjectList);
        for (int i = 0; i < subjectList.size(); i++) {
            String subject = subjectList.get(i);
            CheckBox checkBox = new CheckBox(subject);
            checkBox.setOnAction(event -> applyFilters());
            subjectCheckBoxes.put(subject, checkBox);
            if (i < 4 || subjectsExpanded) {
                subjectFilterBox.getChildren().add(checkBox);
            }
        }
        showMoreSubjectsButton.setVisible(subjectList.size() > 4);
    }

    private String extractSubject(KhoaHoc khoaHoc) {
        String tenKhoaHoc = khoaHoc.getTenKhoaHoc().toLowerCase();
        if (tenKhoaHoc.contains("java")) {
            return "Java";
        }
        if (tenKhoaHoc.contains("python")) {
            return "Python";
        }
        if (tenKhoaHoc.contains("web")) {
            return "Lập trình Web";
        }
        if (tenKhoaHoc.contains("ai") || tenKhoaHoc.contains("trí tuệ")) {
            return "Trí tuệ Nhân tạo";
        }
        if (tenKhoaHoc.contains("khoa học dữ liệu")) {
            return "Khoa học Dữ liệu";
        }
        if (tenKhoaHoc.contains("quản trị")) {
            return "Quản trị mạng";
        }
        if (tenKhoaHoc.contains("an toàn") || tenKhoaHoc.contains("bảo mật")) {
            return "Bảo mật";
        }
        if (tenKhoaHoc.contains("kiểm thử")) {
            return "Kiểm thử phần mềm";
        }
        return "Khác";
    }

    @FXML
    private void toggleShowMoreSubjects() {
        subjectsExpanded = !subjectsExpanded;
        subjectFilterBox.getChildren().clear();
        List<String> subjects = new ArrayList<>(subjectCheckBoxes.keySet());
        Collections.sort(subjects);
        for (int i = 0; i < subjects.size(); i++) {
            if (subjectsExpanded || i < 4) {
                subjectFilterBox.getChildren().add(subjectCheckBoxes.get(subjects.get(i)));
            }
        }
        showMoreSubjectsButton.setText(subjectsExpanded ? "Ẩn bớt" : "Hiển thị thêm");
    }

    @FXML
    private void applyFilters() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            showAlert("Cảnh báo", "Ngày bắt đầu phải trước ngày kết thúc.", Alert.AlertType.WARNING);
            return;
        }

        filteredCourses.clear();
        List<String> selectedSubjects = subjectCheckBoxes.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        for (KhoaHoc course : khoaHocList) {
            String subject = extractSubject(course);
            boolean subjectMatch = selectedSubjects.isEmpty() || selectedSubjects.contains(subject);
            LocalDate courseStartDate = course.getNgayBatDau();
            boolean dateMatch = true;
            if (startDate != null && (courseStartDate == null || courseStartDate.isBefore(startDate))) {
                dateMatch = false;
            }
            if (endDate != null && (courseStartDate == null || courseStartDate.isAfter(endDate))) {
                dateMatch = false;
            }
            if (subjectMatch && dateMatch) {
                filteredCourses.add(course);
            }
        }

        sortCourses();
        currentPage = 1;
        loadPage(currentPage);
    }

    @FXML
    private void clearFilters() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        subjectCheckBoxes.values().forEach(checkBox -> checkBox.setSelected(false));
        applyFilters();
    }

    @FXML
    private void sortCourses() {
        String sortOption = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortOption == null) {
            return;
        }
        switch (sortOption) {
            case "Tên khóa học (A-Z)":
                filteredCourses.sort(Comparator.comparing(KhoaHoc::getTenKhoaHoc));
                break;
            case "Giá (Thấp đến Cao)":
                filteredCourses.sort(Comparator.comparingDouble(KhoaHoc::getGia));
                break;
            case "Giá (Cao đến Thấp)":
                filteredCourses.sort(Comparator.comparingDouble(KhoaHoc::getGia).reversed());
                break;
            default:
                break;
        }
        loadPage(currentPage);
    }

    private void loadPage(int page) {
        courseFlowPane.getChildren().clear();
        if (filteredCourses.isEmpty()) {
            Label noCoursesLabel = new Label("Không có khóa học nào để hiển thị.");
            noCoursesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            courseFlowPane.getChildren().add(noCoursesLabel);
            pageLabel.setText("Trang 1");
            prevPageBtn.setDisable(true);
            nextPageBtn.setDisable(true);
            resultsLabel.setText("Danh Sách Khóa Học (0)");
            return;
        }

        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredCourses.size());

        for (int i = start; i < end; i++) {
            KhoaHoc khoaHoc = filteredCourses.get(i);
            VBox courseCard = createCourseCard(khoaHoc);
            courseFlowPane.getChildren().add(courseCard);
        }

        pageLabel.setText("Trang " + page);
        prevPageBtn.setDisable(page == 1);
        nextPageBtn.setDisable(end >= filteredCourses.size());
        resultsLabel.setText("Danh Sách Khóa Học (" + filteredCourses.size() + ")");
    }

    private VBox createCourseCard(KhoaHoc khoaHoc) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); -fx-padding: 15px; -fx-max-width: 300px;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(270);
        imageView.setFitHeight(150);
        loadCourseImage(imageView, khoaHoc.getHinhAnh());
        imageView.setStyle("-fx-background-radius: 10px;");

        Label lecturerLabel = new Label(khoaHoc.getTenGiangVien());
        lecturerLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        Label titleLabel = new Label(khoaHoc.getTenKhoaHoc());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        titleLabel.setWrapText(true);

        Label descriptionLabel = new Label(khoaHoc.getMoTa());
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        descriptionLabel.setWrapText(true);

        String duration = calculateDuration(khoaHoc.getNgayBatDau(), khoaHoc.getNgayKetThuc());
        Label durationLabel = new Label("Thời gian: " + duration);
        durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        String level = khoaHoc.getTenKhoaHoc().toLowerCase().contains("cơ bản") ? "Sơ cấp" : "Trung cấp";
        Label levelLabel = new Label("Cấp độ: " + level);
        levelLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        Label priceLabel = new Label(String.format("Giá: %,d VNĐ", (long) khoaHoc.getGia()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #0078d4; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        if (!isMyCoursesView) {
            Button addButton = new Button("Thêm vào giỏ");
            addButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 15px;");
            addButton.setOnAction(e -> addToCart(khoaHoc));
            buttonBox.getChildren().add(addButton);
        }

        if (isGoToMyCourse) {
            Button detailsButton = new Button("Xem chi tiết");

            detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 15px;");
            detailsButton.setOnAction(e -> showCourseDetails(khoaHoc));
            buttonBox.getChildren().add(detailsButton);
        } else {

//            vao hoc ngay
            Button vaoHoc = new Button("Vào học ngay");
            vaoHoc.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 15px;");
            vaoHoc.setOnAction(e -> {
                try {
                    vaoHoc(khoaHoc.getId());
                } catch (IOException ex) {
                    Logger.getLogger(DashboardStudentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            buttonBox.getChildren().add(vaoHoc);

            Button hoanTien = new Button("Hoàn tiền học phí");
            hoanTien.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 15px;");
            hoanTien.setOnAction(e -> {
                try {
                    hoanTienHocPhi(khoaHoc.getId());
                } catch (SQLException ex) {
                    Logger.getLogger(DashboardStudentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            buttonBox.getChildren().add(hoanTien);

        }

        card.getChildren().addAll(imageView, lecturerLabel, titleLabel, descriptionLabel, durationLabel, levelLabel, priceLabel, buttonBox);
        return card;
    }

    private String calculateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Không xác định";
        }
        long days = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
        long weeks = days / 7;
        return weeks < 1 ? days + " ngày" : weeks + " tuần";
    }

    private void loadCourseImage(ImageView imageView, String hinhAnh) {
        try {
            String imagePath = hinhAnh == null || hinhAnh.trim().isEmpty()
                    ? DEFAULT_COURSE_IMAGE
                    : IMAGE_PATH + hinhAnh.replaceAll("\\\\", "/").trim();
            if (!imagePath.matches(".*\\.(png|jpg|jpeg|gif)$")) {
                imagePath = DEFAULT_COURSE_IMAGE;
            }
            Image image;
            try (InputStream imageStream = getClass().getResourceAsStream(imagePath)) {
                if (imageStream != null) {
                    image = new Image(imageStream, 270, 150, true, true);
                } else {
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    File file = new File(UPLOAD_DIR + fileName);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString(), 270, 150, true, true);
                    } else {
                        try (InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_COURSE_IMAGE)) {
                            image = defaultStream != null
                                    ? new Image(defaultStream, 270, 150, true, true)
                                    : new Image(PLACEHOLDER_IMAGE, 270, 150, true, true);
                        }
                    }
                }
            }
            imageView.setImage(image);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading image: " + hinhAnh, e);
            imageView.setImage(new Image(PLACEHOLDER_IMAGE, 270, 150, true, true));
        }
    }

    private void changePage(int delta) {
        int newPage = currentPage + delta;
        if (newPage >= 1 && newPage <= (int) Math.ceil((double) filteredCourses.size() / ITEMS_PER_PAGE)) {
            currentPage = newPage;
            loadPage(currentPage);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        openNewWindow("/com/ntn/views/login.fxml", "Đăng nhập", 640, 650, null);
        Window window = ((Node) event.getSource()).getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        Task<List<KhoaHoc>> task = new Task<>() {
            @Override
            protected List<KhoaHoc> call() throws Exception {
                List<KhoaHoc> allCourses = keyword.isEmpty() ? courseService.getAllActiveCourses() : courseService.searchCourses(keyword);
                int userId = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
                if (userId == -1) {
                    throw new IllegalStateException("Cannot determine user.");
                }
                List<KhoaHoc> enrolledCourses = courseService.getEnrolledCourses(userId);
                return allCourses.stream()
                        .filter(khoaHoc -> canEnrollCourse(userId, khoaHoc, enrolledCourses))
                        .collect(Collectors.toList());
            }
        };
        task.setOnSucceeded(event -> {
            khoaHocList = task.getValue();
            applyFilters();
        });
        task.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Error searching courses", task.getException());
            Platform.runLater(() -> {
                showAlert("Lỗi", "Không thể tìm kiếm khóa học: " + task.getException().getMessage(), Alert.AlertType.ERROR);
                courseFlowPane.getChildren().clear();
                courseFlowPane.getChildren().add(new Label("Lỗi tìm kiếm. Vui lòng thử lại."));
            });
        });
        new Thread(task).start();
    }

    public boolean canEnrollCourse(int nguoiDungID, KhoaHoc khoaHoc, List<KhoaHoc> enrolledCourses) {
        try {
            int hocVienID = userService.getHocVienIDFromNguoiDung(nguoiDungID);
            if (hocVienID == -1) {
                LOGGER.warning("No hocVienID found for nguoiDungID: " + nguoiDungID);
                return false;
            }
            if (courseService.isCourseEnrolled(hocVienID, khoaHoc.getId())) {
                return false;
            }
            int currentCount = courseService.getCurrentEnrollmentCount(khoaHoc.getId());
            if (currentCount >= khoaHoc.getSoLuongHocVienToiDa()) {
                return false;
            }
            LocalDateTime ngayBatDau = khoaHoc.getNgayBatDau() != null ? khoaHoc.getNgayBatDau().atStartOfDay() : null;
            if (ngayBatDau != null && Duration.between(LocalDateTime.now(), ngayBatDau).toHours() < 24) {
                return false;
            }
            // Kiểm tra trùng lịch học, chỉ log không hiển thị thông báo
            if (userService.hasOverlappingSchedule(hocVienID, khoaHoc.getId())) {
                LOGGER.info("Khóa học " + khoaHoc.getTenKhoaHoc() + " trùng lịch với khóa học đã đăng ký.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking course eligibility: " + khoaHoc.getTenKhoaHoc(), e);
            return false;
        }
    }

    private void showCourseDetails(KhoaHoc khoaHoc) {
        openNewWindow(
                "/com/ntn/views/course_details.fxml",
                "Chi tiết khóa học: " + khoaHoc.getTenKhoaHoc(),
                600, 400,
                loader -> {
                    CourseDetailsController ctrl = loader.getController();
                    ctrl.setCourse(khoaHoc);
                    return null;
                }
        );
    }

    private void setupCartTable() {
        courseColumn.setCellValueFactory(data -> data.getValue().tenKhoaHocProperty());
        instructorColumn.setCellValueFactory(data -> data.getValue().tenGiangVienProperty());
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("gia"));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double gia, boolean empty) {
                super.updateItem(gia, empty);
                setText(empty || gia == null ? null : String.format("%,.0f VNĐ", gia));
            }
        });
        imageColumn.setCellValueFactory(data -> data.getValue().hinhAnhProperty());
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String hinhAnh, boolean empty) {
                super.updateItem(hinhAnh, empty);
                if (empty || hinhAnh == null) {
                    setGraphic(null);
                } else {
                    loadCourseImage(imageView, hinhAnh);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });
        cartTable.setItems(cartCourses);
    }

    private void updateTotalPrice() {
        double total = cartCourses.stream().mapToDouble(KhoaHoc::getGia).sum();
        Platform.runLater(() -> totalPriceLabel.setText(String.format("Tổng tiền: %,d VNĐ", (long) total)));
    }

    public void addToCart(KhoaHoc khoaHoc) {
        int userId = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
        if (userId == -1) {
            showAlert("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Alert.AlertType.ERROR);
            return;
        }
        try {
            int hocVienId = userService.getHocVienIDFromNguoiDung(userId);
            if (hocVienId == -1) {
                showAlert("Lỗi", "Không tìm thấy thông tin học viên. Vui lòng liên hệ quản trị viên.", Alert.AlertType.ERROR);
                return;
            }

            List<KhoaHoc> enrolledCourses = courseService.getEnrolledCourses(userId);
            if (!canEnrollCourse(userId, khoaHoc, enrolledCourses)) {
                showAlert("Cảnh báo", "Không thể thêm khóa học " + khoaHoc.getTenKhoaHoc() + ".", Alert.AlertType.WARNING);
                return;
            }

            // Kiểm tra xung đột lịch với các khóa học trong giỏ hàng
            if (!cartCourses.isEmpty()) {
                String conflictMessage = checkCartScheduleConflict(hocVienId, khoaHoc);
                if (!conflictMessage.isEmpty()) {
                    showAlert("Cảnh báo", conflictMessage, Alert.AlertType.WARNING);
                    return;
                }
            }

            // Thêm vào giỏ hàng nếu không có xung đột
            if (!cartCourses.contains(khoaHoc)) {
                cartCourses.add(khoaHoc);
                showAlert("Thành công", "Đã thêm khóa học " + khoaHoc.getTenKhoaHoc() + " vào giỏ hàng!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Thông báo", "Khóa học " + khoaHoc.getTenKhoaHoc() + " đã có trong giỏ hàng!", Alert.AlertType.WARNING);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking course status or schedule conflict", e);
            showAlert("Lỗi", "Không thể kiểm tra trạng thái khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String checkCartScheduleConflict(int hocVienId, KhoaHoc khoaHoc) throws SQLException {
        // Lấy danh sách khoaHocId từ giỏ hàng
        List<Integer> cartCourseIds = cartCourses.stream()
                .map(KhoaHoc::getId)
                .collect(Collectors.toList());

        // Truy vấn lịch học của khóa học mới và các khóa học trong giỏ hàng
        String sql = "SELECT lh1.khoaHocId AS khoaHocId1, lh1.ngay_hoc AS ngay_hoc1, lh1.gio_bat_dau AS gio_bat_dau1, lh1.gio_ket_thuc AS gio_ket_thuc1, "
                + "lh2.khoaHocId AS khoaHocId2, lh2.ngay_hoc AS ngay_hoc2, lh2.gio_bat_dau AS gio_bat_dau2, lh2.gio_ket_thuc AS gio_ket_thuc2 "
                + "FROM lich_hoc lh1 "
                + "JOIN lich_hoc lh2 ON lh1.ngay_hoc = lh2.ngay_hoc "
                + "WHERE lh1.khoaHocId IN (" + String.join(",", Collections.nCopies(cartCourseIds.size(), "?")) + ") "
                + "AND lh2.khoaHocId = ? "
                + "AND (lh1.gio_bat_dau <= lh2.gio_ket_thuc AND lh1.gio_ket_thuc >= lh2.gio_bat_dau)";

        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Đặt tham số cho các khoaHocId trong giỏ hàng
            for (int i = 0; i < cartCourseIds.size(); i++) {
                stmt.setInt(i + 1, cartCourseIds.get(i));
            }
            // Đặt tham số cho khoaHocId mới
            stmt.setInt(cartCourseIds.size() + 1, khoaHoc.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> conflictDates = new ArrayList<>();
                while (rs.next()) {
                    LocalDate ngayHoc = rs.getDate("ngay_hoc1").toLocalDate();
                    if (!conflictDates.contains(ngayHoc.toString())) {
                        conflictDates.add(ngayHoc.toString());
                    }
                }

                if (!conflictDates.isEmpty()) {
                    return String.format("Khóa học %s (ID: %d) trùng lịch với khóa học trong giỏ hàng vào các ngày: %s",
                            khoaHoc.getTenKhoaHoc(), khoaHoc.getId(), String.join(", ", conflictDates));
                }
                return "";
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking schedule conflict for hocVienId: " + hocVienId + ", khoaHocId: " + khoaHoc.getId(), e);
            throw e;
        }
    }

    @FXML
    private void handlePayButton() {
        if (cartCourses.isEmpty()) {
            showAlert("Cảnh báo", "Giỏ hàng trống. Vui lòng thêm khóa học!", Alert.AlertType.WARNING);
            return;
        }
        int userId = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
        if (userId == -1) {
            showAlert("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Alert.AlertType.ERROR);
            return;
        }
        double totalAmount = cartCourses.stream().mapToDouble(KhoaHoc::getGia).sum();
        showManualPayment(userId, totalAmount);
    }

    private void showManualPayment(int userId, double totalAmount) {
        openNewWindow(
                "/com/ntn/views/payment_dashboard.fxml",
                "Thanh toán thủ công",
                600, 600,
                loader -> {
                    PaymentDashboardController controller = loader.getController();
                    String username = userNameLabel.getText();
                    controller.initData(userId, totalAmount, cartCourses, unused -> {
                        Platform.runLater(() -> {
                            showAlert("Thành công", "Thanh toán đã được gửi và đang chờ quản trị viên xét duyệt.", Alert.AlertType.INFORMATION);
                            cartCourses.clear();
                            loadCoursesAsync();
                        });
                    }, username);
                    return null;
                }
        );
    }

    private void handleRemove() {
        KhoaHoc selectedCourse = cartTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            cartCourses.remove(selectedCourse);
            showAlert("Thành công", "Đã xóa khóa học " + selectedCourse.getTenKhoaHoc() + " khỏi giỏ hàng!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một khóa học để xóa!", Alert.AlertType.WARNING);
        }
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

    private void loadDashboard() {
        isGoToMyCourse = true;
        isMyCoursesView = false;
        loadCoursesAsync();
        cartTable.setVisible(true);
        payButton.setVisible(true);
        removeButton.setVisible(true);
        setButtonStyles(dashboardBtn, coursesBtn, timetableBtn, profileBtn);
    }

    private void loadMyCourses() {
        isGoToMyCourse = false;
        int nguoiDungID = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
        if (nguoiDungID == -1) {
            showAlert("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Alert.AlertType.ERROR);
            return;
        }
        try {
            int hocVienID = userService.getHocVienIDFromNguoiDung(nguoiDungID);
            if (hocVienID == -1) {
                showAlert("Lỗi", "Không tìm thấy thông tin học viên. Vui lòng liên hệ quản trị viên.", Alert.AlertType.ERROR);
                return;
            }
            Task<List<KhoaHoc>> task = new Task<>() {
                @Override
                protected List<KhoaHoc> call() throws Exception {
                    return courseService.getEnrolledCourses(hocVienID);
                }
            };
            task.setOnSucceeded(event -> {
                List<KhoaHoc> enrolledCourses = task.getValue();
                if (enrolledCourses.isEmpty()) {
                    showAlert("Thông báo", "Bạn chưa đăng ký khóa học nào.", Alert.AlertType.INFORMATION);
                }
                isMyCoursesView = true;
                khoaHocList = enrolledCourses;
                filteredCourses.setAll(khoaHocList);
                currentPage = 1;
                loadPage(currentPage);
                cartTable.setVisible(false);
                payButton.setVisible(false);
                removeButton.setVisible(false);
                setButtonStyles(coursesBtn, dashboardBtn, timetableBtn, profileBtn);
            });
            task.setOnFailed(event -> {
                LOGGER.log(Level.SEVERE, "Error loading enrolled courses", task.getException());
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể tải danh sách khóa học: " + task.getException().getMessage(), Alert.AlertType.ERROR);
                    courseFlowPane.getChildren().clear();
                    courseFlowPane.getChildren().add(new Label("Lỗi tải dữ liệu. Vui lòng thử lại."));
                });
            });
            new Thread(task).start();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving hocVienID", e);
            showAlert("Lỗi", "Không thể lấy thông tin học viên: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void loadTimetable() {
        openNewWindow("/com/ntn/views/timetable.fxml", "Thời Khóa Biểu", 800, 600, null);
        setButtonStyles(timetableBtn, dashboardBtn, coursesBtn, profileBtn);
    }

    @FXML
    private void loadProfile() {
        String userEmail = SessionManager.getLoggedInEmail();
        int userId = Database.getUserIdByEmail(userEmail);
        if (userId == -1) {
            showAlert("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Alert.AlertType.ERROR);
            return;
        }
        openNewWindow("/com/ntn/views/profile.fxml", "Hồ Sơ", 600, 400, null);
        loadUserAvatar();
        setButtonStyles(profileBtn, dashboardBtn, coursesBtn, timetableBtn);
    }

    private void setButtonStyles(Button activeBtn, Button... inactiveBtns) {
        activeBtn.setStyle("-fx-background-color: #6c5ce7; -fx-text-fill: white; -fx-background-radius: 5;");
        for (Button btn : inactiveBtns) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        }
    }

    private boolean isOverlapping(KhoaHoc khoaHoc, KhoaHoc enrolled) {
        LocalDate start1 = khoaHoc.getNgayBatDau();
        LocalDate end1 = khoaHoc.getNgayKetThuc();
        LocalDate start2 = enrolled.getNgayBatDau();
        LocalDate end2 = enrolled.getNgayKetThuc();
        return !(start1 == null || end1 == null || start2 == null || end2 == null
                || end1.isBefore(start2) || start1.isAfter(end2));
    }

    private static boolean isRunningFromJar() {
        return DashboardStudentController.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar");
    }

    private <T> T openNewWindow(String fxmlPath, String title, int width, int height, javafx.util.Callback<FXMLLoader, Void> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (initializer != null) {
                initializer.call(loader);
            }
            Stage stage = new Stage();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening window: " + fxmlPath, e);
            showAlert("Lỗi", "Không thể mở cửa sổ: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

//    Nguyen lm
    public void vaoHoc(int id) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/ntn/views/course.fxml"));
        Parent root = fxmlLoader.load(); // Load FXML trước

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();

        CourseController controller = fxmlLoader.getController(); //  Sau khi load mới gọi getController
        controller.setIdKhoaHoc(id); // Truyền ID

        controller.loadBaiTaiTheoKhoaHocID();

    }

    public void hoanTienHocPhi(int id) throws SQLException {
        CourseService courseService = new CourseService();
        PaymentService paymentService = new PaymentService();
        String email = SessionManager.getLoggedInEmail();

        // Kiểm tra email
        if (email == null) {
            showAlert("Lỗi", "Không tìm thấy thông tin đăng nhập.", Alert.AlertType.ERROR);
            return;
        }

        // Lấy thông tin người dùng
        List<NguoiDung> allUsers = userService.getAllUsers();
        NguoiDung user = allUsers.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        if (user == null) {
            showAlert("Lỗi", "Không tìm thấy thông tin người dùng.", Alert.AlertType.ERROR);
            return;
        }

        // Lấy ID học viên
        int hocVienID = userService.getHocVienIDFromNguoiDung(user.getId());
        if (hocVienID == -1) {
            showAlert("Lỗi", "Không tìm thấy thông tin học viên.", Alert.AlertType.ERROR);
            return;
        }

        // Lấy thông tin đăng ký
        List<KhoaHocHocVien> enrollments = courseService.getKhoaHocHocVien(hocVienID, id);
        if (enrollments.isEmpty()) {
            showAlert("Lỗi", "Bạn chưa đăng ký khóa học này.", Alert.AlertType.ERROR);
            return;
        }

        KhoaHocHocVien kh_hv = enrollments.get(0); // Lấy phần tử đầu tiên

        // Lấy ngày đăng ký dạng LocalDateTime
        LocalDateTime ngayDangKy = kh_hv.getNgay_dang_ky();
        if (ngayDangKy == null) {
            showAlert("Lỗi", "Ngày đăng ký không hợp lệ.", Alert.AlertType.ERROR);
            return;
        }

        LocalDateTime ngayRut = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(ngayDangKy, ngayRut);

        if (daysBetween <= 7) { // Kiểm tra nếu trong vòng 7 ngày
            // Bắt đầu giao dịch
            Connection conn = null;
            try {
                conn = Database.getConn();
                conn.setAutoCommit(false);

                // Cập nhật trạng thái đăng ký thành CANCELLED
                courseService.updateStatus(hocVienID, id);

                // Xóa bản ghi thanh toán liên quan
                String deletePaymentSql = "DELETE FROM lichsu_thanhtoan WHERE hocVienID = ? AND khoaHocID = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deletePaymentSql)) {
                    deleteStmt.setInt(1, hocVienID);
                    deleteStmt.setInt(2, id);
                    int paymentRowsAffected = deleteStmt.executeUpdate();
                    LOGGER.info("Đã xóa " + paymentRowsAffected + " bản ghi lichsu_thanhtoan cho hocVienID=" + hocVienID + ", khoaHocID=" + id);
                }

                conn.commit();
                showAlert("Hoàn tiền thành công!", "Bạn đã được hoàn tiền vì yêu cầu rút trong vòng 7 ngày.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        LOGGER.severe("Lỗi khi rollback: " + rollbackEx.getMessage());
                    }
                }
                LOGGER.severe("Lỗi khi hoàn tiền: " + e.getMessage());
                showAlert("Lỗi", "Không thể xử lý hoàn tiền. Vui lòng thử lại sau.", Alert.AlertType.ERROR);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        LOGGER.severe("Lỗi khi đóng kết nối: " + e.getMessage());
                    }
                }
            }
        } else {
            showAlert("Hoàn tiền không thành công", "Bạn không đủ điều kiện hoàn tiền vì đã quá 7 ngày.", Alert.AlertType.WARNING);
        }
    }

    ObservableList<KhoaHoc> getCartCourses() {
        return cartCourses;
    }
}


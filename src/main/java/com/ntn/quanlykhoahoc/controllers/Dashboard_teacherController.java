

package com.ntn.quanlykhoahoc.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc_HocVien;
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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Dashboard_teacherController implements Initializable {

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
//    @FXML
//    private TableView<KhoaHoc> cartTable;
//    @FXML
//    private TableColumn<KhoaHoc, String> courseColumn;
//    @FXML
//    private TableColumn<KhoaHoc, String> instructorColumn;
//    @FXML
//    private TableColumn<KhoaHoc, Double> priceColumn;
//    @FXML
//    private TableColumn<KhoaHoc, String> imageColumn;
//    @FXML
//    private Button dashboardBtn, coursesBtn, timetableBtn, profileBtn, logoutButton;
//    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
//    @FXML
//    private DatePicker startDatePicker;
//    @FXML
//    private DatePicker endDatePicker;
//    @FXML
//    private Button applyFilterButton;
//    @FXML
//    private Button clearFilterButton;
    @FXML
    private Label resultsLabel;
    @FXML
    private ComboBox<String> sortComboBox;
//    @FXML
//    private VBox subjectFilterBox;
//    @FXML
//    private Button showMoreSubjectsButton;
//    @FXML
//    private Label totalPriceLabel;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSortComboBox();

        loadUserAvatar();
        loadMyCourses();
        loadPage(currentPage);

        // Bind event handlers
        prevPageBtn.setOnAction(e -> changePage(-1));
        nextPageBtn.setOnAction(e -> changePage(1));

//        coursesBtn.setOnAction(e -> loadMyCourses());
//        timetableBtn.setOnAction(e -> loadTimetable());
//        profileBtn.setOnAction(e -> loadProfile());
    }

    private static boolean isRunningFromJar() {
        return DashboardStudentController.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar");
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
// tao 1 khoa hoc 

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

        Button addButton = new Button("Sửa sĩ số học viên");
        addButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: 5px 15px;");
        addButton.setOnAction(e -> {
            try {
                suaSiSo(khoaHoc.getId());
            } catch (SQLException ex) {
                Logger.getLogger(Dashboard_teacherController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        buttonBox.getChildren().add(addButton);

        card.getChildren().addAll(imageView, lecturerLabel, titleLabel, descriptionLabel, durationLabel, levelLabel, priceLabel, buttonBox);
        return card;
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

    private String calculateDuration(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return "Không xác định";
        }
        long days = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
        long weeks = days / 7;
        return weeks < 1 ? days + " ngày" : weeks + " tuần";
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        openNewWindow("/com/ntn/views/login.fxml", "Đăng nhập", 400, 300, null);
        Window window = ((Node) event.getSource()).getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
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

    private void loadMyCourses() {
        isGoToMyCourse = true;
        int nguoiDungID = Database.getUserIdByEmail(SessionManager.getLoggedInEmail());
        if (nguoiDungID == -1) {
            showAlert("Lỗi", "Không thể xác định người dùng. Vui lòng đăng nhập lại.", Alert.AlertType.ERROR);
            return;
        }
        try {
            int hocVienID = userService.getHocVienIDFromNguoiDung(nguoiDungID);
//            if (hocVienID == -1) {
//                showAlert("Lỗi", "Không tìm thấy thông tin học viên. Vui lòng liên hệ quản trị viên.", Alert.AlertType.ERROR);
//                return;
//            }
            Task<List<KhoaHoc>> task = new Task<>() {
                @Override
                protected List<KhoaHoc> call() throws Exception {
                    return courseService.getCoursesByTeacherId(nguoiDungID);
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
//                cartTable.setVisible(false);
//                payButton.setVisible(false);
//                removeButton.setVisible(false);
//                setButtonStyles(dashboardBtn, coursesBtn, timetableBtn, profileBtn);
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

    private void setButtonStyles(Button activeBtn, Button... inactiveBtns) {
        activeBtn.setStyle("-fx-background-color: #6c5ce7; -fx-text-fill: white; -fx-background-radius: 5;");
        for (Button btn : inactiveBtns) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        }
    }

    // Nguyen
    public void sortCourses() {
    }

    public void suaSiSo(int khoaHocID) throws SQLException {
        CourseService courseService1 = new CourseService();

        Label label = new Label("Nhập số:");

        TextField numberField = new TextField();
        numberField.setPromptText("Nhập số tại đây");
        Button btnClose = new Button();

        Button submitButton = new Button("Sửa");
        VBox vbox = new VBox(10);  // Khoảng cách giữa các phần tử là 10px
        vbox.getChildren().addAll(label, numberField, submitButton);
        Scene scene = new Scene(vbox, 300, 200);// Chiều rộng 300px, chiều cao 200px
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Nhập số");  // Tiêu đề cửa sổ
        stage.show();

        submitButton.setOnAction(e -> {
            try {
                int so_luong_hoc_vien_toi_da = Integer.parseInt(numberField.getText());

                if (courseService1.updateSoLuong(so_luong_hoc_vien_toi_da, khoaHocID) == 1) {
                    showAlert("Thành công", "Sửa thành công ", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Thất bại", "Sửa thất bại ", Alert.AlertType.ERROR);
                }

                stage.close();
            } catch (SQLException ex) {
                Logger.getLogger(Dashboard_teacherController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

}
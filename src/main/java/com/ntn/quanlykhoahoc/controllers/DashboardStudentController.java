package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardStudentController {
    @FXML private Label welcomeLabel;
    @FXML private GridPane coursesGrid;
    @FXML private ScrollPane coursesScrollPane;
    @FXML private Button prevPageBtn, nextPageBtn, payButton, removeButton;
    @FXML private Label pageLabel;
    @FXML private TableView<KhoaHoc> cartTable;
    @FXML private TableColumn<KhoaHoc, String> courseColumn;
    @FXML private TableColumn<KhoaHoc, String> instructorColumn;
    @FXML private TableColumn<KhoaHoc, Double> priceColumn;
    @FXML private TableColumn<KhoaHoc, String> imageColumn;
    @FXML private Button dashboardBtn, coursesBtn, profileBtn, logoutButton;

    private final int ITEMS_PER_PAGE = 12;
    private int currentPage = 1;
    private List<KhoaHoc> khoaHocList = new ArrayList<>();
    private final ObservableList<KhoaHoc> cartCourses = FXCollections.observableArrayList();
    private static final String IMAGE_PATH = "/com/ntn/images/courses/"; // Đường dẫn cơ sở trong classpath
    private CourseService courseService = new CourseService();

    @FXML
    public void initialize() {
        setupGrid();

        try {
            khoaHocList = courseService.getAllActiveCourses();
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể tải danh sách khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
            khoaHocList = new ArrayList<>();
        }

        loadPage(currentPage);
        setupCartTable();

        String userEmail = SessionManager.getLoggedInEmail();
        String hoTen = Database.getUserNameByEmail(userEmail);
        welcomeLabel.setText("Chào mừng, " + (hoTen != null ? hoTen : "Sinh viên"));

        prevPageBtn.setOnAction(e -> changePage(-1));
        nextPageBtn.setOnAction(e -> changePage(1));
        payButton.setOnAction(e -> handlePay());
        removeButton.setOnAction(e -> handleRemove());

        dashboardBtn.setOnAction(e -> loadDashboard());
        coursesBtn.setOnAction(e -> loadMyCourses());
        profileBtn.setOnAction(e -> loadProfile());
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        Window window = ((Node) event.getSource()).getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }

        Parent root = FXMLLoader.load(getClass().getResource("/com/ntn/quanlykhoahoc/views/Login.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Đăng nhập");
        stage.show();
    }

    private void setupGrid() {
        coursesGrid.setHgap(30);
        coursesGrid.setVgap(30);
        coursesGrid.setStyle("-fx-padding: 15px;");
    }

    private void loadPage(int page) {
        coursesGrid.getChildren().clear();
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, khoaHocList.size());

        for (int i = start; i < end; i++) {
            KhoaHoc khoaHoc = khoaHocList.get(i);
            VBox courseBox = createCourseBox(khoaHoc);
            coursesGrid.add(courseBox, i % 5, (i - start) / 5);
        }

        pageLabel.setText("Trang " + page);
        prevPageBtn.setDisable(page == 1);
        nextPageBtn.setDisable(end >= khoaHocList.size());
    }

    private VBox createCourseBox(KhoaHoc khoaHoc) {
        ImageView courseImage = new ImageView();
        courseImage.setFitWidth(150);
        courseImage.setFitHeight(100);
        courseImage.setPreserveRatio(true);

        Image image = loadCourseImage(khoaHoc.getHinhAnh());
        courseImage.setImage(image);

        Button addButton = new Button("Thêm");
        addButton.setOnAction(e -> addToCart(khoaHoc));

        VBox courseBox = new VBox(10,
            courseImage,
            new Label(khoaHoc.getTenKhoaHoc()),
            new Label("Giảng viên: " + khoaHoc.getTenGiangVien()),
            new Label("Giá: " + String.format("%,.0f VNĐ", khoaHoc.getGia())),
            addButton
        );
        courseBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 10px; -fx-padding: 10px; -fx-background-color: #ecf0f1;");
        courseBox.setAlignment(javafx.geometry.Pos.CENTER);

        return courseBox;
    }

    private Image loadCourseImage(String hinhAnh) {
        try {
            // Nếu hinhAnh là null hoặc rỗng, dùng mặc định
            if (hinhAnh == null || hinhAnh.trim().isEmpty()) {
                hinhAnh = "default_course.jpg";
            }
            // Tải hình ảnh từ /com/ntn/images/courses/ (ví dụ: 1.jpg, 2.jpg)
            String imagePath = IMAGE_PATH + hinhAnh;
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                return new Image(imageStream, 150, 100, true, true);
            }
            // Fallback về default_course.jpg trong cùng thư mục
            InputStream defaultStream = getClass().getResourceAsStream(IMAGE_PATH + "default_course.jpg");
            if (defaultStream != null) {
                return new Image(defaultStream, 150, 100, true, true);
            }
            throw new IOException("Không tìm thấy hình ảnh mặc định!");
        } catch (Exception e) {
            System.err.println("Lỗi tải hình ảnh: " + hinhAnh + ". Ngoại lệ: " + e.getMessage());
            // Trả về hình mặc định nếu có lỗi
            InputStream fallbackStream = getClass().getResourceAsStream(IMAGE_PATH + "default_course.jpg");
            if (fallbackStream != null) {
                return new Image(fallbackStream, 150, 100, true, true);
            }
            return null; // Hoặc xử lý thêm nếu cần
        }
    }

    private void changePage(int delta) {
        int newPage = currentPage + delta;
        if (newPage >= 1 && newPage <= (int) Math.ceil((double) khoaHocList.size() / ITEMS_PER_PAGE)) {
            currentPage = newPage;
            loadPage(currentPage);
        }
    }

    private void setupCartTable() {
        courseColumn.setCellValueFactory(data -> data.getValue().tenKhoaHocProperty());
        instructorColumn.setCellValueFactory(data -> data.getValue().tenGiangVienProperty());
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("gia"));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double gia, boolean empty) {
                super.updateItem(gia, empty);
                if (empty || gia == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VNĐ", gia));
                }
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
                    Image image = loadCourseImage(hinhAnh);
                    imageView.setImage(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

        cartTable.setItems(cartCourses);
    }

    private void addToCart(KhoaHoc khoaHoc) {
        if (!cartCourses.contains(khoaHoc)) {
            cartCourses.add(khoaHoc);
            showAlert("Thành công", "Đã thêm khóa học " + khoaHoc.getTenKhoaHoc() + " vào giỏ hàng!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Thông báo", "Khóa học " + khoaHoc.getTenKhoaHoc() + " đã có trong giỏ hàng!", Alert.AlertType.WARNING);
        }
    }

    private void handlePay() {
        if (cartCourses.isEmpty()) {
            showAlert("Cảnh báo", "Giỏ hàng trống! Vui lòng thêm khóa học trước khi thanh toán.", Alert.AlertType.WARNING);
            return;
        }
        cartCourses.clear();
        showAlert("Thành công", "Thanh toán thành công!", Alert.AlertType.INFORMATION);
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

    private void loadDashboard() {
        setButtonStyles(dashboardBtn, coursesBtn, profileBtn);
    }

    private void loadMyCourses() {
        showAlert("Thông báo", "Chức năng 'Khóa học của tôi' chưa được triển khai.", Alert.AlertType.INFORMATION);
        setButtonStyles(coursesBtn, dashboardBtn, profileBtn);
    }

    private void loadProfile() {
        showAlert("Thông báo", "Chức năng 'Hồ sơ' chưa được triển khai.", Alert.AlertType.INFORMATION);
        setButtonStyles(profileBtn, dashboardBtn, coursesBtn);
    }

    private void setButtonStyles(Button activeBtn, Button... inactiveBtns) {
        activeBtn.setStyle("-fx-background-color: #6c5ce7; -fx-text-fill: white; -fx-background-radius: 5;");
        for (Button btn : inactiveBtns) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
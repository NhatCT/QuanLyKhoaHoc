package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import com.ntn.quanlykhoahoc.pojo.ThanhToan;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.PaymentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DashboardAdminController {

    private static final Logger LOGGER = Logger.getLogger(DashboardAdminController.class.getName());

    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView userAvatar;
    @FXML
    private Button manageCoursesBtn, manageUsersBtn, managePaymentsBtn, logoutBtn;
    @FXML
    private Label totalUsersLabel, totalCoursesLabel, totalPaymentsLabel;
    @FXML
    private TableView<NguoiDung> userTable;
    @FXML
    private TableView<KhoaHoc> courseTable;
    @FXML
    private TableView<ThanhToan> paymentTable;
    @FXML
    private TableColumn<NguoiDung, String> avatarColumn, hoColumn, tenColumn, emailColumn,
            userStatusColumn, loaiNguoiDungColumn;
    @FXML
    private TableColumn<KhoaHoc, Integer> courseIdColumn;
    @FXML
    private TableColumn<KhoaHoc, String> courseColumn, courseDescriptionColumn, coursePriceColumn,
            courseImageColumn, courseInstructorColumn, courseStatusColumn,
            courseStartDateColumn, courseEndDateColumn;
    @FXML
    private TableColumn<ThanhToan, String> thanhToanIdColumn, paymentDateColumn, paymentAmountColumn,
            paymentMethodColumn, paymentHocVienIDColumn, paymentKhoaHocIDColumn;

    @FXML
    private Button addUserBtn, editUserBtn, deleteUserBtn, toggleUserStatusBtn;
    @FXML
    private Button addCourseBtn, editCourseBtn, deleteCourseBtn, toggleCourseStatusBtn;
    @FXML
    private Button addPaymentBtn, editPaymentBtn, deletePaymentBtn;
    @FXML
    private Button manageApprovalsBtn;
    @FXML
    private VBox userTableContainer, courseTableContainer, paymentTableContainer;
    @FXML
    private TextField searchUserField, searchCourseField, searchPaymentField;
    @FXML
    private Button searchUserBtn, searchCourseBtn, searchPaymentBtn;

    private ObservableList<NguoiDung> users = FXCollections.observableArrayList();
    private ObservableList<KhoaHoc> courses = FXCollections.observableArrayList();
    private ObservableList<ThanhToan> payments = FXCollections.observableArrayList();
    private ObservableList<NguoiDung> allUsers = FXCollections.observableArrayList();
    private ObservableList<KhoaHoc> allCourses = FXCollections.observableArrayList();
    private ObservableList<ThanhToan> allPayments = FXCollections.observableArrayList();

    private CourseService courseService;
    private PaymentService paymentService;
    private static final String AVATAR_BASE_PATH = "/com/ntn/images/avatars/";
    private static final String COURSE_BASE_PATH = "/com/ntn/images/courses/";
    private static final String DEFAULT_COURSE_IMAGE = COURSE_BASE_PATH + "1.jpg";
    private static final String DEFAULT_AVATAR = AVATAR_BASE_PATH + "default.jpg";
    private static final String PLACEHOLDER_IMAGE = "https://via.placeholder.com/50";

    // Quản lý phiên (Session Management)
    private static String currentUserEmail = null;

    @FXML
    public void initialize() {
        // Khởi tạo services
        courseService = new CourseService();
        paymentService = new PaymentService();

        welcomeLabel.setText("Bảng Điều Khiển Quản Trị");
        loadUserAvatar();
        updateQuickStats();

        // Configure User TableColumns
        if (avatarColumn != null) {
            avatarColumn.setCellFactory(column -> new TableCell<>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String avatarPath, boolean empty) {
                    super.updateItem(avatarPath, empty);
                    imageView.setImage(null);
                    if (empty || avatarPath == null || avatarPath.trim().isEmpty()) {
                        setGraphic(loadImage(null, DEFAULT_AVATAR, true));
                    } else {
                        setGraphic(loadImage(avatarPath, DEFAULT_AVATAR, true));
                    }
                }
            });
            avatarColumn.setCellValueFactory(new PropertyValueFactory<>("avatar"));
        }
        if (hoColumn != null) {
            hoColumn.setCellValueFactory(new PropertyValueFactory<>("ho"));
        }
        if (tenColumn != null) {
            tenColumn.setCellValueFactory(new PropertyValueFactory<>("ten"));
        }
        if (emailColumn != null) {
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        }
        if (userStatusColumn != null) {
            userStatusColumn.setCellValueFactory(data
                    -> new SimpleStringProperty(data.getValue().isActive() ? "(Hoạt động)" : "(Vô hiệu)"));
        }
        if (loaiNguoiDungColumn != null) {
            loaiNguoiDungColumn.setCellValueFactory(data
                    -> new SimpleStringProperty(String.valueOf(data.getValue().getLoaiNguoiDungId())));
        }

        // Configure Course TableColumns
        if (courseIdColumn != null) {
            courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        }
        if (courseColumn != null) {
            courseColumn.setCellValueFactory(new PropertyValueFactory<>("tenKhoaHoc"));
        }
        if (courseDescriptionColumn != null) {
            courseDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        }
        if (coursePriceColumn != null) {
            coursePriceColumn.setCellValueFactory(data
                    -> new SimpleStringProperty(String.format("%.2f", data.getValue().getGia())));
        }
        if (courseImageColumn != null) {
            courseImageColumn.setCellFactory(column -> new TableCell<>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String imagePath, boolean empty) {
                    super.updateItem(imagePath, empty);
                    imageView.setImage(null);
                    if (empty || imagePath == null || imagePath.trim().isEmpty()) {
                        setGraphic(loadImage(null, DEFAULT_COURSE_IMAGE, false));
                    } else {
                        setGraphic(loadImage(imagePath, DEFAULT_COURSE_IMAGE, false));
                    }
                }
            });
            courseImageColumn.setCellValueFactory(new PropertyValueFactory<>("hinhAnh"));
        }
        if (courseInstructorColumn != null) {
            courseInstructorColumn.setCellValueFactory(new PropertyValueFactory<>("tenGiangVien"));
        }
        if (courseStatusColumn != null) {
            courseStatusColumn.setCellValueFactory(data
                    -> new SimpleStringProperty(data.getValue().isActive() ? "(Hoạt động)" : "(Vô hiệu)"));
        }
        if (courseStartDateColumn != null) {
            courseStartDateColumn.setCellValueFactory(data
                    -> new SimpleStringProperty(data.getValue().getNgayBatDau() != null
                            ? data.getValue().getNgayBatDau().toString() : "N/A"));
        }
        if (courseEndDateColumn != null) {
            courseEndDateColumn.setCellValueFactory(data
                    -> new SimpleStringProperty(data.getValue().getNgayKetThuc() != null
                            ? data.getValue().getNgayKetThuc().toString() : "N/A"));
        }

        // Configure Payment TableColumns
        if (thanhToanIdColumn != null) {
            thanhToanIdColumn.setCellValueFactory(data -> data.getValue().thanhToanIDProperty());
        }
        if (paymentDateColumn != null) {
            paymentDateColumn.setCellValueFactory(data -> data.getValue().ngayThanhToanProperty());
        }
        if (paymentAmountColumn != null) {
            paymentAmountColumn.setCellValueFactory(data -> data.getValue().soTienProperty());
        }
        if (paymentMethodColumn != null) {
            paymentMethodColumn.setCellValueFactory(data -> data.getValue().phuongThucProperty());
        }
        if (paymentHocVienIDColumn != null) {
            paymentHocVienIDColumn.setCellValueFactory(data -> data.getValue().hocVienIDProperty());
        }
        if (paymentKhoaHocIDColumn != null) {
            paymentKhoaHocIDColumn.setCellValueFactory(data -> data.getValue().khoaHocIDProperty());
        }

        // Bind data to tables
        if (userTable != null) {
            userTable.setItems(users);
        }
        if (courseTable != null) {
            courseTable.setItems(courses);
        }
        if (paymentTable != null) {
            paymentTable.setItems(payments);
        }

        // Set column resize policy
        if (userTable != null) {
            userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        if (courseTable != null) {
            courseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        if (paymentTable != null) {
            paymentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Bind search button actions
        if (searchUserBtn != null) {
            searchUserBtn.setOnAction(e -> handleSearchUser());
        }
        if (searchCourseBtn != null) {
            searchCourseBtn.setOnAction(e -> handleSearchCourse());
        }
        if (searchPaymentBtn != null) {
            searchPaymentBtn.setOnAction(e -> handleSearchPayment());
        }

        // Bind button actions
        if (manageCoursesBtn != null) {
            manageCoursesBtn.setOnAction(e -> loadManageCourses());
        }
        if (manageUsersBtn != null) {
            manageUsersBtn.setOnAction(e -> loadManageUsers());
        }
        if (managePaymentsBtn != null) {
            managePaymentsBtn.setOnAction(e -> loadManagePayments());
        }
        if (manageApprovalsBtn != null) {
            manageApprovalsBtn.setOnAction(e -> loadManageApprovals());
        }
        if (logoutBtn != null) {
            logoutBtn.setOnAction(e -> handleLogout());
        }

        if (toggleCourseStatusBtn != null) {
            toggleCourseStatusBtn.setOnAction(e -> toggleCourseStatus());
        }
        if (toggleUserStatusBtn != null) {
            toggleUserStatusBtn.setOnAction(e -> toggleUserStatus());
        }

        if (addUserBtn != null) {
            addUserBtn.setOnAction(e -> handleAddUser());
        }
        if (editUserBtn != null) {
            editUserBtn.setOnAction(e -> handleEditUser());
        }
        if (deleteUserBtn != null) {
            deleteUserBtn.setOnAction(e -> handleDeleteUser());
        }

        if (addCourseBtn != null) {
            addCourseBtn.setOnAction(e -> handleAddCourse());
        }
        if (editCourseBtn != null) {
            editCourseBtn.setOnAction(e -> handleEditCourse());
        }
        if (deleteCourseBtn != null) {
            deleteCourseBtn.setOnAction(e -> handleDeleteCourse());
        }

        if (addPaymentBtn != null) {
            addPaymentBtn.setOnAction(e -> handleAddPayment());
        }
        if (editPaymentBtn != null) {
            editPaymentBtn.setOnAction(e -> handleEditPayment());
        }
        if (deletePaymentBtn != null) {
            deletePaymentBtn.setOnAction(e -> handleDeletePayment());
        }

        // Load users by default
        loadManageUsers();
    }

    private ImageView loadImage(String imagePath, String defaultPath, boolean isAvatar) {
        ImageView imageView = new ImageView();
        Image image = null;
        try {
            String effectivePath;
            if (imagePath == null || imagePath.trim().isEmpty()) {
                effectivePath = defaultPath;
            } else {
                if (!imagePath.startsWith("http://") && !imagePath.startsWith("https://") && !imagePath.startsWith("/com/ntn/images/")) {
                    String basePath = isAvatar ? AVATAR_BASE_PATH : COURSE_BASE_PATH;
                    effectivePath = basePath + imagePath;
                } else {
                    effectivePath = imagePath;
                }
            }

            LOGGER.info("Attempting to load image: " + effectivePath);

            if (!effectivePath.equals(defaultPath)
                    && !effectivePath.toLowerCase().endsWith(".jpg")
                    && !effectivePath.toLowerCase().endsWith(".png")) {
                LOGGER.warning("Unsupported image format: " + effectivePath);
                image = loadDefaultImage(defaultPath);
            } else if (effectivePath.startsWith("http://") || effectivePath.startsWith("https://")) {
                image = new Image(effectivePath, 50, 50, true, true);
                if (image.isError()) {
                    LOGGER.warning("Failed to load image from URL: " + effectivePath);
                    image = loadDefaultImage(defaultPath);
                }
            } else {
                java.net.URL resource = getClass().getResource(effectivePath);
                if (resource == null) {
                    LOGGER.warning("Resource not found in classpath: " + effectivePath);
                    image = loadDefaultImage(defaultPath);
                } else {
                    image = new Image(resource.toString(), 50, 50, true, true);
                    if (image.isError()) {
                        LOGGER.warning("Failed to load image from classpath: " + effectivePath);
                        image = loadDefaultImage(defaultPath);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading image: " + (imagePath != null ? imagePath : "null"), e);
            image = loadDefaultImage(defaultPath);
        }
        imageView.setImage(image);
        return imageView;
    }

    private Image loadDefaultImage(String defaultPath) {
        try {
            java.net.URL defaultResource = getClass().getResource(defaultPath);
            if (defaultResource != null) {
                Image image = new Image(defaultResource.toString(), 50, 50, true, true);
                if (!image.isError()) {
                    return image;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading default image: " + defaultPath, e);
        }
        return new Image(PLACEHOLDER_IMAGE, 50, 50, true, true);
    }

    private void updateQuickStats() {
        try (Connection conn = Database.getConn()) {
            PreparedStatement userStmt = conn.prepareStatement("SELECT COUNT(*) FROM nguoidung");
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                totalUsersLabel.setText("Tổng: " + userRs.getInt(1));
            }

            PreparedStatement courseStmt = conn.prepareStatement("SELECT COUNT(*) FROM khoahoc");
            ResultSet courseRs = courseStmt.executeQuery();
            if (courseRs.next()) {
                totalCoursesLabel.setText("Tổng: " + courseRs.getInt(1));
            }

            PreparedStatement paymentStmt = conn.prepareStatement("SELECT COUNT(*) FROM lichsu_thanhtoan");
            ResultSet paymentRs = paymentStmt.executeQuery();
            if (paymentRs.next()) {
                totalPaymentsLabel.setText("Tổng: " + paymentRs.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating stats", e);
            showAlert("Lỗi", "Không thể cập nhật số liệu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUserAvatar() {
        String userEmail = getCurrentUserEmail();
        String avatarPath = null;
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("SELECT avatar FROM nguoidung WHERE email = ?")) {
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                avatarPath = rs.getString("avatar");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error loading avatar for email: " + userEmail, e);
            showAlert("Lỗi", "Không thể tải avatar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        if (avatarPath == null) {
            avatarPath = DEFAULT_AVATAR;
        }
        userAvatar.setImage(loadImage(avatarPath, DEFAULT_AVATAR, true).getImage());
    }

    String getCurrentUserEmail() {
        return currentUserEmail != null ? currentUserEmail : "admin1@example.com";
    }

    public static void setCurrentUserEmail(String email) {
        currentUserEmail = email;
    }

    @FXML
    private void loadManageCourses() {
        userTableContainer.setVisible(false);
        userTableContainer.setManaged(false);
        paymentTableContainer.setVisible(false);
        paymentTableContainer.setManaged(false);
        courseTableContainer.setVisible(true);
        courseTableContainer.setManaged(true);

        try {
            allCourses.setAll(courseService.getAllActiveCourses());
            courses.setAll(allCourses);
            courseTable.refresh();
            LOGGER.info("Loaded " + courses.size() + " courses.");
            if (searchCourseField != null) {
                searchCourseField.clear();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error loading courses", e);
            showAlert("Lỗi SQL", "Không thể tải danh sách khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void loadManageUsers() {
        courseTableContainer.setVisible(false);
        courseTableContainer.setManaged(false);
        paymentTableContainer.setVisible(false);
        paymentTableContainer.setManaged(false);
        userTableContainer.setVisible(true);
        userTableContainer.setManaged(true);

        users.clear();
        allUsers.clear();
        try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, ho, ten, email, active, avatar, mat_khau, loai_nguoi_dung_id FROM nguoidung"); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                NguoiDung user = new NguoiDung(
                        rs.getInt("id"),
                        rs.getString("ho"),
                        rs.getString("ten"),
                        rs.getString("email"),
                        rs.getString("mat_khau"),
                        rs.getInt("loai_nguoi_dung_id"),
                        rs.getBoolean("active"),
                        rs.getString("avatar")
                );
                users.add(user);
                allUsers.add(user);
            }
            userTable.refresh();
            if (searchUserField != null) {
                searchUserField.clear();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading users", e);
            showAlert("Lỗi", "Không thể tải danh sách người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void loadManagePayments() {
        userTableContainer.setVisible(false);
        userTableContainer.setManaged(false);
        courseTableContainer.setVisible(false);
        courseTableContainer.setManaged(false);
        paymentTableContainer.setVisible(true);
        paymentTableContainer.setManaged(true);

        payments.clear();
        allPayments.clear();
        try {
            allPayments.setAll(paymentService.getAllPayments());
            payments.setAll(allPayments);
            paymentTable.refresh();
            LOGGER.info("Loaded " + payments.size() + " payment records");
            if (searchPaymentField != null) {
                searchPaymentField.clear();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading payment history", e);
            showAlert("Lỗi", "Không thể tải danh sách lịch sử thanh toán: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void loadManageApprovals() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/admin_approval.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Quản lý Xét Duyệt");
            stage.showAndWait();
            LOGGER.info("Opened approval management window.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening approval management form", e);
            showAlert("Lỗi", "Không thể mở form quản lý xét duyệt: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void toggleCourseStatus() {
        KhoaHoc selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("UPDATE khoahoc SET active = NOT active WHERE id = ?")) {
                stmt.setInt(1, selectedCourse.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    loadManageCourses();
                    updateQuickStats();
                    showAlert("Thành công", "Đã thay đổi trạng thái khóa học!", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error toggling course status", e);
                showAlert("Lỗi", "Không thể thay đổi trạng thái khóa học: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một khóa học để thay đổi trạng thái.",
                    Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void toggleUserStatus() {
        NguoiDung selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("UPDATE nguoidung SET active = NOT active WHERE id = ?")) {
                stmt.setInt(1, selectedUser.getId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    loadManageUsers();
                    updateQuickStats();
                    showAlert("Thành công", "Đã thay đổi trạng thái người dùng!", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error toggling user status", e);
                showAlert("Lỗi", "Không thể thay đổi trạng thái người dùng: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một người dùng để thay đổi trạng thái.",
                    Alert.AlertType.WARNING);
        }
    }

    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/add_user.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm Người Dùng");
            stage.showAndWait();
            loadManageUsers();
            updateQuickStats();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening add user form", e);
            showAlert("Lỗi", "Không thể mở form thêm người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditUser() {
        NguoiDung selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/edit_user.fxml"));
                Parent root = loader.load();
                EditUserController editUserController = loader.getController();
                editUserController.setUser(selectedUser);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Sửa Người Dùng");
                stage.showAndWait();
                loadManageUsers();
                updateQuickStats();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error opening edit user form", e);
                showAlert("Lỗi", "Không thể mở form sửa người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một người dùng để sửa.", Alert.AlertType.WARNING);
        }
    }

    private void handleDeleteUser() {
        NguoiDung selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setContentText("Bạn có chắc chắn muốn xóa người dùng " + selectedUser.getFullName() + "?");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM nguoidung WHERE id = ?")) {
                    stmt.setInt(1, selectedUser.getId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        loadManageUsers();
                        updateQuickStats();
                        showAlert("Thành công", "Đã xóa người dùng!", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error deleting user", e);
                    showAlert("Lỗi", "Không thể xóa người dùng: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một người dùng để xóa.", Alert.AlertType.WARNING);
        }
    }

    private void handleAddCourse() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/add_course.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            java.net.URL cssResource = getClass().getResource("/com/ntn/views/styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }
            stage.setScene(scene);
            stage.setTitle("Thêm Khóa Học");
            stage.showAndWait();
            loadManageCourses();
            updateQuickStats();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening add course form", e);
            showAlert("Lỗi", "Không thể mở form thêm khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditCourse() {
        KhoaHoc selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/edit_course.fxml"));
                Parent root = loader.load();
                EditCourseController editCourseController = loader.getController();
                editCourseController.setCourse(selectedCourse);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Sửa Khóa Học");
                stage.showAndWait();
                loadManageCourses();
                updateQuickStats();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error opening edit course form", e);
                showAlert("Lỗi", "Không thể mở form sửa khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một khóa học để sửa.", Alert.AlertType.WARNING);
        }
    }

    private void handleDeleteCourse() {
        KhoaHoc selectedCourse = courseTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setContentText("Bạn có chắc chắn muốn xóa khóa học " + selectedCourse.getTenKhoaHoc() + "?");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = Database.getConn(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM khoahoc WHERE id = ?")) {
                    stmt.setInt(1, selectedCourse.getId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        loadManageCourses();
                        updateQuickStats();
                        showAlert("Thành công", "Đã xóa khóa học!", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error deleting course", e);
                    showAlert("Lỗi", "Không thể xóa khóa học: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một khóa học để xóa.", Alert.AlertType.WARNING);
        }
    }

    private void handleAddPayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/add_payment.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm Thanh Toán");
            stage.showAndWait();
            loadManagePayments();
            updateQuickStats();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening add payment form", e);
            showAlert("Lỗi", "Không thể mở form thêm thanh toán: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditPayment() {
        ThanhToan selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/edit_payment.fxml"));
                Parent root = loader.load();
                EditPaymentController editPaymentController = loader.getController();
                editPaymentController.setPayment(selectedPayment);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Sửa Thanh Toán");
                stage.showAndWait();
                loadManagePayments();
                updateQuickStats();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error opening edit payment form", e);
                showAlert("Lỗi", "Không thể mở form sửa thanh toán: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một thanh toán để sửa.", Alert.AlertType.WARNING);
        }
    }

    private void handleDeletePayment() {
        ThanhToan selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận xóa");
            confirmAlert.setContentText("Bạn có chắc chắn muốn xóa lịch sử thanh toán ID "
                    + selectedPayment.getThanhToanID() + "?");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    boolean success = paymentService.deletePayment(selectedPayment.getThanhToanID());
                    if (success) {
                        loadManagePayments();
                        updateQuickStats();
                        showAlert("Thành công", "Đã xóa lịch sử thanh toán!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Thông báo", "Không có bản ghi nào được xóa.", Alert.AlertType.INFORMATION);
                    }
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error deleting payment history", e);
                    showAlert("Lỗi", "Không thể xóa lịch sử thanh toán: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Cảnh báo", "Vui lòng chọn một lịch sử thanh toán để xóa.", Alert.AlertType.WARNING);
        }
    }

    private void handleLogout() {
        try {
            // Lấy stage hiện tại (Dashboard Admin)
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();

            // Tải giao diện đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ntn/views/login.fxml"));
            Parent root = loader.load();

            // Tạo stage mới cho giao diện đăng nhập
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Đăng Nhập");

            // Xóa trạng thái phiên (reset email người dùng hiện tại)
            setCurrentUserEmail(null);
            LOGGER.info("User logged out successfully. Session cleared.");

            // Đóng cửa sổ Dashboard Admin
            currentStage.close();

            // Hiển thị cửa sổ đăng nhập
            loginStage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error logging out", e);
            showAlert("Lỗi", "Không thể đăng xuất: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleSearchUser() {
        String keyword = searchUserField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            users.setAll(allUsers);
            return;
        }
        ObservableList<NguoiDung> filteredUsers = allUsers.stream()
                .filter(user -> user.getHo().toLowerCase().contains(keyword)
                || user.getTen().toLowerCase().contains(keyword)
                || user.getEmail().toLowerCase().contains(keyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        users.setAll(filteredUsers);
    }

    private void handleSearchCourse() {
        String keyword = searchCourseField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            courses.setAll(allCourses);
            return;
        }
        ObservableList<KhoaHoc> filteredCourses = allCourses.stream()
                .filter(course -> course.getTenKhoaHoc().toLowerCase().contains(keyword)
                || course.getMoTa().toLowerCase().contains(keyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        courses.setAll(filteredCourses);
    }

    private void handleSearchPayment() {
        String keyword = searchPaymentField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            payments.setAll(allPayments);
            return;
        }
        ObservableList<ThanhToan> filteredPayments = allPayments.stream()
                .filter(payment -> payment.getThanhToanID().toLowerCase().contains(keyword)
                || payment.getHocVienID().toLowerCase().contains(keyword)
                || payment.getKhoaHocID().toLowerCase().contains(keyword)
                || payment.getPhuongThuc().toLowerCase().contains(keyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        payments.setAll(filteredPayments);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
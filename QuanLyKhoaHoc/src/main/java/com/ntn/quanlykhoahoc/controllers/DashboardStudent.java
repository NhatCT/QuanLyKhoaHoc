package com.ntn.quanlykhoahoc;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.models.KhoaHoc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.sql.*;

public class DashboardStudent {
    @FXML
    private TableView<KhoaHoc> courseTable;
    @FXML
    private TableColumn<KhoaHoc, String> nameColumn;
    @FXML
    private TableColumn<KhoaHoc, String> instructorColumn;
    @FXML
    private TableColumn<KhoaHoc, String> priceColumn;
    @FXML
    private TableColumn<KhoaHoc, String> imageColumn;
    @FXML
    private TableColumn<KhoaHoc, String> actionColumn;
    @FXML
    private Pagination pagination;

    private ObservableList<KhoaHoc> khoaHocList = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 4;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().tenKhoaHocProperty());
        instructorColumn.setCellValueFactory(cellData -> cellData.getValue().giangVienProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().giaProperty());

        // Hiển thị hình ảnh khóa học
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().hinhAnhProperty());
        imageColumn.setCellFactory(getImageCellFactory());

        // Thêm cột "Hành động" với nút "Vào học ngay"
        actionColumn.setCellFactory(getActionCellFactory());

        loadKhoaHoc();
        setupPagination();
    }

    private void loadKhoaHoc() {
        String query = "SELECT k.id, k.ten_khoa_hoc, g.ho_ten AS giang_vien, k.gia, k.hinh_anh " +
                       "FROM khoahoc k JOIN nguoidung g ON k.giang_vien_id = g.id";

        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                khoaHocList.add(new KhoaHoc(
                    rs.getInt("id"),
                    rs.getString("ten_khoa_hoc"),
                    rs.getString("giang_vien"),
                    rs.getString("gia"),
                    rs.getString("hinh_anh") // Đường dẫn ảnh từ DB
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) khoaHocList.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(this::createPage);
    }

    private TableView<KhoaHoc> createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, khoaHocList.size());
        courseTable.setItems(FXCollections.observableArrayList(khoaHocList.subList(fromIndex, toIndex)));
        return courseTable;
    }

    private Callback<TableColumn<KhoaHoc, String>, TableCell<KhoaHoc, String>> getImageCellFactory() {
        return param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(getClass().getResource("/com/ntn/images/" + imagePath).toExternalForm());
                        imageView.setImage(image);
                        imageView.setFitWidth(80);
                        imageView.setFitHeight(60);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        };
    }

    private Callback<TableColumn<KhoaHoc, String>, TableCell<KhoaHoc, String>> getActionCellFactory() {
        return param -> new TableCell<>() {
            private final Button button = new Button("Vào học ngay");

            {
                button.setOnAction(event -> {
                    KhoaHoc selectedCourse = getTableView().getItems().get(getIndex());
                    showAlert("Khóa học", "Bạn đã chọn khóa học: " + selectedCourse.getTenKhoaHoc(), Alert.AlertType.INFORMATION);
                });
                button.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white; -fx-padding: 5 10;");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(button);
                }
            }
        };
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

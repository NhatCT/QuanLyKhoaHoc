package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.database.Database;
import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import com.ntn.quanlykhoahoc.services.PaymentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminApprovalController {
    private static final Logger LOGGER = Logger.getLogger(AdminApprovalController.class.getName());

    @FXML private TableView<KhoaHocHocVien> approvalTable;
    @FXML private TableColumn<KhoaHocHocVien, String> idColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> hocVienColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> khoaHocColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> ngayDangKyColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> trangThaiColumn;
    @FXML private TableColumn<KhoaHocHocVien, Void> actionColumn;
    @FXML private Button refreshButton;

    private PaymentService paymentService = new PaymentService();
    private ObservableList<KhoaHocHocVien> pendingRecords = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        hocVienColumn.setCellValueFactory(data -> {
            try {
                String tenHocVien = getTenHocVien(data.getValue().getHocVienID());
                return new SimpleStringProperty(tenHocVien);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lấy tên học viên: " + e.getMessage(), e);
                return new SimpleStringProperty("N/A");
            }
        });
        khoaHocColumn.setCellValueFactory(data -> {
            try {
                String tenKhoaHoc = getTenKhoaHoc(data.getValue().getKhoaHocID());
                return new SimpleStringProperty(tenKhoaHoc);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi lấy tên khóa học: " + e.getMessage(), e);
                return new SimpleStringProperty("N/A");
            }
        });
        ngayDangKyColumn.setCellValueFactory(data -> {
            String ngayDangKy = data.getValue().getNgayDangKy();
            if (ngayDangKy != null && !ngayDangKy.isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(ngayDangKy, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Lỗi định dạng ngày đăng ký: " + ngayDangKy, e);
                    return new SimpleStringProperty(ngayDangKy);
                }
            }
            return new SimpleStringProperty("N/A");
        });
        trangThaiColumn.setCellValueFactory(data -> data.getValue().trangThaiProperty());

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button("Duyệt");
            private final Button rejectButton = new Button("Từ chối");
            private final HBox buttons = new HBox(10, approveButton, rejectButton);

            {
                approveButton.setOnAction(event -> {
                    KhoaHocHocVien record = getTableView().getItems().get(getIndex());
                    handleAction(record, "APPROVED");
                });
                rejectButton.setOnAction(event -> {
                    KhoaHocHocVien record = getTableView().getItems().get(getIndex());
                    handleAction(record, "REJECTED");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        approvalTable.setItems(pendingRecords);
        loadPendingRecords();
    }

    private void handleAction(KhoaHocHocVien record, String trangThai) {
        try {
            boolean success = paymentService.updateKhoaHocHocVienStatus(record.getId(), trangThai);
            if (success) {
                showAlert("Thành công", "Yêu cầu đã được " + (trangThai.equals("APPROVED") ? "duyệt" : "từ chối") + "!", Alert.AlertType.INFORMATION);
                loadPendingRecords();
            } else {
                showAlert("Lỗi", "Không thể xử lý yêu cầu.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xử lý yêu cầu: " + e.getMessage(), e);
            showAlert("Lỗi", "Không thể xử lý yêu cầu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadPendingRecords() {
        try {
            pendingRecords.setAll(paymentService.getPendingKhoaHocHocVien());
            LOGGER.info("Đã tải " + pendingRecords.size() + " bản ghi PENDING.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải bản ghi PENDING: " + e.getMessage(), e);
            showAlert("Lỗi", "Không thể tải danh sách xét duyệt: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getTenHocVien(int hocVienID) throws SQLException {
        String sql = "SELECT n.ho, n.ten FROM hocvien h JOIN nguoidung n ON h.nguoiDungID = n.id WHERE h.id = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hocVienID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ho") + " " + rs.getString("ten");
                }
                return "N/A";
            }
        }
    }

    private String getTenKhoaHoc(int khoaHocID) throws SQLException {
        String sql = "SELECT ten_khoa_hoc FROM khoahoc WHERE id = ?";
        try (Connection conn = Database.getConn();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, khoaHocID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ten_khoa_hoc");
                }
                return "N/A";
            }
        }
    }

    @FXML
    private void refreshTable() {
        loadPendingRecords();
        showAlert("Thành công", "Danh sách đã được làm mới.", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.pojo.KhoaHocHocVien;
import com.ntn.quanlykhoahoc.services.PaymentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.logging.Logger;

public class AdminApprovalController {
    private static final Logger LOGGER = Logger.getLogger(AdminApprovalController.class.getName());
    private final PaymentService paymentService = new PaymentService();

    @FXML private TableView<KhoaHocHocVien> approvalTable;
    @FXML private TableColumn<KhoaHocHocVien, String> hocVienColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> khoaHocColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> ngayDangKyColumn;
    @FXML private TableColumn<KhoaHocHocVien, String> trangThaiColumn;
    @FXML private TableColumn<KhoaHocHocVien, Void> actionColumn;

    public void initialize() {
        hocVienColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getHocVienID())));
        khoaHocColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getKhoaHocID())));
        ngayDangKyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNgayDangKy()));
        trangThaiColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrangThai()));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveButton = new Button("Duyệt");
            private final Button rejectButton = new Button("Từ chối");

            {
                approveButton.setOnAction(e -> {
                    KhoaHocHocVien record = getTableView().getItems().get(getIndex());
                    try {
                        paymentService.updateKhoaHocHocVienStatus(record.getId(), "APPROVED");
                        showInfoAlert("Thành công", "Đã duyệt tham gia khóa học.");
                        refreshTable();
                    } catch (SQLException ex) {
                        showErrorAlert("Lỗi", "Không thể duyệt: " + ex.getMessage());
                    }
                });
                rejectButton.setOnAction(e -> {
                    KhoaHocHocVien record = getTableView().getItems().get(getIndex());
                    try {
                        paymentService.updateKhoaHocHocVienStatus(record.getId(), "REJECTED");
                        showInfoAlert("Thành công", "Đã từ chối tham gia khóa học.");
                        refreshTable();
                    } catch (SQLException ex) {
                        showErrorAlert("Lỗi", "Không thể từ chối: " + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(10, approveButton, rejectButton);
                    setGraphic(pane);
                }
            }
        });

        refreshTable();
    }

    @FXML
    private void refreshTable() {
        try {
            ObservableList<KhoaHocHocVien> records = FXCollections.observableArrayList(paymentService.getPendingKhoaHocHocVien());
            approvalTable.setItems(records);
        } catch (SQLException e) {
            LOGGER.severe("Lỗi tải danh sách xét duyệt: " + e.getMessage());
            showErrorAlert("Lỗi", "Không thể tải danh sách: " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        if (approvalTable != null && approvalTable.getScene() != null) {
            Stage stage = (Stage) approvalTable.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        } else {
            LOGGER.warning("Không thể đóng cửa sổ: TableView hoặc Scene là null");
        }
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
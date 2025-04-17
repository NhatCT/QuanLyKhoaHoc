package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.pojo.LichHoc;
import com.ntn.quanlykhoahoc.services.TimetableService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimetableController {

    @FXML private TextArea upcomingSessionsTextArea;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button filterButton;
    @FXML private Button clearFilterButton;
    @FXML private TableView<LichHoc> timetableTable;
    @FXML private TableColumn<LichHoc, String> tenKhoaHocColumn;
    @FXML private TableColumn<LichHoc, LocalDate> ngayHocColumn;
    @FXML private TableColumn<LichHoc, LocalTime> gioBatDauColumn;
    @FXML private TableColumn<LichHoc, LocalTime> gioKetThucColumn;
    @FXML private TableColumn<LichHoc, String> giangVienColumn;
    @FXML private TableColumn<LichHoc, String> lienKetColumn;
    @FXML private TableColumn<LichHoc, String> statusColumn;

    private static final Logger LOGGER = Logger.getLogger(TimetableController.class.getName());
    private final TimetableService timetableService = new TimetableService();
    private final ObservableList<LichHoc> timetableList = FXCollections.observableArrayList();
    private List<LichHoc> fullTimetableList;
    private HostServices hostServices; // Thêm HostServices

    // Thêm setter cho HostServices
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        timetableTable.setItems(timetableList);

        // Initialize ComboBox items
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Tên Khóa Học (A-Z)", "Tên Khóa Học (Z-A)",
                "Ngày Học (Sớm nhất trước)", "Ngày Học (Muộn nhất trước)",
                "Giờ Bắt Đầu (Sớm nhất trước)", "Giờ Bắt Đầu (Muộn nhất trước)"
        ));
        sortComboBox.getSelectionModel().selectFirst();
        sortComboBox.setOnAction(event -> sortTimetable());

        loadTimetable();
        checkUpcomingSessions();
    }

    private void setupTableColumns() {
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
    }

    private void loadTimetable() {
        try {
            String email = SessionManager.getLoggedInEmail();
            fullTimetableList = timetableService.getTimetableForStudent(email);
            timetableList.setAll(fullTimetableList);
            sortTimetable();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải thời khóa biểu", e);
            showAlert("Lỗi", "Không thể tải thời khóa biểu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setTimetableData(List<LichHoc> schedule) {
        fullTimetableList = schedule;
        timetableList.setAll(fullTimetableList);
        sortTimetable();
        upcomingSessionsTextArea.setText("Hiển thị lịch học cho khóa học cụ thể.");
        startDatePicker.setDisable(true);
        endDatePicker.setDisable(true);
        filterButton.setDisable(true);
        clearFilterButton.setDisable(true);
    }

    @FXML
    private void filterTimetable() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert("Cảnh báo", "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!", Alert.AlertType.WARNING);
            return;
        }

        if (startDate.isAfter(endDate)) {
            showAlert("Cảnh báo", "Ngày bắt đầu không được sau ngày kết thúc!", Alert.AlertType.WARNING);
            return;
        }

        try {
            String email = SessionManager.getLoggedInEmail();
            List<LichHoc> filteredList = timetableService.getTimetableForStudentByDateRange(email, startDate, endDate);
            timetableList.setAll(filteredList);
            sortTimetable();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lọc thời khóa biểu", e);
            showAlert("Lỗi", "Không thể lọc thời khóa biểu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void clearFilter() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        timetableList.setAll(fullTimetableList);
        sortTimetable();
    }

    private void sortTimetable() {
        String sortOption = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortOption == null) return;

        Comparator<LichHoc> comparator = switch (sortOption) {
            case "Tên Khóa Học (A-Z)" -> Comparator.comparing(LichHoc::getTenKhoaHoc);
            case "Tên Khóa Học (Z-A)" -> Comparator.comparing(LichHoc::getTenKhoaHoc, Comparator.reverseOrder());
            case "Ngày Học (Sớm nhất trước)" -> Comparator.comparing(LichHoc::getNgayHoc)
                    .thenComparing(LichHoc::getGioBatDau);
            case "Ngày Học (Muộn nhất trước)" -> Comparator.comparing(LichHoc::getNgayHoc, Comparator.reverseOrder())
                    .thenComparing(LichHoc::getGioBatDau, Comparator.reverseOrder());
            case "Giờ Bắt Đầu (Sớm nhất trước)" -> Comparator.comparing(LichHoc::getGioBatDau);
            case "Giờ Bắt Đầu (Muộn nhất trước)" -> Comparator.comparing(LichHoc::getGioBatDau, Comparator.reverseOrder());
            default -> Comparator.comparing(LichHoc::getNgayHoc);
        };

        timetableList.sort(comparator);
    }

    private void checkUpcomingSessions() {
        try {
            String email = SessionManager.getLoggedInEmail();
            List<LichHoc> timetable = timetableService.getTimetableForStudent(email);
            StringBuilder upcomingSessions = new StringBuilder();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            for (LichHoc lichHoc : timetable) {
                LocalDateTime sessionStart = LocalDateTime.of(lichHoc.getNgayHoc(), lichHoc.getGioBatDau());
                long hoursUntilStart = java.time.Duration.between(now, sessionStart).toHours();
                if (hoursUntilStart > 0 && hoursUntilStart <= 24) {
                    upcomingSessions.append("Buổi học: ").append(lichHoc.getTenKhoaHoc())
                            .append("\nNgày: ").append(lichHoc.getNgayHoc().format(dateFormatter))
                            .append("\nGiờ: ").append(lichHoc.getGioBatDau().format(timeFormatter))
                            .append(" - ").append(lichHoc.getGioKetThuc().format(timeFormatter))
                            .append("\nGiảng viên: ").append(lichHoc.getGiangVien())
                            .append("\nLink: ").append(lichHoc.getLienKet() != null ? lichHoc.getLienKet() : "Chưa có")
                            .append("\n\n");
                }
            }

            if (upcomingSessions.length() > 0) {
                upcomingSessionsTextArea.setText(upcomingSessions.toString());
            } else {
                upcomingSessionsTextArea.setText("Không có buổi học nào trong 24 giờ tới.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi kiểm tra lịch học sắp tới", e);
            upcomingSessionsTextArea.setText("Lỗi khi kiểm tra lịch học sắp tới.");
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
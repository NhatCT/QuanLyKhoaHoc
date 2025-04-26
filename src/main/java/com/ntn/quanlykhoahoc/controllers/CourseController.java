/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.pojo.BaiTap;
import com.ntn.quanlykhoahoc.pojo.CauHoi;
import com.ntn.quanlykhoahoc.pojo.HocVien_BaiTap;
import com.ntn.quanlykhoahoc.pojo.KhoaHoc;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import com.ntn.quanlykhoahoc.services.CourseService;
import com.ntn.quanlykhoahoc.services.ExerciseServices;
import com.ntn.quanlykhoahoc.services.QuestionServices;
import com.ntn.quanlykhoahoc.services.SubmitServices;
import com.ntn.quanlykhoahoc.services.UserService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import java.io.IOException;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;

/**
 * FXML Controller class
 *
 * @author ADMIN
 */
public class CourseController implements Initializable {

    @FXML
    Button btnLamBai1;
    @FXML
    Text txtKhoaHoc;
    @FXML
    Text txtTienTrinh;
    @FXML
    VBox vboxDanhSachBaiTap;
    @FXML
    Button btnClose;
    @FXML
    Text progressText;
    @FXML
    ProgressBar progressBar;

    private int idKhoaHoc;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void loadBaiTaiTheoKhoaHocID() throws SQLException {
        // set ten khoa hoc

        UserService userService = new UserService();
        String email = SessionManager.getLoggedInEmail();
        List<NguoiDung> allUsers = userService.getAllUsers();
        NguoiDung user = allUsers.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        int hocVienID = userService.getHocVienIDFromNguoiDung(user.getId());

        ExerciseServices exerciseServices = new ExerciseServices();
        double tatCaBT = (double) exerciseServices.getBaiTapsTheoKhoaHocID(idKhoaHoc).size();
        int baiTapDaLam = 0;
        ExerciseServices bt = new ExerciseServices();
        List<BaiTap> ds = bt.getBaiTapsTheoKhoaHocID(idKhoaHoc);
        for (BaiTap bt2 : ds) {
            if (exerciseServices.countSubbmitted(idKhoaHoc, hocVienID, bt2.getId()).size() == 1) {
                baiTapDaLam++;
            }
        }

        double c = (baiTapDaLam / tatCaBT) * 100;
        int phanTramTienTrinh = (int) c;

        progressBar.setStyle("-fx-accent: green;"); // Đặt màu sắc thanh tiến trình

        // Tạo Text để hiển thị giá trị phần trăm tiến trình
        // Cập nhật tiến trình theo giá trị từ 0 đến 100
        progressBar.setProgress(phanTramTienTrinh / 100.0);

        // Hiển thị phần trăm tiến trình
        progressText.setText("Tiến trình: " + phanTramTienTrinh + "%");

        try {
            CourseService courseService = new CourseService();

            KhoaHoc khoaHoc = courseService.getCourseID(idKhoaHoc).get(0);
            String tenKhoaHoc = khoaHoc.getTenKhoaHoc();
            this.txtKhoaHoc.setText(tenKhoaHoc);

        } catch (SQLException ex) {
            Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
//            ExerciseServices bt3 = new ExerciseServices();
            List<BaiTap> ds1 = exerciseServices.getBaiTapsTheoKhoaHocID(idKhoaHoc);
            for (BaiTap b : ds1) {
                HBox h = taoHangBaiTap(b.getTenBaiTap(), b.getDeadline(), b.getId());
                vboxDanhSachBaiTap.getChildren().add(h); // ✅ đúng tên biến
            }
        } catch (SQLException ex) {
            Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private HBox taoHangBaiTap(String ten, Timestamp deadline, int baiTapID) throws SQLException {
        Label lblTen = new Label(ten);
        lblTen.setPrefWidth(200);
        // Chuyển Timestamp thành LocalDateTime
        LocalDateTime deadline1 = deadline.toLocalDateTime();

        // Định dạng LocalDateTime thành chuỗi theo định dạng ngày giờ
        String deadlineText = deadline1.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        Label lblDeadline = new Label("Hạn nộp: " + deadlineText);
        lblDeadline.setPrefWidth(220);
        // submitted, kiem tra deadline
        UserService userService = new UserService();
        String email = SessionManager.getLoggedInEmail();
        List<NguoiDung> allUsers = userService.getAllUsers();
        NguoiDung user = allUsers.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        int hocVienID = userService.getHocVienIDFromNguoiDung(user.getId());
        SubmitServices sb = new SubmitServices();
        List<HocVien_BaiTap> hv_bt = sb.getSubmitHocVienIDBaiTapID(hocVienID, baiTapID);

        Button btnLamBai;

        Date hienTai = Date.from(Instant.now());

        if ((hv_bt.size() == 1) || (hienTai.after(deadline))) {
            btnLamBai = new Button("-----");
            btnLamBai.setPrefWidth(120);
            btnLamBai.setOnAction(e -> {
                daLamBai();
            });

        } else {
            btnLamBai = new Button("Làm bài");
            btnLamBai.setPrefWidth(120);
//        SessionManager ses = new SessionManager();

            btnLamBai.setOnAction(e -> {
                try {
                    vaoTrangBaiTap(baiTapID);
                } catch (SQLException ex) {
                    Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        HBox hbox = new HBox(20, lblTen, lblDeadline, btnLamBai);
        hbox.setPadding(new Insets(8));
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ccc; -fx-border-radius: 5;");
        return hbox;

    }

    public void vaoTrangBaiTap(int baiTapid) throws SQLException {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/ntn/views/exercise.fxml"));
            Parent root = fxmlLoader.load(); // Load FXML trước

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            ExerciseController controler = fxmlLoader.getController();
            controler.setBaiTapID(baiTapid);
//        loc theo baitapID

            controler.loadCauHoi();
            controler.load();
            controler.khoiPhucCauHoi();
            controler.startTimer();
            Stage currentStage = (Stage) btnClose.getScene().getWindow();
            currentStage.close();

//            controler.loadTungCau();
        } catch (IOException ex) {
            Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
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

    public void daLamBai() {
    }

    public void setIdKhoaHoc(int idKhoaHoc) {
        this.idKhoaHoc = idKhoaHoc;
    }

}

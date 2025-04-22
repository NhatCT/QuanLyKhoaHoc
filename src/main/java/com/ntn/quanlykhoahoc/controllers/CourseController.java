/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.App;
import com.ntn.quanlykhoahoc.pojo.BaiTap;
import com.ntn.quanlykhoahoc.services.ExerciseServices;
import com.ntn.quanlykhoahoc.session.SessionManager;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
    VBox vboxDanhSachBaiTap;

    private int idKhoaHoc;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
        public void loadBaiTaiTheoKhoaHocID() {
        try {
            ExerciseServices bt = new ExerciseServices();
            List<BaiTap> ds = bt.getBaiTapTheoKhoaHocID(idKhoaHoc);
            for (BaiTap b : ds) {
                HBox h = taoHangBaiTap(b.getTenBaiTap(), b.getDeadline(), b.getId());
                vboxDanhSachBaiTap.getChildren().add(h); // ✅ đúng tên biến
            }
        } catch (SQLException ex) {
            Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private HBox taoHangBaiTap(String ten, Date deadline, int baiTapID) {
        Label lblTen = new Label(ten);
        lblTen.setPrefWidth(200);

        String deadlineText = (deadline != null)
                ? new SimpleDateFormat("dd/MM/yyyy").format(deadline)
                : "Không có hạn";

        Label lblDeadline = new Label("Hạn nộp: " + deadlineText);
        lblDeadline.setPrefWidth(220);
        

        Button btnLamBai = new Button("Làm bài");
        btnLamBai.setPrefWidth(120);
        SessionManager ses = new SessionManager();
        

        btnLamBai.setOnAction(e -> {
            try {
                vaoTrangBaiTap(baiTapID);
            } catch (SQLException ex) {
                Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
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
            
            
            
//            controler.loadTungCau();
        } catch (IOException ex) {
            Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setIdKhoaHoc(int idKhoaHoc) {
        this.idKhoaHoc = idKhoaHoc;
    }

}

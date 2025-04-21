/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.ntn.quanlykhoahoc.controllers;

import com.ntn.quanlykhoahoc.pojo.CauHoi;
import com.ntn.quanlykhoahoc.pojo.DapAn;
import com.ntn.quanlykhoahoc.pojo.NguoiDung;
import com.ntn.quanlykhoahoc.services.ChoiceServices;
import com.ntn.quanlykhoahoc.services.QuestionServices;
import com.ntn.quanlykhoahoc.services.SubmitServices;
import com.ntn.quanlykhoahoc.services.UserService;
import com.ntn.quanlykhoahoc.session.SessionManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import java.util.prefs.Preferences;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ExerciseController implements Initializable {

    @FXML
    private VBox radioButtonsContainer;  // Dùng VBox hoặc HBox để chứa các radio button động
    @FXML
    private Text txtContent;
    @FXML
    private Text dem;
    private Map<Integer, String> cauHoiMap = new HashMap<>();  // Lưu lựa chọn của người dùng
    private List<RadioButton> radioButtons = new ArrayList<>();  // Danh sách radio button tạo động
    private List<CauHoi> cauhoi;
    private int currentIdx = 0;
    private int baiTapID;
    private int ketQua = 0;
    private boolean is_submit = true;
    @FXML
    private Text timerText;
    @FXML
    private Button btnClose;
    private int minutes = 24;
    private int seconds = 31;
    private static final String TIMER_KEY = "remainingTime4";
    private static final String QUESTION_KEY = "question1_";
    private static Preferences preferences = Preferences.userRoot().node(CourseController.class.getName());

    // Phương thức để load câu hỏi và đáp án, xử lý tạo động radio buttons
    public void loadCauHoi() {
        QuestionServices loadCauHoi = new QuestionServices();
        try {
            this.cauhoi = loadCauHoi.getCauHoiTheoBaiTapID(baiTapID);
        } catch (SQLException ex) {
            Logger.getLogger(ExerciseController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void load() {
        try {
            // Lấy câu hỏi hiện tại
            CauHoi ch = this.cauhoi.get(this.currentIdx);

            // Lấy danh sách đáp án cho câu hỏi
            ChoiceServices da = new ChoiceServices();
            List<DapAn> das = da.getDapAnTheoCauHoiID(ch.getId());

            // Clear radio buttons cũ trước khi tạo mới
            radioButtonsContainer.getChildren().clear();  // Xóa hết các radio buttons cũ trong container

            // Tạo động các radio buttons cho đáp án của câu hỏi
            radioButtons.clear();  // Xóa danh sách radio buttons cũ

            // gắn nội dung câu hỏi vào
            for (int i = 0; i < das.size(); i++) {
                RadioButton radioButton = new RadioButton(das.get(i).getNoiDung());
                radioButton.setUserData(String.valueOf(i));   // dap an lua chon 0,1,2,3
                radioButtons.add(radioButton);
                radioButtonsContainer.getChildren().add(radioButton);  // Thêm radio button vào container
            }

            // Tạo ToggleGroup để chỉ cho phép chọn một radio button
            ToggleGroup toggleGroup = new ToggleGroup();
            for (RadioButton radioButton : radioButtons) {
                radioButton.setToggleGroup(toggleGroup);
            }

            // Kiểm tra và khôi phục lựa chọn nếu có
            String luaChon = cauHoiMap.get(this.currentIdx);  // Kiểm tra lựa chọn đã lưu cho câu này
            if (luaChon != null) {
                // Duyệt qua các radio button và chọn lại radio button tương ứng với lựa chọn đã lưu
                for (RadioButton radioButton : radioButtons) {
                    if (radioButton.getUserData().equals(luaChon)) {
                        radioButton.setSelected(true);

                        break;
                    }
                }
            }

            // Cập nhật nội dung câu hỏi
            this.txtContent.setText(ch.getNoiDung());

            // Lưu lựa chọn của người dùng khi họ chọn đáp án và lưu vào map
            for (RadioButton radioButton : radioButtons) {
                radioButton.setOnAction(e -> {
                    String luaChon1 = (String) radioButton.getUserData();  // Lấy giá trị 'A', 'B', 'C', 'D'
                    cauHoiMap.put(this.currentIdx, luaChon1);  // Lưu vào Map
                });
            }

        } catch (SQLException ex) {
            Logger.getLogger(CourseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String remainingTime = preferences.get(TIMER_KEY, "24:31");  // Thời gian mặc định nếu không có dữ liệu lưu trữ
        String[] timeParts = remainingTime.split(":");
        minutes = Integer.parseInt(timeParts[0]);
        seconds = Integer.parseInt(timeParts[1]);

        // Cập nhật thời gian đã lưu
        updateTimeDisplay();

    }

    public void prevHandler() throws SQLException {
        if (this.currentIdx < this.cauhoi.size() - 1) {
            this.currentIdx++;
        } else {
            this.currentIdx = 0;
        }

        load();

    }

    public void nextHandler() throws SQLException {
        if (this.currentIdx > 0) {
            this.currentIdx--;
        } else {
            this.currentIdx = this.cauhoi.size() - 1;
        }

        load();

    }

    public void startTimer() {
        // Tạo Timeline để thực hiện việc đếm ngược mỗi giây
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateTimer())
        );

        // Lặp vô hạn (đếm ngược không giới hạn)
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Bắt đầu timeline
        timeline.play();
    }

    private void updateTimer() {
        if (seconds == 0) {
            if (minutes == 0) {
                // Thời gian đã hết
                timerText.setText("Thời gian đã hết!");
                // Dừng lại nếu hết thời gian
            } else {
                minutes--;
                seconds = 59;
            }
        } else {
            seconds--;
        }

        // Cập nhật lại thời gian hiển thị
        String time = String.format("Thời gian còn lại: %02d:%02d", minutes, seconds);
        saveState();
        timerText.setText(time);
    }

    private void updateTimeDisplay() {
        String time = String.format("Thời gian còn lại: %02d:%02d", minutes, seconds);
        timerText.setText(time);
    }

    public void saveState() {
        String timeRemaining = String.format("%02d:%02d", minutes, seconds);
        preferences.put(TIMER_KEY, timeRemaining);

//        entrySet() trả về danh sách các cặp key-value chứa trong Map
//Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
//    System.out.println("Length: " + entrySet.size());
//    for(Map.Entry<String, Integer> entry : entrySet) {
//        System.out.println("Key - Value: " + entry.getKey() + " - " + entry.getValue());
        for (Map.Entry<Integer, String> entry : cauHoiMap.entrySet()) {
            int questionIndex = entry.getKey();  // Chỉ mục câu hỏi
            String luaChon = entry.getValue();  // Đáp án đã chọn
            preferences.put(QUESTION_KEY + questionIndex, luaChon);  // Lưu lựa chọn cho câu hỏi theo index
        }

    }

    public void khoiPhucCauHoi() {
        if (this.cauhoi != null && !this.cauhoi.isEmpty()) {
            // Duyệt qua từng câu hỏi để khôi phục lựa chọn của người dùng
            for (int i = 0; i < this.cauhoi.size(); i++) {
                // Lấy lựa chọn đã lưu cho câu hỏi i từ Preferences
                String luaChon = preferences.get(QUESTION_KEY + i, null);  // Nếu không có dữ liệu thì trả về null
                if (luaChon != null) {
                    // Lưu lựa chọn vào Map, để khi cần có thể khôi phục
                    cauHoiMap.put(i, luaChon);

                    // Khôi phục lựa chọn vào các RadioButton
                    for (RadioButton radioButton : radioButtons) {
                        // Kiểm tra xem radio button nào đã được lưu và chọn lại
                        if (radioButton.getUserData().equals(luaChon)) {
                            radioButton.setSelected(true);
                            break;  // Chỉ cần chọn 1 radio button thôi, nên dùng break để thoát vòng lặp
                        }
                    }
                }
            }
        }
    }

    public void submit() throws SQLException {
        double dem1 = 0; // Biến đếm số câu trả lời đúng

        // Duyệt qua tất cả các câu hỏi trong map để kiểm tra đáp án
        for (Map.Entry<Integer, String> entry : cauHoiMap.entrySet()) {
            int questionIndex = entry.getKey();  // Lấy chỉ mục câu hỏi
            String luaChon = entry.getValue();  // Lấy đáp án người dùng đã chọn (0, 1, 2, 3)

            // Lấy câu hỏi hiện tại từ cơ sở dữ liệu (dùng ID câu hỏi)
            CauHoi ch = this.cauhoi.get(questionIndex);

            ChoiceServices dapAnServices = new ChoiceServices();
            List<DapAn> das = dapAnServices.getDapAnTheoCauHoiID(ch.getId());
            int true1 = -1;  // 

            for (int i = 0; i < das.size(); i++) {
                if (das.get(i).isDapAnDung() == true) {
                    true1 = i;  // Lưu chỉ mục của đáp án đúng
                }
            }

            if (Integer.parseInt(luaChon) == true1) {
                dem1++;
            }

        }
        double a = dem1 / (double) cauhoi.size();

        a = Double.parseDouble(String.format("%.2f", a)) * 100;
        ketQua = (int) a;
        dem.setText(String.valueOf(ketQua));
        // Lấy cửa sổ (stage) hiện tại từ scene
        // Lấy cửa sổ hiện tại từ nút hoặc bất kỳ thành phần nào trong scene
        

        UserService userService = new UserService();
        String email = SessionManager.getLoggedInEmail();
        List<NguoiDung> allUsers = userService.getAllUsers();
        NguoiDung user = allUsers.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        int hocVienID = userService.getHocVienIDFromNguoiDung(user.getId());
        SubmitServices nopBaiServices = new SubmitServices();
        if((nopBaiServices.submitSQL(hocVienID, baiTapID, ketQua) == 1)){
             showAlert("Nộp thành công!", "Số điểm bạn là: " + String.valueOf(ketQua), Alert.AlertType.INFORMATION);
        }
        Stage currentStage = (Stage) btnClose.getScene().getWindow();
        currentStage.close();
            
       
        

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

    public void setBaiTapID(int baiTapID) {
        this.baiTapID = baiTapID;
    }

    /**
     * @return the ketQua
     */
    public int getKetQua() {
        return ketQua;
    }

}

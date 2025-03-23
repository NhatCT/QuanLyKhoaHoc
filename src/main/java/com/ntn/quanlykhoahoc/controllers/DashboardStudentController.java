package com.ntn.quanlykhoahoc.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class DashboardStudentController {

    @FXML
    private Button dashboardBtn;
    @FXML
    private Button coursesBtn;
    @FXML
    private Button profileBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private GridPane coursesGrid;
    @FXML
    private TableView<String> cartTable;
    @FXML
    private Button payButton;
    @FXML
    private Button removeButton;

    @FXML
    public void initialize() {
        // Thêm dữ liệu mẫu vào GridPane
        String[] courseNames = {"Java", "Python", "C++", "Web Dev", "Data Science", "AI"};
        String[] prices = {"$10", "$15", "$12", "$20", "$25", "$30"};

        for (int i = 0; i < courseNames.length; i++) {
            final String courseName = courseNames[i];
            Button addButton = new Button("Add");
            addButton.setOnAction(e -> System.out.println(courseName + " added!"));

            coursesGrid.add(addButton, i % 3, i / 3);
        }

    }
}

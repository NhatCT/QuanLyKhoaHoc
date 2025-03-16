package com.ntn.quanlykhoahoc;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"),640,650); 
        stage.setScene(scene);
        stage.setTitle("Quản Lý Khóa Học");
        stage.show();
    }
    
public static void setRoot(String fxml) throws IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/ntn/views/" + fxml + ".fxml"));
    Parent root = loader.load();
    scene.setRoot(root);
}


private static Parent loadFXML(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/ntn/views/" + fxml + ".fxml"));
    return fxmlLoader.load();
}


    public static void main(String[] args) {
        launch(); // Chạy ứng dụng JavaFX
    }
}
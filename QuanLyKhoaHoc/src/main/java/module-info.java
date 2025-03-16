module com.ntn.quanlykhoahoc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.ntn.quanlykhoahoc to javafx.fxml;
    opens com.ntn.quanlykhoahoc.controllers to javafx.fxml; // Thêm dòng này để JavaFX có thể truy cập các controller

    exports com.ntn.quanlykhoahoc;
    exports com.ntn.quanlykhoahoc.controllers; // Xuất package controllers nếu cần
}

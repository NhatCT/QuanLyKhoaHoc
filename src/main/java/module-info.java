module com.ntn.quanlykhoahoc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires java.logging;
    requires jakarta.mail;

    // Mở package cho JavaFX (FXML)
    opens com.ntn.quanlykhoahoc to javafx.fxml;
    opens com.ntn.quanlykhoahoc.controllers to javafx.fxml;
    opens com.ntn.quanlykhoahoc.models to javafx.fxml;

    // Xuất package cho các module khác sử dụng
    exports com.ntn.quanlykhoahoc;
    exports com.ntn.quanlykhoahoc.controllers;

    // 🚀 Sửa lỗi test không thể truy cập Database
    exports com.ntn.quanlykhoahoc.database; 
    opens com.ntn.quanlykhoahoc.database;
}

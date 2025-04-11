module com.ntn.quanlykhoahoc {
    // Required modules
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires java.logging;
    requires jakarta.mail;

    // Open packages for reflection (needed by JavaFX FXML and javafx.base)
    opens com.ntn.quanlykhoahoc to javafx.fxml;
    opens com.ntn.quanlykhoahoc.controllers to javafx.fxml;
    opens com.ntn.quanlykhoahoc.pojo to javafx.fxml, javafx.base; // Mở cho javafx.base
    opens com.ntn.quanlykhoahoc.database to javafx.fxml; // Kiểm tra lại xem có cần thiết không

    // Export packages for external use
    exports com.ntn.quanlykhoahoc;
    exports com.ntn.quanlykhoahoc.controllers;
    exports com.ntn.quanlykhoahoc.database; // Đảm bảo package tồn tại
}
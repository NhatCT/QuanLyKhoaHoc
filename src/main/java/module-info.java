module com.ntn.quanlykhoahoc {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires java.logging;
    requires jakarta.mail;

    opens com.ntn.quanlykhoahoc to javafx.fxml;
    opens com.ntn.quanlykhoahoc.controllers to javafx.fxml;
    opens com.ntn.quanlykhoahoc.pojo to javafx.fxml, javafx.base;

    exports com.ntn.quanlykhoahoc;
    exports com.ntn.quanlykhoahoc.controllers;
    exports com.ntn.quanlykhoahoc.database;
}
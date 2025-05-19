module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;

    opens com.example.demo to javafx.fxml;
    opens com.example.demo.controller to javafx.fxml;

    exports com.example.demo;
    exports com.example.demo.controller;
}

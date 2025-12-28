module app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires jbcrypt;

    opens app.client to javafx.fxml;
    opens app.client.views to javafx.fxml;
    opens app.types to javafx.base;
    exports app.client;
    exports app.config;
    exports app.util;
}
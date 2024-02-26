module com.freshgrown {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.freshgrown to javafx.fxml;

    exports com.freshgrown;
}

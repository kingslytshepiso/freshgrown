package com.freshgrown;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static Stage stage;
    public static Integer currentTransactionId = null;
    public static Integer transactionViewId = null;
    private DatabaseController db = new DatabaseController();

    @Override
    public void start(Stage stage) throws IOException {
        App.stage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                db.destroySession();
                App.currentTransactionId = null;
                Platform.exit();
                System.exit(0);
            }
        });
        db.createSession("$-cashier-$-session", 444123);
        scene = new Scene(loadFXML("login"), 1250, 650);
        stage.setTitle("Freshgrown v1");
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() {
        // Save file
    }

    public static void main(String[] args) {
        launch();
    }

}

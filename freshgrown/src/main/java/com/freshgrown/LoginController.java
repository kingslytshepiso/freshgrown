package com.freshgrown;

import java.io.IOException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button submitButton;

    private DatabaseController db = new DatabaseController();

    Alert alert1 = new Alert(AlertType.NONE);

    public Boolean popPrompt(String message) {
        alert1.setAlertType(AlertType.CONFIRMATION);
        alert1.setHeaderText(message);
        Optional<ButtonType> result = alert1.showAndWait();
        if (!result.isPresent()) {
            return false;
        } else if (result.get() == ButtonType.OK) {
            return true;
        } else if (result.get() == ButtonType.CANCEL) {
            return false;
        } else
            return false;
    }

    public void popAlert(String message) {
        alert1.setAlertType(AlertType.INFORMATION);
        alert1.setHeaderText(message);
        alert1.showAndWait();
    }

    @FXML
    void initialize() {

        submitButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                String usernameValue = emailIdField.getText();
                String passwordValue = passwordField.getText();
                User user = db.login(usernameValue, passwordValue);
                boolean isValid = user != null;
                if (isValid) {
                    popAlert("Login successfull");
                    try {
                        App.setRoot("search");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    popAlert("Login error");
                }
            }

        });

    }
}

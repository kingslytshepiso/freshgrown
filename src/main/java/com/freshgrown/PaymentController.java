package com.freshgrown;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class PaymentController {

    @FXML
    private TextField cashTxt;

    @FXML
    private TextField cardNoTxt;

    @FXML
    private DatePicker expDate;

    @FXML
    private ToggleGroup paymentType;

    @FXML
    private Button cashPayBtn;

    @FXML
    private Label amountLbl;

    @FXML
    private ComboBox<String> bankCmb;

    @FXML
    private Button cardPayBtn;

    @FXML
    private Label changeLbl;

    @FXML
    private TextField securityNoTxt;

    @FXML
    private TextField firstNameTxt;

    @FXML
    private TextField lastNameTxt;

    @FXML
    private VBox cardContainer;

    @FXML
    private VBox cashContainer;

    @FXML
    private RadioButton cardRdo;

    @FXML
    private RadioButton cashRdo;

    @FXML

    DatabaseController db = new DatabaseController();

    Alert alert1 = new Alert(AlertType.NONE);

    public void popAlert(String title, String message) {
        alert1.setAlertType(AlertType.INFORMATION);
        alert1.setTitle(title);
        alert1.setHeaderText(message);
        alert1.showAndWait();
    }

    public Boolean validateCardPayment() {
        String fname = firstNameTxt.getText();
        String lname = lastNameTxt.getText();
        String bank = bankCmb.getValue() == null ? new String() : bankCmb.getValue();
        Long cardNo = !(cardNoTxt.getText().isEmpty()) ? Long.parseLong(cardNoTxt.getText()) : 0;
        LocalDate expiration = expDate.getValue();
        Long securityNo = !(securityNoTxt.getText().isEmpty()) ? Long.parseLong(securityNoTxt.getText()) : 0;
        int invalidCount = 0;
        // First Name
        if (fname.isEmpty()) {
            invalidCount++;
            firstNameTxt.setStyle("-fx-border-color: red");
        } else {
            firstNameTxt.setStyle("-fx-border-color: green");
        }
        // last Name
        if (lname.isEmpty()) {
            invalidCount++;
            lastNameTxt.setStyle("-fx-border-color: red");
        } else {
            lastNameTxt.setStyle("-fx-border-color: green");
        }
        // Bank
        if (bank.isEmpty()) {
            invalidCount++;
            bankCmb.setStyle("-fx-border-color: red");
        } else {
            bankCmb.setStyle("-fx-border-color: green");
        }
        // Card Number
        if (cardNo == 0) {
            invalidCount++;
            cardNoTxt.setStyle("-fx-border-color: red");
        } else {
            cardNoTxt.setStyle("-fx-border-color: green");
        }
        // Expiry Date
        if (expiration == null) {
            invalidCount++;
            expDate.setStyle("-fx-border-color: red");
        } else {
            expDate.setStyle("-fx-border-color: green");
        }
        // Security Number
        if (securityNo == 0) {
            invalidCount++;
            securityNoTxt.setStyle("-fx-border-color: red");
        } else {
            securityNoTxt.setStyle("-fx-border-color: green");
        }
        if (invalidCount > 0) {
            return false;
        } else {
            return true;
        }
    }

    public void restrictToNumeric(@SuppressWarnings("exports") TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            var charStr = newValue.toCharArray();
            Boolean containsLetter = false;
            for (Character i : charStr) {
                if (Character.isAlphabetic(i)) {
                    containsLetter = true;
                }
            }
            if (!newValue.isEmpty()) {
                if (containsLetter) {
                    field.setText(oldValue);
                } else {
                    field.setText(newValue);
                }
            } else {

            }
        });
    }

    public void initialize() {
        initializeNavigation();
        restrictToNumeric(cardNoTxt);
        restrictToNumeric(securityNoTxt);
        cardContainer.setDisable(true);
        cashPayBtn.setDisable(true);
        Transaction current = db.getCurrentTransaction();
        amountLbl.setText(current.amount.toString());
        cashTxt.textProperty().addListener((observable, oldValue, newValue) -> {
            var charStr = newValue.toCharArray();
            Boolean containsLetter = false;
            for (Character i : charStr) {
                if (Character.isAlphabetic(i)) {
                    containsLetter = true;
                }
            }
            if (!newValue.isEmpty()) {
                if (containsLetter) {
                    cashTxt.setText(oldValue);
                    cashPayBtn.setDisable(true);
                } else {
                    cashPayBtn.setDisable(false);
                    Double totalChange = Double.parseDouble(newValue) - current.amount;
                    if (Double.parseDouble(newValue) < current.amount) {
                        cashTxt.setStyle("-fx-border-color: red");
                        cashPayBtn.setDisable(true);
                    } else {
                        cashTxt.setStyle("-fx-border-color: green");
                        cashPayBtn.setDisable(false);
                    }
                    changeLbl.setText(totalChange.toString());
                }
            } else {
                changeLbl.setText("0");
                cashPayBtn.setDisable(true);
            }
        });
        cashRdo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (cashRdo.isSelected()) {
                    cardContainer.setDisable(true);
                    cashContainer.setDisable(false);
                }
            }
        });
        cashPayBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ev) {
                ArrayList<CartItem> cart = db.getCart();
                ArrayList<SlipItem> slip = new ArrayList<SlipItem>();
                for (CartItem c : cart) {
                    slip.add(new SlipItem(null, c.name, c.quantity, c.price));
                }
                Boolean isSlipSuccess = db.createSlip(slip, cart);
                if (isSlipSuccess) {
                    Boolean isPaymentSuccess;
                    User currentUser = db.getCurrentUser();
                    String fullname = currentUser.surname + " " + currentUser.name;
                    if (cashRdo.isSelected()) {
                        Double totalAmount = Double.parseDouble(cashTxt.getText());
                        Double change = Double.parseDouble(changeLbl.getText());
                        isPaymentSuccess = db.executePayment(slip,
                                totalAmount, change, 0,
                                fullname);
                    } else {
                        Integer cardNo = Integer.parseInt(cardNoTxt.getText());
                        isPaymentSuccess = db.executePayment(slip,
                                0.0, 0.0, cardNo,
                                fullname);
                    }
                    if (isPaymentSuccess) {
                        db.clearCart();
                        popAlert("Success", "Payment successfull.");
                        try {
                            App.setRoot("slip");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        popAlert("Payment Error", "There was an error completing the request");
                    }

                } else {
                    popAlert("Error",
                            "There was a problem creating finalizing the slip, make sure " +
                                    "the cart item's quantities dont exceed what's available in the " +
                                    "inventory.");
                }

            }
        });
        cardPayBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                ArrayList<CartItem> cart = db.getCart();
                ArrayList<SlipItem> slip = new ArrayList<SlipItem>();
                for (CartItem c : cart) {
                    slip.add(new SlipItem(null, c.name, c.quantity, c.price));
                }
                Boolean isSlipSuccess = db.createSlip(slip, cart);
                if (isSlipSuccess) {
                    Boolean isPaymentSuccess;
                    User currentUser = db.getCurrentUser();
                    String fullname = currentUser.surname + " " + currentUser.name;
                    if (cashRdo.isSelected()) {
                        Double totalAmount = Double.parseDouble(cashTxt.getText());
                        Double change = Double.parseDouble(changeLbl.getText());
                        isPaymentSuccess = db.executePayment(slip,
                                totalAmount, change, 0,
                                fullname);
                    } else {
                        Integer cardNo = Integer.parseInt(cardNoTxt.getText());
                        isPaymentSuccess = db.executePayment(slip,
                                0.0, 0.0, cardNo,
                                fullname);
                    }
                    if (isPaymentSuccess) {
                        db.clearCart();
                        popAlert("Success", "Payment successfull.");
                        try {
                            App.setRoot("slip");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        popAlert("Payment Error", "There was an error completing the request");
                    }

                }
            }
        });
        cardRdo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (cardRdo.isSelected()) {
                    cashContainer.setDisable(true);
                    cardContainer.setDisable(false);
                }
            }
        });
        bankCmb.setItems(
                FXCollections.observableArrayList("Standard Bank", "Capitec", "ABSA", "FNB"));
    }

    // Navigation Setup
    @FXML
    private MenuItem aboutMnu;

    @FXML
    private MenuItem logoutMnu;

    @FXML
    private MenuItem historyMnu;

    @FXML
    private MenuItem createTransMnu;

    @FXML
    private MenuItem cartMnu;

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

    /**
     * Initialize and create event handlers for navigation
     * for all pages. Different pages may handle this differently
     * depending on where they are in the event logic.
     */
    void initializeNavigation() {
        logoutMnu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Boolean isOk = popPrompt("Are you sure you wish to log out, " +
                        "and discard the current transaction?");
                if (isOk) {
                    db.destroySession();
                    try {
                        App.setRoot("login");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
        cartMnu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    App.setRoot("search");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        createTransMnu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Boolean isOk = popPrompt("Navigating away from this page will discard the current transaction.");
                if (isOk) {
                    try {
                        App.setRoot("search");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
        historyMnu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Boolean isOk = popPrompt("Navigating away from this page will discard the current transaction.");
                if (isOk) {
                    try {
                        App.setRoot("history");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
    }
}

package com.freshgrown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.layout.GridPane;

public class TransactionViewController {
    @FXML
    private Label cashierLbl;

    @FXML
    private GridPane resultGrid;

    @FXML
    private Label idLbl;

    @FXML
    private Label dateLbl;

    @FXML
    private Label amountLbl;

    @FXML
    private Label costLbl;

    @FXML
    private Label cardNoLbl;

    @FXML
    private Label changeLbl;

    @FXML
    private Label statusLbl;

    @FXML
    private Pagination pgBtn;

    private DatabaseController db = new DatabaseController();

    public void initialize() {
        initializeNavigation();
        loadTransaction();
    }

    public void loadTransaction() {
        Transaction tr = db.getTransactionWithId(App.transactionViewId);
        ArrayList<CartItem> products = db.getTransactionItemsWithId(tr.id);
        for (int i = 0; i < products.size(); i++) {
            Label barcodeLbl = new Label(products.get(i).barcode.toString());
            Label nameLbl = new Label(products.get(i).name);
            Label priceLbl = new Label("R " + products.get(i).price.toString());
            resultGrid.add(barcodeLbl, 0, i);
            resultGrid.add(nameLbl, 1, i);
            resultGrid.add(priceLbl, 2, i);
        }
        if (tr != null) {
            idLbl.setText(tr.id.toString() == null ? "" : tr.id.toString());
            dateLbl.setText(tr.date.toString() == null ? "" : tr.date.toString());
            statusLbl.setText(tr.paymentStatus.toString() == null ? "" : tr.paymentStatus.toString());
            amountLbl.setText(tr.amount.toString() == null ? "" : tr.amount.toString());
            costLbl.setText(tr.totalCost.toString() == null ? "" : tr.totalCost.toString());
            changeLbl.setText(tr.change.toString() == null ? "" : tr.change.toString());
            cardNoLbl.setText(tr.bankCardNo.toString() == null ? "" : tr.bankCardNo.toString());
            cashierLbl.setText(tr.cashierName == null ? "" : tr.cashierName);

        }
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

    Alert alert1 = new Alert(AlertType.NONE);

    public Boolean popPrompt(String message) {
        alert1.setAlertType(AlertType.CONFIRMATION);
        alert1.setContentText(message);
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
        alert1.setContentText(message);
        alert1.showAndWait();
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
                Boolean isOk = popPrompt("Are you sure you wish to log out");
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
                try {
                    App.setRoot("search");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        historyMnu.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    App.setRoot("history");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}

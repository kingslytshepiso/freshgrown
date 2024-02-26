package com.freshgrown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class HistoryController {
    @FXML
    private Pagination pgBtn;

    @FXML
    private GridPane resultGrid;

    private DatabaseController db = new DatabaseController();

    public void initialize() {
        initializeNavigation();
        pgBtn.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                loadHistory(pageIndex);
                return new Text();
            }
        });
        loadHistory(0);
    }

    public void loadHistory(Integer from) {
        Node g = resultGrid.getChildren().get(0);
        resultGrid.getChildren().clear();
        ArrayList<Transaction> history = db.getTransactions();
        Double divided = (double) (history.size() / 10);
        Boolean hasMore = divided % 10 > 0;
        var pageCount = hasMore ? divided + 1 : divided;

        pgBtn.setPageCount((int) pageCount);
        for (int i = 0; i < 10; i++) {
            try {
                var item = history.get(i + 10 * from);
                Button viewBtn = new Button("view");
                viewBtn.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        App.transactionViewId = item.id;
                        try {
                            App.setRoot("transaction");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Label idLbl = new Label("#" + item.id.toString());
                Label dateLbl = new Label(item.date);
                resultGrid.add(idLbl, 0, i);
                resultGrid.add(dateLbl, 1, i);
                resultGrid.add(viewBtn, 2, i);
            } catch (Exception ex) {
                i = 10;
            }

        }
        resultGrid.getChildren().add(0, g);
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
        alert1.setHeaderText(message);
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

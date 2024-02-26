package com.freshgrown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class SearchController {
    @FXML
    private GridPane resultGrid;

    @FXML
    private Button searchBtn;

    @FXML
    private TextField searchTxt;

    @FXML
    private GridPane cartContent;

    @FXML
    private Label amountLbl;

    @FXML
    private Button proceedBtn;

    @FXML
    private Button clearCartBtn;

    @FXML
    private Pagination pgBtn;

    Dialog<String> popup = new Dialog<String>();
    Dialog<String> alert = new Dialog<String>();
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

    private DatabaseController db = new DatabaseController();

    public void initialize() {
        ButtonType yesBtn = new ButtonType("Yes", ButtonData.YES);
        ButtonType noBtn = new ButtonType("No", ButtonData.NO);
        ButtonType okButton = new ButtonType("OK", ButtonData.YES);
        popup.getDialogPane().getButtonTypes().addAll(noBtn, yesBtn);
        alert.getDialogPane().getButtonTypes().add(okButton);
        searchBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String keyword = searchTxt.getText().toLowerCase();
                updateResultGrid(0, keyword);
            }
        });
        proceedBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Integer returnId = db.captureTransaction(Double.parseDouble(amountLbl.getText()));
                App.currentTransactionId = returnId;
                Boolean transactionCreated = returnId > 0 ? true : false;
                if (transactionCreated) {
                    try {
                        App.setRoot("Payment");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    popAlert("Error");
                }
            }
        });
        clearCartBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                db.clearCart();
                updateCart();
            }
        });
        pgBtn.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                String keyword = searchTxt.getText().toLowerCase();
                updateResultGrid(pageIndex, keyword);
                return new Text();
            }
        });
        initializeNavigation();
        updateCart();
    }

    public void updateResultGrid(Integer from, String keyword) {
        Node g = resultGrid.getChildren().get(0);
        resultGrid.getChildren().clear();
        ArrayList<Product> products = keyword.isEmpty() ? db.getProducts() : db.searchProducts(keyword);
        Double divided = products.size() / 10.0;
        Boolean hasMore = divided % 1 != 0;
        var pageCount = hasMore ? divided + 1 : divided;
        int page = products.size() >= 10 ? from : 0;
        int test = (int) pageCount;
        try {
            pgBtn.setPageCount(test);
            for (int i = 0; i < 10; i++) {
                try {
                    var p = products.get(i + 10 * page);
                    Button addBtn = new Button("add");
                    addBtn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            ArrayList<CartItem> cart = db.getCart();
                            Boolean itemExists = false;
                            for (CartItem j : cart) {
                                if (j.name.equalsIgnoreCase(p.getName())) {
                                    itemExists = true;
                                }
                            }
                            if (itemExists) {
                                db.insertToCart(p.getBarcode());
                            } else {
                                db.insertToCart(p.getBarcode(), p.getName(), 1, p.getPrice());

                            }
                            updateCart();
                        }
                    });
                    Label barcodeLbl = new Label("#" + p.getBarcode().toString());
                    barcodeLbl.setFont(new Font(17));
                    Label nameLbl = new Label(p.getName().toString());
                    nameLbl.setFont(new Font(17));
                    Label descLbl = new Label(p.getDescription().toString());
                    descLbl.setFont(new Font(17));
                    Label typeLbl = new Label(p.getType().toString());
                    typeLbl.setFont(new Font(17));
                    Label priceLbl = new Label("R " + p.getPrice().toString());
                    priceLbl.setFont(new Font(17));
                    Label availableLbl = new Label(p.getQuantityAvailable().toString());
                    availableLbl.setFont(new Font(17));
                    resultGrid.add(barcodeLbl, 0, i);
                    resultGrid.add(nameLbl, 1, i);
                    resultGrid.add(descLbl, 2, i);
                    resultGrid.add(typeLbl, 3, i);
                    resultGrid.add(priceLbl, 4, i);
                    resultGrid.add(availableLbl, 5, i);
                    resultGrid.add(addBtn, 6, i);
                    i = i == products.size() - 1 ? 10 : i;
                } catch (Exception ex) {
                }

            }
            resultGrid.getChildren().add(0, g);
        } catch (Exception ex) {

        }

    }

    public void updateCart() {
        Node g = cartContent.getChildren().get(0);
        cartContent.getChildren().clear();
        ArrayList<CartItem> cart = db.getCart();
        Double totalAmount = 0.00;
        for (int i = 0; i < cart.size(); i++) {
            var thisItem = cart.get(i);
            totalAmount = totalAmount + thisItem.price * thisItem.quantity;
            Button removeBtn = new Button("remove");
            removeBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // popAMessage("Are you sure u wamt to remove this item");
                    db.removeFromCart(thisItem.barcode, thisItem.quantity);
                    updateCart();
                }
            });
            Label nameLbl = new Label(cart.get(i).name);
            // TextField quantityTxt = new TextField(cart.get(i).quantity.toString());
            Label quantityLbl = new Label(cart.get(i).quantity.toString());
            Label priceLbl = new Label("R " + cart.get(i).price.toString());
            cartContent.add(nameLbl, 0, i);
            cartContent.add(quantityLbl, 1, i);
            cartContent.add(priceLbl, 2, i);
            cartContent.add(removeBtn, 3, i);

        }
        if (!(cart.size() > 0)) {
            proceedBtn.setDisable(true);
        } else {
            proceedBtn.setDisable(false);
        }
        amountLbl.setText(totalAmount.toString());
        cartContent.getChildren().add(0, g);
    }

    public void loadMockData() {
        ArrayList<Product> products = db.getProducts();
        Double divided = (double) (products.size() / 10);
        Boolean hasMore = divided % 10 > 0;
        var pageCount = hasMore ? divided + 1 : divided;
        pgBtn.setPageCount((int) pageCount);
        for (int i = 0; i < 10; i++) {
            var p = products.get(i);

            Button addBtn = new Button("add");
            addBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    ArrayList<CartItem> cart = db.getCart();
                    Boolean itemExists = false;
                    for (CartItem i : cart) {
                        if (i.name.equalsIgnoreCase(p.getName())) {
                            itemExists = true;
                        }
                    }
                    if (itemExists) {
                        db.insertToCart(p.getBarcode());
                    } else {
                        db.insertToCart(p.getBarcode(), p.getName(), 1, p.getPrice());

                    }
                    updateCart();
                }
            });
            Label barcodeLbl = new Label("#" + products.get(i).getBarcode().toString());
            barcodeLbl.setFont(new Font(17));
            Label nameLbl = new Label(products.get(i).getName().toString());
            nameLbl.setFont(new Font(17));
            Label descLbl = new Label(products.get(i).getDescription().toString());
            descLbl.setFont(new Font(17));
            Label typeLbl = new Label(products.get(i).getType().toString());
            typeLbl.setFont(new Font(17));
            Label priceLbl = new Label("R " + products.get(i).getPrice().toString());
            priceLbl.setFont(new Font(17));
            Label availableLbl = new Label(products.get(i).getQuantityAvailable().toString());
            availableLbl.setFont(new Font(17));
            resultGrid.add(barcodeLbl, 0, i);
            resultGrid.add(nameLbl, 1, i);
            resultGrid.add(descLbl, 2, i);
            resultGrid.add(typeLbl, 3, i);
            resultGrid.add(priceLbl, 4, i);
            resultGrid.add(availableLbl, 5, i);
            resultGrid.add(addBtn, 6, i);
            i = i == products.size() - 1 ? 10 : i;
        }
    }

    public void loadColumns() {
        ArrayList<Label> labels = new ArrayList<Label>();
        labels.add(new Label("bardcode"));
        labels.add(new Label("name"));
        labels.add(new Label("description"));
        labels.add(new Label("type"));
        labels.add(new Label("price"));
        labels.add(new Label("available"));
        labels.add(new Label("action"));
        for (int i = 0; i < labels.size(); i++) {
            labels.get(i).setPadding(new Insets(10, 5, 10, 5));
            labels.get(i).setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            resultGrid.add(labels.get(i), i, 0);
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

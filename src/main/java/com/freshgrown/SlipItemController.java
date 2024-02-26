
package com.freshgrown;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class SlipItemController {
    @FXML
    private GridPane resultGrid;

    @FXML
    private Label changeAmntLbl;

    @FXML
    private Label totalLbl;

    @FXML
    private Button printBtn;

    @FXML
    private Label cashierLbl;

    @FXML
    private Label cashAmntLbl;

    @FXML
    private Label bankCardNoLbl;

    @FXML
    private VBox printSection;

    private DatabaseController db = new DatabaseController();

    Alert alert1 = new Alert(AlertType.NONE);

    public void popAlert(String title, String message) {
        alert1.setAlertType(AlertType.INFORMATION);
        alert1.setTitle(title);
        alert1.setHeaderText(message);
        alert1.showAndWait();
    }

    public void initialize() {
        initializeNavigation();
        printBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                printBtn.setVisible(false);
                PrinterJob printerJob = PrinterJob.createPrinterJob();
                JobSettings jobSet = printerJob.getJobSettings();
                LocalDateTime d = LocalDateTime.now();
                jobSet.setJobName("slip#" + d.toString());
                if (printerJob != null) {
                    WritableImage slipImg = printSection.snapshot(new SnapshotParameters(), null);
                    ImageView img = new ImageView(slipImg);
                    PageLayout pageLayout = printerJob.getPrinter().createPageLayout(Paper.A1,
                            PageOrientation.PORTRAIT, 0, 0, 0, 0);
                    boolean success = printerJob.printPage(pageLayout, img);
                    if (success) {
                        printerJob.endJob();
                        popAlert("Success", "Printing done!!");
                        printBtn.setVisible(true);
                    }
                }
            }
        });
        // loadColumns();
        loadSlip();
        db.clearSlip();
    }

    public void loadSlip() {
        Transaction tr = db.getCurrentTransaction();
        ArrayList<SlipItem> products = db.getSlip();
        for (int i = 0; i < products.size(); i++) {
            Label nameLbl = new Label(products.get(i).itemName);
            Label quantityLbl = new Label(products.get(i).quantity.toString());
            Label priceLbl = new Label("R " + products.get(i).price.toString());
            resultGrid.add(nameLbl, 0, i);
            resultGrid.add(quantityLbl, 1, i);
            resultGrid.add(priceLbl, 2, i);
        }
        if (tr != null) {
            totalLbl.setText(tr.amount.toString() == null ? "" : tr.amount.toString());
            cashAmntLbl.setText(tr.totalCost.toString() == null ? "" : tr.totalCost.toString());
            changeAmntLbl.setText(tr.change.toString() == null ? "" : tr.change.toString());
            bankCardNoLbl.setText(tr.bankCardNo.toString() == null ? "" : tr.bankCardNo.toString());
            cashierLbl.setText(tr.cashierName == null ? "" : tr.cashierName);
        }
    }

    public void loadMockData() {
        ArrayList<SlipItem> products = new ArrayList<SlipItem>();
        products.add(new SlipItem(1, "Apple", 5, 100.00));
        products.add(new SlipItem(1, "Apple", 5, 100.00));
        products.add(new SlipItem(1, "Pear", 3, 120.00));
        products.add(new SlipItem(1, "Pear", 3, 120.00));
        products.add(new SlipItem(1, "Pear", 3, 120.00));
        products.add(new SlipItem(1, "Pear", 3, 120.00));
        products.add(new SlipItem(1, "Pear", 3, 120.00));
        products.add(new SlipItem(1, "Pear", 3, 120.00));
        for (int i = 0; i < products.size(); i++) {
            Label nameLbl = new Label(products.get(i).itemName);
            Label quantityLbl = new Label(products.get(i).quantity.toString());
            Label priceLbl = new Label("R " + products.get(i).price.toString());
            resultGrid.add(nameLbl, 0, i);
            resultGrid.add(quantityLbl, 1, i);
            resultGrid.add(priceLbl, 2, i);

        }
    }

    public void loadColumns() {
        ArrayList<Label> labels = new ArrayList<Label>();
        labels.add(new Label("bardcode"));
        labels.add(new Label("name"));
        labels.add(new Label("price"));
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

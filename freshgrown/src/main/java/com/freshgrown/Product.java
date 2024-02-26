package com.freshgrown;

import javafx.beans.property.*;

public class Product {
    private SimpleIntegerProperty barcode;
    private SimpleStringProperty name;
    private SimpleStringProperty description;
    private SimpleDoubleProperty price;
    private SimpleIntegerProperty available;
    private SimpleStringProperty type;

    public Product() {}

    public Product(Integer barcode, String name, String description, Double price,
            Integer available, String type) {
        this.barcode = new SimpleIntegerProperty(barcode);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
        this.available = new SimpleIntegerProperty(available);
        this.type = new SimpleStringProperty(type);
    }

    public void SetBarcode(Integer value) {
        this.barcode = new SimpleIntegerProperty(value);
    }

    public void SetName(String value) {
        this.name = new SimpleStringProperty(value);
    }

    public void SetDescription(String value) {
        this.description = new SimpleStringProperty(value);
    }

    public void SetPrice(Double value) {
        this.price = new SimpleDoubleProperty(value);
    }

    public void SetAvailable(Integer value) {
        this.available = new SimpleIntegerProperty(value);
    }

    public void SetType(String value) {
        this.type = new SimpleStringProperty(value);
    }

    public Integer getBarcode() {
        return barcode.get();
    }

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public Double getPrice() {
        return price.get();
    }

    public Integer getQuantityAvailable() {
        return available.get();
    }

    public String getType() {
        return type.get();
    }
}

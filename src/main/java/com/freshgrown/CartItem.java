package com.freshgrown;

public class CartItem {
    public Integer barcode;
    public String name;
    public Integer quantity;
    public Double price;

    public CartItem() {

    }

    public CartItem(Integer barcode, String name, Integer quantity, Double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
}

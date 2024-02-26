package com.freshgrown;

public class SlipItem {
    public Integer id;
    public String itemName;
    public Integer quantity;
    public Double price;

    public SlipItem() {

    }

    public SlipItem(Integer id, String itemName,
            Integer quantity,
            Double price) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }
}
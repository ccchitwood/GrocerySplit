package edu.uga.cs.grocerysplit;

import java.util.List;

public class CheckedOutBasket {
    private String userID;
    private List<Product> items;
    private double totalPrice;

    public CheckedOutBasket() {
        // Default constructor for Firebase
    }

    public CheckedOutBasket(String userID, List<Product> items, double totalPrice) {
        this.userID = userID;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public String getUserID() {
        return userID;
    }

    public List<Product> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}


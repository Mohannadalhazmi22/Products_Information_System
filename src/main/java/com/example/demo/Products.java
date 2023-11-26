package com.example.demo;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Date;

public class Products {
    public SimpleIntegerProperty ID = new SimpleIntegerProperty(0);
    public SimpleStringProperty type = new SimpleStringProperty("");
    public SimpleStringProperty model= new SimpleStringProperty("");
    public SimpleDoubleProperty price = new SimpleDoubleProperty(0.0);
    public SimpleIntegerProperty count = new SimpleIntegerProperty(0);
    public SimpleStringProperty DeliveryDate = new SimpleStringProperty("");

    //A constructor to put each value to the right variable
    public Products(int id, String type, String model, double price, int count, Date DeliveryDate){
        this.ID.set(id);
        this.type.set(type);
        this.model.set(model);
        this.price.set(price);
        this.count.set(count);
        this.DeliveryDate.set(String.valueOf(DeliveryDate));
    }
    //getters

    public String getType() {
        return type.get();
    }

    public String getModel() {
        return model.get();
    }

    public double getPrice() {
        return price.get();
    }

    public int getCount() {
        return count.get();
    }

    public int getID() {
        return ID.get();
    }

    public String getDeliveryDate() {
        return DeliveryDate.get();
    }
}

package com.vendingmachine.repository;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SOFT_DRINK")
public class SoftDrink extends Beverage {

    @Column(length = 100)
    private String brand;

    protected SoftDrink() {}

    public SoftDrink(String id, String name, double price, String brand) {
        super(id, name, price);
        this.brand = brand;
    }

    public String getBrand() { return brand; }

    @Override
    public String getDescription() {
        return getName() + " (" + brand + ")";
    }
}

package com.vendingmachine.repository;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("TEA")
public class Tea extends Beverage {

    @Column(length = 100)
    private String flavor;

    protected Tea() {}

    public Tea(String id, String name, double price, String flavor) {
        super(id, name, price);
        this.flavor = flavor;
    }

    public String getFlavor() { return flavor; }

    @Override
    public String getDescription() {
        return getName() + " (Trà " + flavor + ")";
    }
}

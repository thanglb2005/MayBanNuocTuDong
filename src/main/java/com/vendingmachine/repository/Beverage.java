package com.vendingmachine.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "beverages")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "beverage_type")
public abstract class Beverage {

    @Id
    @Column(name = "beverage_id", length = 10)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private double price;

    protected Beverage() {}

    public Beverage(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    public abstract String getDescription();

    @Override
    public String toString() {
        return String.format("[%s] %s - %,.0f VND", id, getDescription(), price);
    }
}

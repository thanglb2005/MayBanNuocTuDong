package com.vendingmachine.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @OneToOne
    @JoinColumn(name = "beverage_id", nullable = false, unique = true)
    private Beverage beverage;

    @Column(nullable = false)
    private int quantity;

    protected InventoryItem() {}

    public InventoryItem(Beverage beverage, int quantity) {
        this.beverage = beverage;
        this.quantity = quantity;
    }

    public Long getItemId() { return itemId; }
    public Beverage getBeverage() { return beverage; }
    public int getQuantity() { return quantity; }

    public boolean isAvailable() { return quantity > 0; }

    public boolean reduce() {
        if (quantity <= 0) return false;
        quantity--;
        return true;
    }

    public void addStock(int amount) { this.quantity += amount; }
}

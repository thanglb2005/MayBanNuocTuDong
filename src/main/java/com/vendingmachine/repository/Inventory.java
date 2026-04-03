package com.vendingmachine.repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý tồn kho in-memory — dùng List&lt;InventoryItem&gt; (OOP thuần).
 * Không phải JPA entity, chỉ là domain object trong VendingMachine.
 */
public class Inventory {
    private List<InventoryItem> items = new ArrayList<>();

    public void addBeverage(Beverage beverage, int quantity) {
        for (InventoryItem item : items) {
            if (item.getBeverage().getId().equals(beverage.getId())) {
                item.addStock(quantity);
                return;
            }
        }
        items.add(new InventoryItem(beverage, quantity));
    }

    public List<InventoryItem> getAllItems() { return items; }

    public List<InventoryItem> getAvailableItems() {
        List<InventoryItem> available = new ArrayList<>();
        for (InventoryItem item : items) {
            if (item.isAvailable()) available.add(item);
        }
        return available;
    }

    public InventoryItem findItem(String beverageId) {
        for (InventoryItem item : items) {
            if (item.getBeverage().getId().equals(beverageId)) return item;
        }
        return null;
    }

    public Beverage getBeverage(String id) {
        InventoryItem item = findItem(id);
        return item != null ? item.getBeverage() : null;
    }

    public int getStock(String id) {
        InventoryItem item = findItem(id);
        return item != null ? item.getQuantity() : 0;
    }

    public boolean reduceStock(String id) {
        InventoryItem item = findItem(id);
        return item != null && item.reduce();
    }

    public boolean isAvailable(String id) {
        InventoryItem item = findItem(id);
        return item != null && item.isAvailable();
    }

    public boolean hasAnyStock() {
        for (InventoryItem item : items) {
            if (item.isAvailable()) return true;
        }
        return false;
    }
}

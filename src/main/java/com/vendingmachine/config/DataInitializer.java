package com.vendingmachine.config;

import com.vendingmachine.repository.*;
import com.vendingmachine.state.VendingMachine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final BeverageRepository beverageRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final TransactionRepository transactionRepository;

    public DataInitializer(BeverageRepository beverageRepository,
                           InventoryItemRepository inventoryItemRepository,
                           TransactionRepository transactionRepository) {
        this.beverageRepository = beverageRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        seedDataIfEmpty();
        loadIntoVendingMachine();
        System.out.println("=== Máy bán nước đã khởi động — http://localhost:9090 ===");
    }

    private void seedDataIfEmpty() {
        if (beverageRepository.count() > 0) return;

        Tea t1 = beverageRepository.save(new Tea("T01", "Trà Ô Long", 15000, "Ô Long"));
        Tea t2 = beverageRepository.save(new Tea("T02", "Trà Xanh", 12000, "Xanh"));
        SoftDrink s1 = beverageRepository.save(new SoftDrink("S01", "Coca-Cola", 10000, "Coca-Cola"));
        SoftDrink s2 = beverageRepository.save(new SoftDrink("S02", "Pepsi", 10000, "PepsiCo"));
        MineralWater w1 = beverageRepository.save(new MineralWater("W01", "Aquafina", 8000, 500));
        MineralWater w2 = beverageRepository.save(new MineralWater("W02", "Lavie", 7000, 350));

        inventoryItemRepository.save(new InventoryItem(t1, 5));
        inventoryItemRepository.save(new InventoryItem(t2, 4));
        inventoryItemRepository.save(new InventoryItem(s1, 5));
        inventoryItemRepository.save(new InventoryItem(s2, 3));
        inventoryItemRepository.save(new InventoryItem(w1, 6));
        inventoryItemRepository.save(new InventoryItem(w2, 5));

        System.out.println("[DB] Seed data: 6 beverages + inventory created.");
    }

    private void loadIntoVendingMachine() {
        VendingMachine machine = VendingMachine.getInstance();

        List<InventoryItem> items = inventoryItemRepository.findAll();
        for (InventoryItem item : items) {
            machine.getInventory().addBeverage(item.getBeverage(), item.getQuantity());
        }

        List<Transaction> transactions = transactionRepository.findAll();
        for (Transaction tx : transactions) {
            machine.addTransaction(tx);
        }

        System.out.println("[DB] Loaded " + items.size() + " inventory items, "
                + transactions.size() + " transactions from DB.");
    }
}

package com.vendingmachine.state;

import com.vendingmachine.repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Pattern (Context): Chỉ tồn tại duy nhất 1 instance.
 * State Pattern (Context): giữ -state, ủy quyền hành vi cho state hiện tại.
 *
 * Chuẩn học thuật:
 *   Singleton: -instance (static), -VendingMachine() (private), +getInstance()
 *   Context:   -state, +changeState(state) { this.state = state; state.setContext(this); }
 */
public class VendingMachine {

    // ==================== SINGLETON ====================
    private static VendingMachine instance;

    public static VendingMachine getInstance() {
        if (instance == null) {
            instance = new VendingMachine();
        }
        return instance;
    }

    private VendingMachine() {
        this.inventory = new Inventory();
        this.transactions = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.state = new ReadyState();
        this.state.setContext(this);
    }

    // ==================== STATE CONTEXT ====================
    private VendingMachineState state;
    private Inventory inventory;
    private Beverage selectedBeverage;
    private List<Transaction> transactions;
    private List<String> messages;

    public void changeState(VendingMachineState state) {
        this.state = state;
        state.setContext(this);
    }

    public VendingMachineState getState() { return state; }
    public Inventory getInventory() { return inventory; }

    public Beverage getSelectedBeverage() { return selectedBeverage; }
    public void setSelectedBeverage(Beverage b) { this.selectedBeverage = b; }

    // ==================== TRANSACTIONS ====================
    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;
        if (transaction.getId() != null) {
            boolean exists = transactions.stream()
                    .anyMatch(t -> t != null && t.getId() != null && t.getId().equals(transaction.getId()));
            if (exists) return;
        }
        transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    // ==================== MESSAGES (cho UI) ====================
    public void addMessage(String msg) { messages.add(msg); }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessages() { messages.clear(); }

    // ==================== ỦY QUYỀN CHO STATE ====================
    public void selectProduct(String beverageId) {
        state.selectProduct(beverageId);
    }

    public void processPayment(PaymentMethod method) {
        state.processPayment(method);
    }

    public void dispense() {
        state.dispense();
    }

    public static void resetInstance() {
        instance = null;
    }
}

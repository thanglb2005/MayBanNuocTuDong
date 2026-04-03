package com.vendingmachine.state;

import com.vendingmachine.repository.PaymentMethod;

public class OutOfStockState implements VendingMachineState {

    private VendingMachine context;

    @Override
    public void setContext(VendingMachine context) {
        this.context = context;
    }

    @Override
    public void selectProduct(String beverageId) {
        context.addMessage("Máy đã hết hàng. Vui lòng quay lại sau.");
    }

    @Override
    public void processPayment(PaymentMethod method) {
        context.addMessage("Máy đã hết hàng. Không thể thanh toán.");
    }

    @Override
    public void dispense() {
        context.addMessage("Máy đã hết hàng. Không có sản phẩm để xuất.");
    }

    @Override
    public String getStateName() {
        return "OUT_OF_STOCK";
    }
}

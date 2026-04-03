package com.vendingmachine.state;

import com.vendingmachine.repository.Beverage;
import com.vendingmachine.repository.PaymentMethod;

public class ReadyState implements VendingMachineState {

    private VendingMachine context;

    @Override
    public void setContext(VendingMachine context) {
        this.context = context;
    }

    @Override
    public void selectProduct(String beverageId) {
        Beverage beverage = context.getInventory().getBeverage(beverageId);

        if (beverage == null) {
            context.addMessage("Không tìm thấy sản phẩm: " + beverageId);
            return;
        }

        if (!context.getInventory().isAvailable(beverageId)) {
            context.addMessage("Sản phẩm \"" + beverage.getName() + "\" đã hết hàng.");
            if (!context.getInventory().hasAnyStock()) {
                context.changeState(new OutOfStockState());
            }
            return;
        }

        context.setSelectedBeverage(beverage);
        context.addMessage("Bạn đã chọn: " + beverage.getDescription()
                + " — Giá: " + String.format("%,.0f", beverage.getPrice()) + " VND");
        context.changeState(new ProcessingPaymentState(beverage));
    }

    @Override
    public void processPayment(PaymentMethod method) {
        context.addMessage("Vui lòng chọn sản phẩm trước khi thanh toán.");
    }

    @Override
    public void dispense() {
        context.addMessage("Vui lòng chọn sản phẩm và thanh toán trước.");
    }

    @Override
    public String getStateName() {
        return "READY";
    }
}
